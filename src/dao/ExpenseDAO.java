package dao;

import model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO extends BaseDAO {

    @Override
    public String getEntityName() {
        return "Expense";
    }

    public void addExpense(Expense e) throws SQLException {
        String sql = "INSERT INTO expenses (category_id, amount, description, expense_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getCategoryId());
            ps.setDouble(2, e.getAmount());
            ps.setString(3, e.getDescription());
            ps.setDate(4, Date.valueOf(e.getDate()));
            ps.executeUpdate();
        }
    }

    public List<Expense> getAllExpenses() throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.expense_id, e.category_id, c.category_name, e.amount, e.description, e.expense_date " +
                "FROM expenses e JOIN categories c ON e.category_id = c.category_id " +
                "ORDER BY e.expense_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Expense> getExpensesForCurrentMonth() throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.expense_id, e.category_id, c.category_name, e.amount, e.description, e.expense_date " +
                "FROM expenses e JOIN categories c ON e.category_id = c.category_id " +
                "WHERE YEAR(e.expense_date) = YEAR(CURDATE()) AND MONTH(e.expense_date) = MONTH(CURDATE()) " +
                "ORDER BY e.expense_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public double getTotalSpentThisMonthForCategory(int categoryId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM expenses " +
                "WHERE category_id = ? AND YEAR(expense_date) = YEAR(CURDATE()) AND MONTH(expense_date) = MONTH(CURDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        }
        return 0;
    }

    public double getTotalSpentThisMonth() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) AS total FROM expenses " +
                "WHERE YEAR(expense_date) = YEAR(CURDATE()) AND MONTH(expense_date) = MONTH(CURDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("total");
        }
        return 0;
    }

    public boolean deleteExpense(int expenseId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE expense_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            return ps.executeUpdate() > 0;
        }
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setExpenseId(rs.getInt("expense_id"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setCategoryName(rs.getString("category_name"));
        e.setAmount(rs.getDouble("amount"));
        e.setDescription(rs.getString("description"));
        e.setDate(rs.getDate("expense_date").toLocalDate());
        return e;
    }
}
