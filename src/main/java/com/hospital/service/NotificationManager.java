package com.hospital.service;

import com.hospital.dao.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * NotificationManager handles system notifications and unread badge alerts.
 */
public class NotificationManager {

    public List<Map<String, String>> getAllNotifications() {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM system_notifications ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("id", rs.getString("id"));
                m.put("title", rs.getString("title"));
                m.put("message", rs.getString("message"));
                m.put("type", rs.getString("type"));
                m.put("timestamp", rs.getString("timestamp"));
                m.put("isRead", String.valueOf(rs.getInt("is_read")));
                list.add(m);
            }
        } catch (Exception ignored) {}
        return list;
    }

    public int getUnreadCount() {
        int count = 0;
        for (Map<String, String> n : getAllNotifications()) {
            if ("0".equals(n.get("isRead"))) count++;
        }
        return count;
    }

    public boolean addNotification(String title, String message, String type) {
        String id = "NOTIF-" + System.currentTimeMillis();
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "INSERT INTO system_notifications (id, title, message, type, timestamp, is_read) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.setString(5, nowStr);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }


    public boolean addPatientNotification(String patientId, String title, String message, String type) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return false;
        }
        String id = "NTF-" + System.currentTimeMillis();
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "INSERT INTO notifications (id, patient_id, title, message, type, timestamp, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, 0, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, patientId.trim());
            ps.setString(3, title);
            ps.setString(4, message);
            ps.setString(5, type);
            ps.setString(6, nowStr);
            ps.setString(7, nowStr);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[NotificationManager] ERROR inserting patient notification: " + e.getMessage());
        }

        // Also broadcast to system notifications for admin oversight
        addNotification(title + " [Patient: " + patientId + "]", message, type);
        return true;
    }

    public List<Map<String, String>> getPatientNotifications(String patientId) {
        List<Map<String, String>> list = new ArrayList<>();
        if (patientId == null || patientId.trim().isEmpty()) {
            return list;
        }
        String sql = "SELECT * FROM notifications WHERE patient_id = ? ORDER BY created_at DESC, timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("id", rs.getString("id"));
                    m.put("patientId", rs.getString("patient_id"));
                    m.put("title", rs.getString("title"));
                    m.put("message", rs.getString("message"));
                    m.put("type", rs.getString("type"));
                    m.put("timestamp", rs.getString("timestamp"));
                    m.put("isRead", String.valueOf(rs.getInt("is_read")));
                    list.add(m);
                }
            }
        } catch (Exception e) {
            System.err.println("[NotificationManager] ERROR reading patient notifications: " + e.getMessage());
        }
        return list;
    }
}
