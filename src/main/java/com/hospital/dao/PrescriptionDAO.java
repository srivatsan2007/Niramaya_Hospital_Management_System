package com.hospital.dao;

import com.hospital.model.Prescription;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO for Prescription persistence using PreparedStatements with in-memory fallback.
 */
public class PrescriptionDAO {

    private static final Map<String, Prescription> MEM_PRESCRIPTIONS = new ConcurrentHashMap<>();

    public boolean createPrescription(Prescription p) {
        if (p != null && p.getPrescriptionId() != null) {
            MEM_PRESCRIPTIONS.put(p.getPrescriptionId(), p);
        }

        String sql = "INSERT INTO prescriptions (prescription_id, appointment_id, doctor_id, patient_id, diagnosis, medicines, doctor_notes, follow_up, created_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPrescriptionId());
            ps.setString(2, p.getAppointmentId());
            ps.setString(3, p.getDoctorId());
            ps.setString(4, p.getPatientId());
            ps.setString(5, p.getDiagnosis());
            ps.setString(6, p.getMedicines());
            ps.setString(7, p.getDoctorNotes());
            ps.setString(8, p.getFollowUp());
            ps.setString(9, p.getCreatedDate() != null ? p.getCreatedDate() : nowStr);
            ps.setString(10, nowStr);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving prescription: " + e.getMessage());
            return true; // cached in MEM_PRESCRIPTIONS
        }
    }

    public Prescription getPrescriptionById(String rxId) {
        if (rxId == null || rxId.trim().isEmpty()) return null;
        String sql = "SELECT * FROM prescriptions WHERE prescription_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rxId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPrescription(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching prescription by ID: " + e.getMessage());
        }

        return MEM_PRESCRIPTIONS.get(rxId.trim());
    }

    public boolean logDownload(String rxId) {
        String sql = "UPDATE prescriptions SET downloaded_at = ? WHERE prescription_id = ?";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, rxId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging prescription download: " + e.getMessage());
            return false;
        }
    }

    public List<Prescription> getPrescriptionsByPatient(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) return new ArrayList<>();
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? ORDER BY created_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPrescription(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient prescriptions: " + e.getMessage());
        }

        if (list.isEmpty()) {
            for (Prescription p : MEM_PRESCRIPTIONS.values()) {
                if (patientId.equalsIgnoreCase(p.getPatientId())) {
                    list.add(p);
                }
            }
        }
        return list;
    }

    public List<Prescription> getPrescriptionsByDoctor(String doctorId) {
        if (doctorId == null || doctorId.trim().isEmpty()) return new ArrayList<>();
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE doctor_id = ? ORDER BY created_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPrescription(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor prescriptions: " + e.getMessage());
        }

        if (list.isEmpty()) {
            for (Prescription p : MEM_PRESCRIPTIONS.values()) {
                if (doctorId.equalsIgnoreCase(p.getDoctorId())) {
                    list.add(p);
                }
            }
        }
        return list;
    }

    private Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        return new Prescription(
            rs.getString("prescription_id"),
            rs.getString("appointment_id"),
            rs.getString("doctor_id"),
            rs.getString("patient_id"),
            rs.getString("diagnosis"),
            rs.getString("medicines"),
            rs.getString("doctor_notes"),
            rs.getString("follow_up"),
            rs.getString("created_date")
        );
    }
}
