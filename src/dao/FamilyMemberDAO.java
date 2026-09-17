package dao;

import model.FamilyMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FamilyMemberDAO extends BaseDAO {

    @Override
    public String getEntityName() {
        return "Family Member";
    }

    public void addMember(FamilyMember m) throws SQLException {
        String sql = "INSERT INTO family_members (name, age, gender, height_cm, weight_kg, activity_level) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setInt(2, m.getAge());
            ps.setString(3, m.getGender());
            ps.setDouble(4, m.getHeightCm());
            ps.setDouble(5, m.getWeightKg());
            ps.setString(6, m.getActivityLevel());
            ps.executeUpdate();
        }
    }

    public List<FamilyMember> getAllMembers() throws SQLException {
        List<FamilyMember> members = new ArrayList<>();
        String sql = "SELECT member_id, name, age, gender, height_cm, weight_kg, activity_level " +
                "FROM family_members ORDER BY member_id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FamilyMember m = new FamilyMember();
                m.setMemberId(rs.getInt("member_id"));
                m.setName(rs.getString("name"));
                m.setAge(rs.getInt("age"));
                m.setGender(rs.getString("gender"));
                m.setHeightCm(rs.getDouble("height_cm"));
                m.setWeightKg(rs.getDouble("weight_kg"));
                m.setActivityLevel(rs.getString("activity_level"));
                members.add(m);
            }
        }
        return members;
    }

    public boolean deleteMember(int memberId) throws SQLException {
        String sql = "DELETE FROM family_members WHERE member_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            return ps.executeUpdate() > 0;
        }
    }
}
