package com.hospital.dao;

import com.hospital.model.ContactQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for visitor Contact Inquiries & Support Desk persistence.
 */
public class ContactDAO {

    public boolean createContactQuery(ContactQuery q) {
        String sql = "INSERT INTO contact_queries (query_id, name, email, phone, subject, message, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getQueryId());
            ps.setString(2, q.getName());
            ps.setString(3, q.getEmail());
            ps.setString(4, q.getPhone());
            ps.setString(5, q.getSubject());
            ps.setString(6, q.getMessage());
            ps.setString(7, q.getStatus() != null ? q.getStatus() : "Open");
            ps.setString(8, q.getCreatedAt());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving contact query: " + e.getMessage());
            return false;
        }
    }

    public List<ContactQuery> getAllContactQueries() {
        List<ContactQuery> list = new ArrayList<>();
        String sql = "SELECT * FROM contact_queries ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ContactQuery(
                    rs.getString("query_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("subject"),
                    rs.getString("message"),
                    rs.getString("status"),
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching contact queries: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(String queryId, String status) {
        String sql = "UPDATE contact_queries SET status = ? WHERE query_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, queryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating query status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteQuery(String queryId) {
        String sql = "DELETE FROM contact_queries WHERE query_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting contact query: " + e.getMessage());
            return false;
        }
    }
}
