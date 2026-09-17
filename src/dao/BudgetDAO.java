package dao;

import model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO extends BaseDAO {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    @Override
    public String getEntityName() {
        return "Budget";
    }

    public void setBudget(int categoryId, double monthlyLimit) throws SQLException {
        String sql = "INSERT INTO budgets (category_id, monthly_limit) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE monthly_limit = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setDouble(2, monthlyLimit);
            ps.setDouble(3, monthlyLimit);
            ps.executeUpdate();
        }
    }

    public List<Budget> getAllBudgetsWithProgress() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT b.budget_id, b.category_id, c.category_name, b.monthly_limit " +
                "FROM budgets b JOIN categories c ON b.category_id = c.category_id " +
                "ORDER BY c.category_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Budget b = new Budget();
                b.setBudgetId(rs.getInt("budget_id"));
                b.setCategoryId(rs.getInt("category_id"));
                b.setCategoryName(rs.getString("category_name"));
                b.setMonthlyLimit(rs.getDouble("monthly_limit"));
                b.setSpentSoFar(expenseDAO.getTotalSpentThisMonthForCategory(b.getCategoryId()));
                budgets.add(b);
            }
        }
        return budgets;
    }

    public Double getMonthlyLimitForCategory(int categoryId) throws SQLException {
        String sql = "SELECT monthly_limit FROM budgets WHERE category_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("monthly_limit");
                }
            }
        }
        return null;
    }
}
