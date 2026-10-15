package com.hospital.dao;

import com.hospital.model.BirthReport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Maternity & Newborn Birth Registry persistence.
 */
public class BirthReportDAO {

    public boolean createBirthReport(BirthReport r) {
        String sql = "INSERT INTO birth_reports (report_id, certificate_no, mother_id, mother_name, father_name, baby_gender, birth_weight, birth_date, birth_time, delivery_type, delivering_doctor, pediatrician_name, blood_group, complications_notes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getReportId());
            ps.setString(2, r.getCertificateNo());
            ps.setString(3, r.getMotherId());
            ps.setString(4, r.getMotherName());
            ps.setString(5, r.getFatherName());
            ps.setString(6, r.getBabyGender());
            ps.setString(7, r.getBirthWeight());
            ps.setString(8, r.getBirthDate());
            ps.setString(9, r.getBirthTime());
            ps.setString(10, r.getDeliveryType());
            ps.setString(11, r.getDeliveringDoctor());
            ps.setString(12, r.getPediatricianName());
            ps.setString(13, r.getBloodGroup());
            ps.setString(14, r.getComplicationsNotes());
            ps.setString(15, r.getCreatedAt());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating birth report: " + e.getMessage());
            return false;
        }
    }

    public List<BirthReport> getAllBirthReports() {
        List<BirthReport> list = new ArrayList<>();
        String sql = "SELECT * FROM birth_reports ORDER BY birth_date DESC, birth_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching birth reports: " + e.getMessage());
        }
        return list;
    }

    public BirthReport getBirthReportByCertificate(String certificateNo) {
        String sql = "SELECT * FROM birth_reports WHERE certificate_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, certificateNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching birth certificate: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteBirthReport(String reportId) {
        String sql = "DELETE FROM birth_reports WHERE report_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reportId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting birth report: " + e.getMessage());
            return false;
        }
    }

    private BirthReport mapResultSet(ResultSet rs) throws SQLException {
        return new BirthReport(
                rs.getString("report_id"),
                rs.getString("certificate_no"),
                rs.getString("mother_id"),
                rs.getString("mother_name"),
                rs.getString("father_name"),
                rs.getString("baby_gender"),
                rs.getString("birth_weight"),
                rs.getString("birth_date"),
                rs.getString("birth_time"),
                rs.getString("delivery_type"),
                rs.getString("delivering_doctor"),
                rs.getString("pediatrician_name"),
                rs.getString("blood_group"),
                rs.getString("complications_notes"),
                rs.getString("created_at")
        );
    }
}
