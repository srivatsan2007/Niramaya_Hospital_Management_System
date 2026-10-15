package com.hospital.dao;

import com.hospital.model.OperationReport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Operation Theater (OT) Surgical Reports and Scheduling.
 */
public class OperationReportDAO {

    public boolean createReport(OperationReport r) {
        String sql = "INSERT INTO operation_reports (report_id, patient_id, patient_name, surgeon_id, surgeon_name, anesthetist_name, procedure_name, ot_room, operation_date, start_time, end_time, anesthesia_type, status, findings_notes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getReportId());
            ps.setString(2, r.getPatientId());
            ps.setString(3, r.getPatientName());
            ps.setString(4, r.getSurgeonId());
            ps.setString(5, r.getSurgeonName());
            ps.setString(6, r.getAnesthetistName());
            ps.setString(7, r.getProcedureName());
            ps.setString(8, r.getOtRoom());
            ps.setString(9, r.getOperationDate());
            ps.setString(10, r.getStartTime());
            ps.setString(11, r.getEndTime());
            ps.setString(12, r.getAnesthesiaType());
            ps.setString(13, r.getStatus() != null ? r.getStatus() : "Scheduled");
            ps.setString(14, r.getFindingsNotes());
            ps.setString(15, r.getCreatedAt());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating operation report: " + e.getMessage());
            return false;
        }
    }

    public List<OperationReport> getAllReports() {
        List<OperationReport> list = new ArrayList<>();
        String sql = "SELECT * FROM operation_reports ORDER BY operation_date DESC, start_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching operation reports: " + e.getMessage());
        }
        return list;
    }

    public List<OperationReport> getReportsBySurgeon(String surgeonIdOrName) {
        List<OperationReport> list = new ArrayList<>();
        String sql = "SELECT * FROM operation_reports WHERE surgeon_id = ? OR surgeon_name LIKE ? ORDER BY operation_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, surgeonIdOrName);
            ps.setString(2, "%" + surgeonIdOrName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching surgeon operation reports: " + e.getMessage());
        }
        return list;
    }

    public List<OperationReport> getReportsByPatient(String patientId) {
        List<OperationReport> list = new ArrayList<>();
        String sql = "SELECT * FROM operation_reports WHERE patient_id = ? ORDER BY operation_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient operation reports: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(String reportId, String status, String findingsNotes) {
        String sql = "UPDATE operation_reports SET status = ?, findings_notes = COALESCE(?, findings_notes) WHERE report_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, findingsNotes);
            ps.setString(3, reportId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating operation report: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReport(String reportId) {
        String sql = "DELETE FROM operation_reports WHERE report_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting operation report: " + e.getMessage());
            return false;
        }
    }

    private OperationReport mapResultSet(ResultSet rs) throws SQLException {
        return new OperationReport(
                rs.getString("report_id"),
                rs.getString("patient_id"),
                rs.getString("patient_name"),
                rs.getString("surgeon_id"),
                rs.getString("surgeon_name"),
                rs.getString("anesthetist_name"),
                rs.getString("procedure_name"),
                rs.getString("ot_room"),
                rs.getString("operation_date"),
                rs.getString("start_time"),
                rs.getString("end_time"),
                rs.getString("anesthesia_type"),
                rs.getString("status"),
                rs.getString("findings_notes"),
                rs.getString("created_at")
        );
    }
}
