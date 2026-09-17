import dao.CategoryDAO;
import menu.Menu;
import model.Budget;
import model.Category;
import model.Expense;
import model.SavingsSummary;
import service.BudgetService;
import service.ExpenseService;
import service.SavingsService;
import util.DBConnection;
import util.Validation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Personal Expense Tracker - Console Application
 * Main only wires things together: console I/O lives in Menu, business rules
 * live in service/, raw persistence lives in dao/ (built on util.DBConnection).
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Menu menu = new Menu(scanner);

    private static final ExpenseService expenseService = new ExpenseService();
    private static final BudgetService budgetService = new BudgetService();
    private static final SavingsService savingsService = new SavingsService();
    private static final CategoryDAO categoryDAO = new CategoryDAO();

    public static void main(String[] args) {
        menu.printBanner();

        boolean running = true;
        while (running) {
            menu.printMenu();
            String choice = menu.readChoice();
            try {
                switch (choice) {
                    case "1": addExpense(); break;
                    case "2": viewAllExpenses(); break;
                    case "3": viewThisMonthExpenses(); break;
                    case "4": deleteExpense(); break;
                    case "5": setBudget(); break;
                    case "6": viewBudgetStatus(); break;
                    case "7": setIncomeAndGoal(); break;
                    case "8": viewSavingsSummary(); break;
                    case "9": addCategory(); break;
                    case "10": backupExpenses(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice, try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Goodbye! Keep saving.");
        DBConnection.close();
    }

    private static void addExpense() throws SQLException {
        List<Category> categories = categoryDAO.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories found. Add one first (option 9).");
            return;
        }
        System.out.println("\nCategories:");
        categories.forEach(System.out::println);
        int categoryId = Integer.parseInt(menu.prompt("Enter category ID: "));

        double amount = Validation.parseAmount(menu.prompt("Enter amount (Rs.): "));
        String description = menu.prompt("Enter description: ");
        LocalDate date = Validation.parseDateOrToday(
                menu.prompt("Enter date (YYYY-MM-DD) or press Enter for today: "));

        expenseService.addExpense(categoryId, amount, description, date);
        System.out.println("Expense added successfully.");

        String alert = budgetService.checkBudgetAlert(categoryId);
        if (alert != null) {
            System.out.println(">> " + alert);
        }
    }

    private static void viewAllExpenses() throws SQLException {
        printExpenseList(expenseService.getAllExpenses());
    }

    private static void viewThisMonthExpenses() throws SQLException {
        printExpenseList(expenseService.getExpensesForCurrentMonth());
    }

    private static void printExpenseList(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }
        System.out.println("\nID   Category     Amount       Description          Date");
        System.out.println("---------------------------------------------------------------");
        double total = 0;
        for (Expense e : expenses) {
            System.out.println(e);
            total += e.getAmount();
        }
        System.out.printf("%nTotal: Rs.%.2f%n", total);
    }

    private static void deleteExpense() throws SQLException {
        int id = Integer.parseInt(menu.prompt("Enter expense ID to delete: "));
        boolean deleted = expenseService.deleteExpense(id);
        System.out.println(deleted ? "Deleted." : "No expense found with that ID.");
    }

    private static void setBudget() throws SQLException {
        List<Category> categories = categoryDAO.getAllCategories();
        System.out.println("\nCategories:");
        categories.forEach(System.out::println);
        int categoryId = Integer.parseInt(menu.prompt("Enter category ID: "));
        double limit = Validation.parseAmount(menu.prompt("Enter monthly budget limit (Rs.): "));
        budgetService.setBudget(categoryId, limit);
        System.out.println("Budget set successfully.");
    }

    private static void viewBudgetStatus() throws SQLException {
        List<Budget> budgets = budgetService.getAllBudgetsWithProgress();
        if (budgets.isEmpty()) {
            System.out.println("No budgets set yet.");
            return;
        }
        System.out.println("\nCategory       Spent        Limit        % Used   Status");
        System.out.println("-------------------------------------------------------------");
        for (Budget b : budgets) {
            String status = budgetService.getStatusLabel(b);
            System.out.printf("%-14s Rs.%-10.2f Rs.%-10.2f %-8.0f%% %s%n",
                    b.getCategoryName(), b.getSpentSoFar(), b.getMonthlyLimit(), b.getPercentUsed(), status);
        }
    }

    private static void setIncomeAndGoal() throws SQLException {
        double income = Validation.parseAmount(menu.prompt("Enter your monthly income (Rs.): "));
        double goalPercent = Validation.parsePercent(
                menu.prompt("Enter your savings goal (% of income, e.g. 20): "), 0, 100);
        savingsService.updateIncomeAndGoal(income, goalPercent);
        System.out.println("Income and savings goal updated.");
    }

    private static void viewSavingsSummary() throws SQLException {
        SavingsSummary summary = savingsService.getSavingsSummary();

        System.out.println("\n============ SAVINGS SUMMARY (This Month) ============");
        System.out.printf("Monthly Income:      Rs.%.2f%n", summary.getIncome());
        System.out.printf("Total Spent:         Rs.%.2f%n", summary.getSpent());
        System.out.printf("Actual Savings:      Rs.%.2f (%.1f%% of income)%n",
                summary.getSavings(), summary.getSavingsPercent());
        System.out.printf("Savings Goal:        Rs.%.2f (%.1f%% of income)%n",
                summary.getGoalAmount(), summary.getGoalPercent());
        System.out.println("Status: " + summary.getStatusMessage());
        System.out.println("========================================================");
    }

    private static void addCategory() throws SQLException {
        String name = menu.prompt("Enter new category name: ");
        Validation.requireNotBlank(name, "Category name");
        categoryDAO.addCategory(name);
        System.out.println("Category added.");
    }

    private static void backupExpenses() throws SQLException {
        expenseService.backupAllExpenses();
        System.out.println("Backup written to data/backup.txt");
    }
}
