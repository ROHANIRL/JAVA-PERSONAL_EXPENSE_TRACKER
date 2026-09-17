package dao;

import java.sql.*;

public class UserProfileDAO extends BaseDAO {

    @Override
    public String getEntityName() {
        return "User Profile";
    }

    public double getMonthlyIncome() throws SQLException {
        String sql = "SELECT monthly_income FROM user_profile LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("monthly_income");
        }
        return 0;
    }

    public double getSavingsGoalPercent() throws SQLException {
        String sql = "SELECT savings_goal_percent FROM user_profile LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble("savings_goal_percent");
        }
        return 20.0;
    }

    public void updateIncomeAndGoal(double income, double goalPercent) throws SQLException {
        String sql = "INSERT INTO user_profile (profile_id, monthly_income, savings_goal_percent) VALUES (1, ?, ?) " +
                "ON DUPLICATE KEY UPDATE monthly_income = VALUES(monthly_income), " +
                "savings_goal_percent = VALUES(savings_goal_percent)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, income);
            ps.setDouble(2, goalPercent);
            ps.executeUpdate();
        }
    }
}
