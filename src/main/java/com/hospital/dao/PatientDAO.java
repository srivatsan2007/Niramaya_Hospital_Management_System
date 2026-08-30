package com.hospital.dao;

import com.hospital.model.Patient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for Patient entity using PreparedStatements.
 */
public class PatientDAO {

    public boolean createPatient(Patient p) {
        String sql = "INSERT INTO patients (patient_id, name, email, phone, age, gender, blood_group, registration_time, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPatientId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getPhone());
            ps.setInt(5, p.getAge());
            ps.setString(6, p.getGender());
            ps.setString(7, p.getBloodGroup());
            ps.setString(8, nowStr);
            ps.setString(9, nowStr);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving patient: " + e.getMessage());
            return false;
        }
    }

    public boolean updateLastLogin(String patientIdOrEmail) {
        String sql = "UPDATE patients SET last_login = ? WHERE patient_id = ? OR email = ?";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, patientIdOrEmail);
            ps.setString(3, patientIdOrEmail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient last login: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfileTimestamp(String patientIdOrEmail) {
        String sql = "UPDATE patients SET profile_updated_time = ?, updated_at = ? WHERE patient_id = ? OR email = ?";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, nowStr);
            ps.setString(3, patientIdOrEmail);
            ps.setString(4, patientIdOrEmail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient profile timestamp: " + e.getMessage());
            return false;
        }
    }

    public Patient getPatientByIdOrEmail(String identifier) {
        String sql = "SELECT * FROM patients WHERE patient_id = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getString("patient_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("blood_group")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient: " + e.getMessage());
        }
        return null;
    }

    public java.util.List<Patient> getAllPatients() {
        java.util.List<Patient> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Patient(
                    rs.getString("patient_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("blood_group")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patients: " + e.getMessage());
        }
        if (list.isEmpty()) {
            list.add(new Patient("PT100842", "Rekha Prasad", "patient@niramaya.health", "+91 98765 43210", 28, "Female", "O+"));
            list.add(new Patient("PT394821", "Aniket Sharma", "aniket@niramaya.health", "+91 98123 45678", 29, "Male", "B+"));
        }
        return list;
    }

    public java.util.List<Patient> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) return getAllPatients();
        java.util.List<Patient> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM patients WHERE LOWER(name) LIKE ? OR LOWER(patient_id) LIKE ? OR LOWER(email) LIKE ? OR phone LIKE ?";
        String pattern = "%" + query.toLowerCase() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Patient(
                        rs.getString("patient_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("blood_group")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching patients: " + e.getMessage());
        }
        return list;
    }

    public boolean savePatient(Patient p) {
        return createPatient(p);
    }

    public boolean deletePatient(String patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            ps.setString(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
