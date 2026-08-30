package com.hospital.dao;

import com.hospital.model.PharmacyStaff;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Pharmacy Staff module via JDBC & Neon PostgreSQL.
 */
public class PharmacyStaffDAO {

    public boolean insertStaff(PharmacyStaff staff) {
        String sql = "INSERT INTO pharmacy_staff (staff_id, name, phone, age, gender, email, password, qualification, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (email) DO NOTHING";
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getStaffId());
            ps.setString(2, staff.getName());
            ps.setString(3, staff.getPhone());
            ps.setInt(4, staff.getAge());
            ps.setString(5, staff.getGender());
            ps.setString(6, staff.getEmail());
            ps.setString(7, staff.getPassword());
            ps.setString(8, staff.getQualification());
            ps.setString(9, staff.getCreatedAt() != null ? staff.getCreatedAt() : nowStr);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Keep users table in sync for centralized authentication
                syncToUsersTable(staff);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting pharmacy staff: " + e.getMessage());
            DBConnection.logSQLException(e);
        }
        return false;
    }

    public PharmacyStaff getStaffByEmail(String email) {
        String sql = "SELECT * FROM pharmacy_staff WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStaff(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pharmacy staff by email: " + e.getMessage());
        }
        return null;
    }

    public List<PharmacyStaff> getAllStaff() {
        List<PharmacyStaff> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacy_staff ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToStaff(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all pharmacy staff: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteStaff(String staffId) {
        String sql = "DELETE FROM pharmacy_staff WHERE staff_id = ? OR LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staffId);
            ps.setString(2, staffId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting pharmacy staff: " + e.getMessage());
            return false;
        }
    }

    private PharmacyStaff mapResultSetToStaff(ResultSet rs) throws SQLException {
        return new PharmacyStaff(
            rs.getString("staff_id"),
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

    private void syncToUsersTable(PharmacyStaff staff) {
        String sql = "INSERT INTO users (email, password, role, name, phone) VALUES (?, ?, 'pharmacy', ?, ?) ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password, name = EXCLUDED.name, phone = EXCLUDED.phone";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staff.getEmail());
            ps.setString(2, staff.getPassword());
            ps.setString(3, staff.getName());
            ps.setString(4, staff.getPhone());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}
