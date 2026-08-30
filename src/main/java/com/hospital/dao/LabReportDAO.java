package com.hospital.dao;

import com.hospital.model.LabReport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for LabReport using PreparedStatements and MVC Pattern.
 */
public class LabReportDAO {

    public boolean createReport(LabReport report) {
        String sql = "INSERT INTO lab_reports (report_id, booking_id, patient_id, doctor_id, test_name, result, observation, remarks, report_file, uploaded_by, report_date, status, report_uploaded_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, report.getReportId());
            ps.setString(2, report.getBookingId());
            ps.setString(3, report.getPatientId());
            ps.setString(4, report.getDoctorId() != null ? report.getDoctorId() : "DOC1001");
            ps.setString(5, report.getTestName());
            ps.setString(6, report.getResult() != null ? report.getResult() : "");
            ps.setString(7, report.getObservation() != null ? report.getObservation() : "");
            ps.setString(8, report.getRemarks() != null ? report.getRemarks() : "");
            ps.setString(9, report.getReportFile());
            ps.setString(10, report.getUploadedBy() != null ? report.getUploadedBy() : "Senior Lab Tech");
            ps.setString(11, report.getReportDate() != null ? report.getReportDate() : nowStr);
            ps.setString(12, report.getStatus() != null ? report.getStatus() : "Ready");
            ps.setString(13, nowStr);
            ps.setString(14, nowStr);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving lab report in JDBC: " + e.getMessage());
            return false;
        }
    }

    public boolean logView(String reportId) {
        String sql = "UPDATE lab_reports SET report_viewed_at = ? WHERE report_id = ?";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, reportId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean logDownload(String reportId) {
        String sql = "UPDATE lab_reports SET report_downloaded_at = ? WHERE report_id = ?";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, reportId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<LabReport> getReportsByPatient(String patientId) {
        List<LabReport> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_reports WHERE patient_id = ? ORDER BY report_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient lab reports: " + e.getMessage());
        }
        return list;
    }

    public List<LabReport> getReportsByDoctor(String doctorId) {
        List<LabReport> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_reports WHERE doctor_id = ? OR doctor_id LIKE ? ORDER BY report_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            ps.setString(2, "%" + doctorId + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor lab reports: " + e.getMessage());
        }
        return list;
    }

    public List<LabReport> searchReports(String query) {
        List<LabReport> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_reports WHERE patient_id LIKE ? OR booking_id LIKE ? OR report_id LIKE ? OR test_name LIKE ? ORDER BY report_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + (query != null ? query.trim() : "") + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching lab reports: " + e.getMessage());
        }
        return list;
    }

    public LabReport getReportById(String reportId) {
        String sql = "SELECT * FROM lab_reports WHERE report_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching report by ID: " + e.getMessage());
        }
        return null;
    }

    public List<LabReport> getAllReports() {
        List<LabReport> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_reports ORDER BY report_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all lab reports: " + e.getMessage());
        }
        return list;
    }

    private LabReport mapResultSet(ResultSet rs) throws SQLException {
        LabReport rep = new LabReport(
            rs.getString("report_id"),
            rs.getString("booking_id"),
            rs.getString("patient_id"),
            rs.getString("doctor_id"),
            rs.getString("test_name"),
            rs.getString("result"),
            rs.getString("observation"),
            rs.getString("remarks"),
            rs.getString("report_file"),
            rs.getString("uploaded_by"),
            rs.getString("report_date"),
            rs.getString("status")
        );
        try { if (rs.getString("patient_name") != null) rep.setPatientName(rs.getString("patient_name")); } catch(Exception e){}
        try { if (rs.getString("patient_age") != null) rep.setPatientAge(rs.getString("patient_age")); } catch(Exception e){}
        try { if (rs.getString("patient_gender") != null) rep.setPatientGender(rs.getString("patient_gender")); } catch(Exception e){}
        try { if (rs.getString("doctor_name") != null) rep.setDoctorName(rs.getString("doctor_name")); } catch(Exception e){}
        try { if (rs.getString("department") != null) rep.setDepartment(rs.getString("department")); } catch(Exception e){}
        try { if (rs.getString("verified_by") != null) rep.setVerifiedBy(rs.getString("verified_by")); } catch(Exception e){}
        return rep;
    }
}
