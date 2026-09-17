package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import dao.CategoryDAO;
import model.Budget;
import model.Category;
import model.EssentialsPlan;
import model.Expense;
import model.FamilyMember;
import model.MemberNutritionResult;
import model.SavingsSummary;
import service.BudgetService;
import service.ExpenseService;
import service.NutritionService;
import service.RecommendationEngine;
import service.SavingsService;
import util.Json;
import util.Validation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight REST API + static file server for the web frontend.
 * Uses only the built-in JDK HTTP server (com.sun.net.httpserver) - no external
 * framework, no Maven, no Tomcat - so it compiles and runs exactly like the rest
 * of this project.
 */
public class ApiServer {

    private static final ExpenseService expenseService = new ExpenseService();
    private static final BudgetService budgetService = new BudgetService();
    private static final SavingsService savingsService = new SavingsService();
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    private static final NutritionService nutritionService = new NutritionService();
    private static final RecommendationEngine recommendationEngine = new RecommendationEngine();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Port must be a whole number, for example: 8080", e);
            }
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/expenses", ApiServer::handleExpenses);
        server.createContext("/api/categories", ApiServer::handleCategories);
        server.createContext("/api/budgets", ApiServer::handleBudgets);
        server.createContext("/api/savings", ApiServer::handleSavings);
        server.createContext("/api/family", ApiServer::handleFamily);
        server.createContext("/api/insights", ApiServer::handleInsights);

        // SimpleFileServer requires an absolute path on current JDK releases.
        server.createContext("/", SimpleFileServer.createFileHandler(Path.of("webapp").toAbsolutePath()));

        server.setExecutor(null);
        server.start();

        System.out.println("========================================");
        System.out.println("  Expense Tracker web server running at:");
        System.out.println("  http://localhost:" + port);
        System.out.println("========================================");
    }

    // ---------- request/response helpers ----------

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private static Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        return parseQuery(body);
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, "{\"error\":" + Json.str(message) + "}");
    }

    // ---------- /api/expenses ----------

    private static void handleExpenses(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                List<Expense> expenses = expenseService.getAllExpenses();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < expenses.size(); i++) {
                    Expense e = expenses.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                            .append("\"id\":").append(e.getExpenseId()).append(",")
                            .append("\"category\":").append(Json.str(e.getCategoryName())).append(",")
                            .append("\"amount\":").append(e.getAmount()).append(",")
                            .append("\"description\":").append(Json.str(e.getDescription())).append(",")
                            .append("\"date\":").append(Json.str(e.getDate().toString()))
                            .append("}");
                }
                json.append("]");
                sendJson(exchange, 200, json.toString());
            } else if ("POST".equals(method)) {
                Map<String, String> form = parseFormBody(exchange);
                int categoryId = requiredPositiveInt(form, "categoryId", "Category");
                double amount = requiredPositiveDouble(form, "amount", "Amount");
                String description = form.getOrDefault("description", "");
                String dateInput = form.get("date");
                LocalDate date = (dateInput == null || dateInput.isEmpty()) ? LocalDate.now() : LocalDate.parse(dateInput);
                expenseService.addExpense(categoryId, amount, description, date);
                String alert = budgetService.checkBudgetAlert(categoryId);
                String alertJson = alert == null ? "null" : Json.str(alert);
                sendJson(exchange, 200, "{\"success\":true,\"alert\":" + alertJson + "}");
            } else if ("DELETE".equals(method)) {
                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                int id = Integer.parseInt(params.get("id"));
                boolean deleted = expenseService.deleteExpense(id);
                sendJson(exchange, 200, "{\"success\":" + deleted + "}");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Bad request: " + e.getMessage());
        }
    }

    // ---------- /api/categories ----------

    private static void handleCategories(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                List<Category> categories = categoryDAO.getAllCategories();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < categories.size(); i++) {
                    Category c = categories.get(i);
                    if (i > 0) json.append(",");
                    json.append("{\"id\":").append(c.getCategoryId())
                            .append(",\"name\":").append(Json.str(c.getCategoryName())).append("}");
                }
                json.append("]");
                sendJson(exchange, 200, json.toString());
            } else if ("POST".equals(method)) {
                Map<String, String> form = parseFormBody(exchange);
                String name = form.get("name");
                Validation.requireNotBlank(name, "Category name");
                categoryDAO.addCategory(name.trim());
                sendJson(exchange, 200, "{\"success\":true}");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Bad request: " + e.getMessage());
        }
    }

    // ---------- /api/budgets ----------

    private static void handleBudgets(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                List<Budget> budgets = budgetService.getAllBudgetsWithProgress();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < budgets.size(); i++) {
                    Budget b = budgets.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                            .append("\"category\":").append(Json.str(b.getCategoryName())).append(",")
                            .append("\"spent\":").append(b.getSpentSoFar()).append(",")
                            .append("\"limit\":").append(b.getMonthlyLimit()).append(",")
                            .append("\"percentUsed\":").append(b.getPercentUsed()).append(",")
                            .append("\"status\":").append(Json.str(budgetService.getStatusLabel(b)))
                            .append("}");
                }
                json.append("]");
                sendJson(exchange, 200, json.toString());
            } else if ("POST".equals(method)) {
                Map<String, String> form = parseFormBody(exchange);
                int categoryId = requiredPositiveInt(form, "categoryId", "Category");
                double limit = requiredPositiveDouble(form, "limit", "Monthly limit");
                budgetService.setBudget(categoryId, limit);
                sendJson(exchange, 200, "{\"success\":true}");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Bad request: " + e.getMessage());
        }
    }

    // ---------- /api/savings ----------

    private static void handleSavings(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                SavingsSummary s = savingsService.getSavingsSummary();
                String json = "{"
                        + "\"income\":" + s.getIncome() + ","
                        + "\"spent\":" + s.getSpent() + ","
                        + "\"savings\":" + s.getSavings() + ","
                        + "\"savingsPercent\":" + s.getSavingsPercent() + ","
                        + "\"goalAmount\":" + s.getGoalAmount() + ","
                        + "\"goalPercent\":" + s.getGoalPercent() + ","
                        + "\"onTrack\":" + s.isOnTrack() + ","
                        + "\"statusMessage\":" + Json.str(s.getStatusMessage())
                        + "}";
                sendJson(exchange, 200, json);
            } else if ("POST".equals(method)) {
                Map<String, String> form = parseFormBody(exchange);
                double income = requiredPositiveDouble(form, "income", "Monthly income");
                double goalPercent = requiredDouble(form, "goalPercent", "Savings goal percent");
                savingsService.updateIncomeAndGoal(income, goalPercent);
                sendJson(exchange, 200, "{\"success\":true}");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Bad request: " + e.getMessage());
        }
    }

    // ---------- /api/family ----------

    private static void handleFamily(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                List<FamilyMember> members = nutritionService.getAllMembers();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < members.size(); i++) {
                    FamilyMember m = members.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                            .append("\"id\":").append(m.getMemberId()).append(",")
                            .append("\"name\":").append(Json.str(m.getName())).append(",")
                            .append("\"age\":").append(m.getAge()).append(",")
                            .append("\"gender\":").append(Json.str(m.getGender())).append(",")
                            .append("\"heightCm\":").append(m.getHeightCm()).append(",")
                            .append("\"weightKg\":").append(m.getWeightKg()).append(",")
                            .append("\"activityLevel\":").append(Json.str(m.getActivityLevel())).append(",")
                            .append("\"bmi\":").append(m.getBmi()).append(",")
                            .append("\"bmiCategory\":").append(Json.str(m.getBmiCategory()))
                            .append("}");
                }
                json.append("]");
                sendJson(exchange, 200, json.toString());
            } else if ("POST".equals(method)) {
                Map<String, String> form = parseFormBody(exchange);
                String name = form.get("name");
                int age = requiredPositiveInt(form, "age", "Age");
                String gender = form.get("gender");
                double heightCm = requiredPositiveDouble(form, "heightCm", "Height");
                double weightKg = requiredPositiveDouble(form, "weightKg", "Weight");
                String activityLevel = form.getOrDefault("activityLevel", "light");
                nutritionService.addFamilyMember(name, age, gender, heightCm, weightKg, activityLevel);
                sendJson(exchange, 200, "{\"success\":true}");
            } else if ("DELETE".equals(method)) {
                Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
                int id = Integer.parseInt(params.get("id"));
                boolean deleted = nutritionService.deleteMember(id);
                sendJson(exchange, 200, "{\"success\":" + deleted + "}");
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Bad request: " + e.getMessage());
        }
    }

    // ---------- /api/insights (essentials plan + AI recommendations, combined) ----------

    private static void handleInsights(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            EssentialsPlan plan = nutritionService.generateHouseholdPlan();
            SavingsSummary savings = savingsService.getSavingsSummary();
            List<Budget> budgets = budgetService.getAllBudgetsWithProgress();
            List<String> recommendations = recommendationEngine.analyze(plan, savings, budgets);

            StringBuilder membersJson = new StringBuilder("[");
            for (int i = 0; i < plan.getMemberResults().size(); i++) {
                MemberNutritionResult r = plan.getMemberResults().get(i);
                if (i > 0) membersJson.append(",");
                membersJson.append("{")
                        .append("\"name\":").append(Json.str(r.getMemberName())).append(",")
                        .append("\"bmi\":").append(r.getBmi()).append(",")
                        .append("\"bmiCategory\":").append(Json.str(r.getBmiCategory())).append(",")
                        .append("\"tdee\":").append(r.getTdee()).append(",")
                        .append("\"riceKgPerMonth\":").append(r.getRiceKgPerMonth()).append(",")
                        .append("\"dalKgPerMonth\":").append(r.getDalKgPerMonth()).append(",")
                        .append("\"oilLitersPerMonth\":").append(r.getOilLitersPerMonth())
                        .append("}");
            }
            membersJson.append("]");

            StringBuilder recJson = new StringBuilder("[");
            for (int i = 0; i < recommendations.size(); i++) {
                if (i > 0) recJson.append(",");
                recJson.append(Json.str(recommendations.get(i)));
            }
            recJson.append("]");

            String json = "{"
                    + "\"members\":" + membersJson + ","
                    + "\"totalRiceKg\":" + plan.getTotalRiceKg() + ","
                    + "\"totalDalKg\":" + plan.getTotalDalKg() + ","
                    + "\"totalOilLiters\":" + plan.getTotalOilLiters() + ","
                    + "\"estimatedMonthlyCost\":" + plan.getEstimatedMonthlyCost() + ","
                    + "\"recommendations\":" + recJson
                    + "}";
            sendJson(exchange, 200, json);
        } catch (SQLException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 400, "Bad request: " + e.getMessage());
        }
    }

    private static int requiredPositiveInt(Map<String, String> form, String key, String label) {
        try {
            int value = Integer.parseInt(form.getOrDefault(key, "").trim());
            if (value <= 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a positive whole number.");
        }
    }

    private static double requiredPositiveDouble(Map<String, String> form, String key, String label) {
        double value = requiredDouble(form, key, label);
        Validation.requirePositive(value, label);
        return value;
    }

    private static double requiredDouble(Map<String, String> form, String key, String label) {
        try {
            return Double.parseDouble(form.getOrDefault(key, "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }
}
