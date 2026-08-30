package com.hospital.dao;

import com.hospital.model.LabTechnician;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Lab Technician module via JDBC & Neon PostgreSQL.
 */
public class LabTechnicianDAO {

    public boolean insertTechnician(LabTechnician tech) {
        String sql = "INSERT INTO lab_technicians (technician_id, name, phone, age, gender, email, password, qualification, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (email) DO NOTHING";
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tech.getTechnicianId());
            ps.setString(2, tech.getName());
            ps.setString(3, tech.getPhone());
            ps.setInt(4, tech.getAge());
            ps.setString(5, tech.getGender());
            ps.setString(6, tech.getEmail());
            ps.setString(7, tech.getPassword());
            ps.setString(8, tech.getQualification());
            ps.setString(9, tech.getCreatedAt() != null ? tech.getCreatedAt() : nowStr);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Keep users table in sync for centralized authentication
                syncToUsersTable(tech);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting lab technician: " + e.getMessage());
            DBConnection.logSQLException(e);
        }
        return false;
    }

    public LabTechnician getTechnicianByEmail(String email) {
        String sql = "SELECT * FROM lab_technicians WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTechnician(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching lab technician by email: " + e.getMessage());
        }
        return null;
    }

    public List<LabTechnician> getAllTechnicians() {
        List<LabTechnician> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_technicians ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTechnician(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all lab technicians: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteTechnician(String technicianId) {
        String sql = "DELETE FROM lab_technicians WHERE technician_id = ? OR LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, technicianId);
            ps.setString(2, technicianId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting lab technician: " + e.getMessage());
            return false;
        }
    }

    private LabTechnician mapResultSetToTechnician(ResultSet rs) throws SQLException {
        return new LabTechnician(
            rs.getString("technician_id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("qualification"),
            rs.getString("created_at")
        );
    }

    private void syncToUsersTable(LabTechnician tech) {
        String sql = "INSERT INTO users (email, password, role, name, phone) VALUES (?, ?, 'technician', ?, ?) ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password, name = EXCLUDED.name, phone = EXCLUDED.phone";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tech.getEmail());
            ps.setString(2, tech.getPassword());
            ps.setString(3, tech.getName());
            ps.setString(4, tech.getPhone());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}
