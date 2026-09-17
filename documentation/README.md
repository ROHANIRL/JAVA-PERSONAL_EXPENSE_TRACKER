# Personal Expense Tracker - AI-Powered Household Budget & Nutrition Planner

A layered Java + JDBC + MySQL application that helps a household budget, save
toward a goal, and now also plan essential grocery needs (rice, dal, cooking
oil) based on each family member's body metrics - all fitted against the
household's actual budget. Ships with three interfaces sharing one backend:
a console app, a Swing desktop GUI, and a website.

## Novelty

Most student expense trackers stop at "log spending, set a budget." This
project connects spending to health outcomes: it computes each family
member's BMI, derives their individual daily energy requirement (Mifflin-St
Jeor BMR -> TDEE), converts that into a personalized essentials requirement
(rice/dal/oil), and checks whether the household's actual budget can support
it - flagging shortfalls and recommending trade-offs rather than treating the
family as one undifferentiated unit. A rule-based "AI" advisor layer ties
this together with the savings and budget data into a single set of
plain-language recommendations.

## Project Structure

```
ExpenseTracker/
├── src/
│   ├── model/
│   │   Expense.java, Category.java, Budget.java, SavingsSummary.java
│   │   FamilyMember.java, MemberNutritionResult.java, EssentialsPlan.java
│   ├── dao/
│   │   BaseDAO.java (abstract)
│   │   ExpenseDAO.java, BudgetDAO.java, CategoryDAO.java,
│   │   UserProfileDAO.java, FamilyMemberDAO.java
│   ├── service/
│   │   ExpenseService.java, BudgetService.java, SavingsService.java
│   │   NutritionService.java, RecommendationEngine.java (the "AI" layer)
│   ├── util/
│   │   DBConnection.java, FileManager.java, Validation.java, Json.java
│   ├── menu/
│   │   Menu.java
│   ├── web/
│   │   ApiServer.java (REST API + static file server, JDK-only, no framework)
│   ├── gui/
│   │   (Swing desktop GUI - 12 files)
│   ├── Main.java          -- console entry point
│   └── AppGUI.java        -- desktop GUI entry point
├── webapp/                -- the website frontend, served by ApiServer
│   index.html, style.css, app.js
│   lib/chart.umd.js       -- Chart.js, vendored locally (no CDN/internet needed)
├── data/
│   backup.txt, logs.txt
├── sql/
│   schema.sql, sample_data.sql
├── documentation/
│   README.md
└── screenshots/
```

## How the nutrition/essentials calculation works

For each family member, given age, gender, height, weight, and activity level:

1. **BMR** (Basal Metabolic Rate) via the Mifflin-St Jeor equation
2. **TDEE** (Total Daily Energy Expenditure) = BMR x activity multiplier
   (sedentary 1.2, light 1.375, moderate 1.55, active 1.725)
3. Split TDEE into a typical Indian dietary macro pattern: ~55% carbohydrate,
   ~15% protein, ~27% fat
4. The carbohydrate (staple) share is adjusted slightly by BMI category -
   Underweight +10%, Overweight -10%, Obese -15% - to nudge the staple
   allocation without touching protein/fat targets
5. Convert each macro's calories into a real grocery quantity using standard
   caloric densities: rice (raw) ~3.45 kcal/g, protein 4 kcal/g -> dal at
   ~23% protein by weight, fat 9 kcal/g -> cooking oil
6. Multiply by 30 days and sum across all family members for the household
   total, then price it out at approximate market rates

This is a simplified educational model built for a college project, not
medical or dietary advice - that disclaimer is also shown in the UI.

## The "AI" layer

`RecommendationEngine` is a transparent, rule-based advisor - a classical
expert-systems approach to AI, chosen deliberately over an external LLM API
call so the demo works fully offline and every recommendation can be traced
back to the exact rule that produced it (essentials cost vs income, BMI
flags, savings goal status, budget thresholds). This is easier to defend in
a viva than a black-box API response, and it can't fail because of a bad
Wi-Fi connection during a live demo.

## OOP Concept Map

| Concept | Where it lives |
|---|---|
| Class / Object / Constructor | Every file, e.g. `new FamilyMember(name, age, gender, heightCm, weightKg, activityLevel)` |
| Encapsulation | All `model/` classes - private fields, public getters/setters |
| **Inheritance** | `dao/BaseDAO.java` (abstract) -> all 5 DAOs `extends BaseDAO` |
| **Abstraction** | `BaseDAO.getEntityName()` is abstract - every DAO implements it differently |
| **Polymorphism (overriding)** | Each DAO's `getEntityName()`; `Expense.toString()`, `Category.toString()`, `FamilyMember.toString()` override `Object.toString()` |
| **Polymorphism (overloading)** | `ExpenseService.addExpense(...)` - one version takes an explicit date, one defaults to today |
| Packages | `model`, `dao`, `service`, `util`, `menu`, `web`, `gui` |
| Arrays / ArrayList | `List<Expense>`, `List<FamilyMember>` throughout; `String[][]` in the GUI's nav config |
| StringBuilder | `util/FileManager.java` (backup report), `web/ApiServer.java` (JSON responses) |
| Static members | `Validation`, `Json`, `DBConnection`'s connection field |

## Setup

### 1. Create the database
Run **`sql/schema.sql`** first, then **`sql/sample_data.sql`** (MySQL Workbench or CLI).

### 2. Get the MySQL JDBC driver
Install JDK 21 or later, then download MySQL Connector/J from
`https://dev.mysql.com/downloads/connector/j/`. Create the `lib` folder if it
does not already exist and put the downloaded `mysql-connector-j-*.jar` inside it.

### 3. Configure database credentials (do not edit Java source)
Open Command Prompt in the project folder and set these values for the current
window. Replace `YOUR_MYSQL_PASSWORD` with the password you use in MySQL Workbench:
```
set EXPENSE_DB_USER=root
set EXPENSE_DB_PASSWORD=YOUR_MYSQL_PASSWORD
```
`EXPENSE_DB_URL` is optional; it defaults to a local MySQL server on port 3306
using the `expense_tracker` database.

### 4. Run the website
Double-click `run-web.bat`, or run it from Command Prompt. It compiles every
source file and starts the site at `http://localhost:8080`.

If port 8080 is already in use, run `run-web.bat 8081` and open
`http://localhost:8081` instead.

### 5. Run another interface (optional)
After `run-web.bat` has compiled the project, use either command below from the
same Command Prompt window (so the database password setting is still present):
```
java --add-modules jdk.httpserver -cp "out;lib\*" Main
java --add-modules jdk.httpserver -cp "out;lib\*" AppGUI
```
Then open `http://localhost:8080` in a browser. The `webapp/` folder (HTML/CSS/JS)
is served automatically from the same server - there's nothing else to start,
no Node, no separate frontend server. Chart.js is vendored locally in
`webapp/lib/`, so the whole thing works with no internet connection.

## Demo flow for the review

1. **Savings tab** - set income + savings goal
2. **Budgets tab** - set a Food category budget
3. **Family & Nutrition tab** - add 2-3 family members with different ages/BMIs
4. **AI Insights tab** - show the per-member breakdown, the essentials-vs-budget
   chart, and the recommendation list - this is the single view that ties
   the whole project together and is worth leading the demo with
5. **Add Expense tab** - add an expense that crosses 80% of the Food budget,
   show the alert
6. **Dashboard** - show the spending-by-category chart

## Why the website is built the way it is

The backend is plain Java using `com.sun.net.httpserver` (built into the JDK
since Java 6, with `SimpleFileServer` added in JDK 18) - no Spring, no
Tomcat, no Maven. It compiles and runs with the exact same `javac`/`java`
workflow as the console and GUI versions. The frontend is plain HTML/CSS/JS
with Chart.js vendored locally rather than loaded from a CDN, so the whole
demo runs with zero internet dependency - important for a live review where
Wi-Fi isn't guaranteed.
