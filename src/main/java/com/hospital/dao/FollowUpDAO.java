package com.hospital.dao;

import com.hospital.model.PostDischargeFollowup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Post-Discharge Follow-up care tracking and management.
 */
public class FollowUpDAO {

    private static volatile boolean tableInitialized = false;

    private static void ensureTable(Connection conn) {
        if (tableInitialized) return;
        synchronized (FollowUpDAO.class) {
            if (tableInitialized) return;
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS post_discharge_followups (" +
                        "followup_id VARCHAR(50) PRIMARY KEY, " +
                        "admission_id VARCHAR(50) NOT NULL, " +
                        "patient_id VARCHAR(50) NOT NULL, " +
                        "patient_name VARCHAR(100) NOT NULL, " +
                        "doctor_id VARCHAR(50), " +
                        "doctor_name VARCHAR(100), " +
                        "discharge_date VARCHAR(50), " +
                        "followup_date VARCHAR(50) NOT NULL, " +
                        "discharge_instructions TEXT, " +
                        "clinical_notes TEXT, " +
                        "status VARCHAR(50) DEFAULT 'Pending', " +
                        "outcome_notes TEXT, " +
                        "contacted_by VARCHAR(100), " +
                        "contacted_at VARCHAR(50), " +
                        "followup_appointment_id VARCHAR(50), " +
                        "created_at VARCHAR(50)" +
                        ");");
                tableInitialized = true;
            } catch (Exception e) {
                System.err.println("Note: ensureTable in FollowUpDAO: " + e.getMessage());
            }
        }
    }

    public boolean createFollowUp(PostDischargeFollowup f) {
        if (f == null || f.getAdmissionId() == null || f.getAdmissionId().trim().isEmpty()) {
            return false;
        }

        // Prevent duplicate follow-ups for the same admission
        String checkSql = "SELECT COUNT(*) FROM post_discharge_followups WHERE admission_id = ?";
        String insertSql = "INSERT INTO post_discharge_followups (followup_id, admission_id, patient_id, patient_name, doctor_id, doctor_name, discharge_date, followup_date, discharge_instructions, clinical_notes, status, outcome_notes, contacted_by, contacted_at, followup_appointment_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, f.getAdmissionId());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return true; // Already created, idempotent
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, f.getFollowupId());
                ps.setString(2, f.getAdmissionId());
                ps.setString(3, f.getPatientId());
                ps.setString(4, f.getPatientName());
                ps.setString(5, f.getDoctorId());
                ps.setString(6, f.getDoctorName());
                ps.setString(7, f.getDischargeDate());
                ps.setString(8, f.getFollowupDate());
                ps.setString(9, f.getDischargeInstructions());
                ps.setString(10, f.getClinicalNotes());
                ps.setString(11, f.getStatus() != null ? f.getStatus() : "Pending");
                ps.setString(12, f.getOutcomeNotes());
                ps.setString(13, f.getContactedBy());
                ps.setString(14, f.getContactedAt());
                ps.setString(15, f.getFollowupAppointmentId());
                ps.setString(16, f.getCreatedAt());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error creating post-discharge follow-up: " + e.getMessage());
            return false;
        }
    }

    public List<PostDischargeFollowup> getAllFollowUps() {
        List<PostDischargeFollowup> list = new ArrayList<>();
        String sql = "SELECT * FROM post_discharge_followups ORDER BY followup_date ASC, created_at DESC";
        try (Connection conn = DBConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFollowUpWithOverdueCheck(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all follow-ups: " + e.getMessage());
        }
        return list;
    }

    public List<PostDischargeFollowup> getFollowUpsByPatient(String patientId) {
        List<PostDischargeFollowup> list = new ArrayList<>();
        String sql = "SELECT * FROM post_discharge_followups WHERE patient_id = ? ORDER BY followup_date ASC";
        try (Connection conn = DBConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapFollowUpWithOverdueCheck(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient follow-ups: " + e.getMessage());
        }
        return list;
    }

    public List<PostDischargeFollowup> getFollowUpsByDoctor(String doctorIdOrName) {
        List<PostDischargeFollowup> list = new ArrayList<>();
        String sql = "SELECT * FROM post_discharge_followups WHERE doctor_id = ? OR LOWER(doctor_name) LIKE ? ORDER BY followup_date ASC";
        try (Connection conn = DBConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, doctorIdOrName);
                ps.setString(2, "%" + doctorIdOrName.toLowerCase() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapFollowUpWithOverdueCheck(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor follow-ups: " + e.getMessage());
        }
        return list;
    }

    public PostDischargeFollowup getFollowUpById(String followupId) {
        String sql = "SELECT * FROM post_discharge_followups WHERE followup_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, followupId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapFollowUpWithOverdueCheck(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching follow-up by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean updateFollowUpOutcome(String followupId, String status, String outcomeNotes, String clinicalNotes, String contactedBy, String contactedAt) {
        String sql = "UPDATE post_discharge_followups SET status = ?, outcome_notes = ?, clinical_notes = COALESCE(?, clinical_notes), contacted_by = ?, contacted_at = ? WHERE followup_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setString(2, outcomeNotes);
                ps.setString(3, clinicalNotes);
                ps.setString(4, contactedBy);
                ps.setString(5, contactedAt);
                ps.setString(6, followupId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating follow-up outcome: " + e.getMessage());
            return false;
        }
    }

    public boolean linkFollowUpAppointment(String followupId, String appointmentId) {
        String sql = "UPDATE post_discharge_followups SET followup_appointment_id = ?, status = 'Appointment Scheduled' WHERE followup_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, appointmentId);
                ps.setString(2, followupId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error linking follow-up appointment: " + e.getMessage());
            return false;
        }
    }

    private PostDischargeFollowup mapFollowUpWithOverdueCheck(ResultSet rs) throws SQLException {
        String status = rs.getString("status");
        String fDate = rs.getString("followup_date");

        // Automatically determine overdue if status is still 'Pending' and date is in past
        if ("Pending".equalsIgnoreCase(status) && fDate != null && fDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                LocalDate target = LocalDate.parse(fDate);
                if (target.isBefore(LocalDate.now())) {
                    status = "Overdue";
                }
            } catch (Exception ignored) {}
        }

        return new PostDischargeFollowup(
                rs.getString("followup_id"),
                rs.getString("admission_id"),
                rs.getString("patient_id"),
                rs.getString("patient_name"),
                rs.getString("doctor_id"),
                rs.getString("doctor_name"),
                rs.getString("discharge_date"),
                rs.getString("followup_date"),
                rs.getString("discharge_instructions"),
                rs.getString("clinical_notes"),
                status,
                rs.getString("outcome_notes"),
                rs.getString("contacted_by"),
                rs.getString("contacted_at"),
                rs.getString("followup_appointment_id"),
                rs.getString("created_at")
        );
    }
}
