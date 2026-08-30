package com.hospital.dao;

import com.hospital.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for system-wide Activity Logs.
 * Records all user and module activities automatically using JDBC PreparedStatement.
 */
public class ActivityLogDAO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean logActivity(String userId, String userName, String role, String module, String action, String status, String ipAddress) {
        String logId = "LOG-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);
        String nowStr = LocalDateTime.now().format(FORMATTER);
        Timestamp nowTs = Timestamp.valueOf(LocalDateTime.now());

        String sql = "INSERT INTO activity_logs (log_id, user_id, user_name, role, module, action, status, ip_address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, logId);
            ps.setString(2, userId != null ? userId : "SYSTEM");
            ps.setString(3, userName != null ? userName : "User");
            ps.setString(4, role != null ? role : "System");
            ps.setString(5, module != null ? module : "General");
            ps.setString(6, action != null ? action : "Action Performed");
            ps.setString(7, status != null ? status : "Success");
            ps.setString(8, ipAddress != null ? ipAddress : "127.0.0.1");
            ps.setString(9, nowStr);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getLogsByUser(String userId) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user activity logs: " + e.getMessage());
        }
        return list;
    }

    public List<ActivityLog> getLogsByRole(String role) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE role = ? ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching role activity logs: " + e.getMessage());
        }
        return list;
    }

    public List<ActivityLog> getLogsByModule(String module) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs WHERE module = ? ORDER BY created_at DESC LIMIT 100";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, module);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching module activity logs: " + e.getMessage());
        }
        return list;
    }

    public List<ActivityLog> getAllLogs(int limit) {
        List<ActivityLog> list = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all activity logs: " + e.getMessage());
        }
        return list;
    }

    private ActivityLog mapResultSetToLog(ResultSet rs) throws SQLException {
        return new ActivityLog(
            rs.getString("log_id"),
            rs.getString("user_id"),
            rs.getString("user_name"),
            rs.getString("role"),
            rs.getString("module"),
            rs.getString("action"),
            rs.getString("status"),
            rs.getString("ip_address"),
            rs.getString("created_at")
        );
    }
}
