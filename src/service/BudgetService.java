package service;

import dao.BudgetDAO;
import dao.ExpenseDAO;
import model.Budget;
import util.Validation;

import java.sql.SQLException;
import java.util.List;

/** Business rules around budget limits: how close is the user to overspending, and when to warn them. */
public class BudgetService {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    private static final double WARNING_THRESHOLD_PERCENT = 80.0;

    public void setBudget(int categoryId, double monthlyLimit) throws SQLException {
        Validation.requirePositive(monthlyLimit, "Monthly limit");
        budgetDAO.setBudget(categoryId, monthlyLimit);
    }

    public List<Budget> getAllBudgetsWithProgress() throws SQLException {
        return budgetDAO.getAllBudgetsWithProgress();
    }

    public String checkBudgetAlert(int categoryId) throws SQLException {
        Double limit = budgetDAO.getMonthlyLimitForCategory(categoryId);
        if (limit == null) {
            return null;
        }
        double spent = expenseDAO.getTotalSpentThisMonthForCategory(categoryId);
        double percent = (spent / limit) * 100.0;

        if (spent > limit) {
            return String.format("OVER BUDGET! You've now spent Rs.%.2f of your Rs.%.2f limit (%.0f%%).",
                    spent, limit, percent);
        } else if (percent >= WARNING_THRESHOLD_PERCENT) {
            return String.format("WARNING: You've used %.0f%% of this category's budget (Rs.%.2f / Rs.%.2f).",
                    percent, spent, limit);
        }
        return null;
    }

    public String getStatusLabel(Budget budget) {
        if (budget.getPercentUsed() >= 100) return "OVER BUDGET";
        if (budget.getPercentUsed() >= WARNING_THRESHOLD_PERCENT) return "WARNING";
        return "OK";
    }
}
