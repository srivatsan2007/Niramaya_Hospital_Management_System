package com.hospital.dao;

import com.hospital.model.DailyReport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DailyReportDAO {

    public DailyReportDAO() {
        initTable();
    }

    private void initTable() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS daily_reports (" +
                    "report_id VARCHAR(50) PRIMARY KEY, " +
                    "sender_role VARCHAR(50) NOT NULL, " +
                    "sender_id VARCHAR(50) NOT NULL, " +
                    "sender_name VARCHAR(100) NOT NULL, " +
                    "department VARCHAR(100), " +
                    "report_date VARCHAR(30) NOT NULL, " +
                    "summary_notes TEXT, " +
                    "total_patients INT DEFAULT 0, " +
                    "total_tasks_completed INT DEFAULT 0, " +
                    "total_pending INT DEFAULT 0, " +
                    "revenue_generated DECIMAL(10,2) DEFAULT 0.0, " +
                    "metrics_json TEXT, " +
                    "status VARCHAR(30) DEFAULT 'Submitted', " +
                    "created_at VARCHAR(50)" +
                    ");");
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
    }

    public boolean saveReport(DailyReport report) {
        String sql = "INSERT INTO daily_reports (report_id, sender_role, sender_id, sender_name, department, " +
                "report_date, summary_notes, total_patients, total_tasks_completed, total_pending, revenue_generated, " +
                "metrics_json, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, report.getReportId());
            ps.setString(2, report.getSenderRole());
            ps.setString(3, report.getSenderId());
            ps.setString(4, report.getSenderName());
            ps.setString(5, report.getDepartment());
            ps.setString(6, report.getReportDate());
            ps.setString(7, report.getSummaryNotes());
            ps.setInt(8, report.getTotalPatients());
            ps.setInt(9, report.getTotalTasksCompleted());
            ps.setInt(10, report.getTotalPending());
            ps.setDouble(11, report.getRevenueGenerated());
            ps.setString(12, report.getMetricsJson());
            ps.setString(13, report.getStatus() != null ? report.getStatus() : "Submitted");
            ps.setString(14, report.getCreatedAt());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<DailyReport> getAllReports() {
        List<DailyReport> list = new ArrayList<>();
        String sql = "SELECT * FROM daily_reports ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    public List<DailyReport> getReportsByDate(String date) {
        List<DailyReport> list = new ArrayList<>();
        String sql = "SELECT * FROM daily_reports WHERE report_date = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    public List<DailyReport> getReportsByRole(String role) {
        List<DailyReport> list = new ArrayList<>();
        String sql = "SELECT * FROM daily_reports WHERE LOWER(sender_role) = LOWER(?) ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    private DailyReport extractFromResultSet(ResultSet rs) throws SQLException {
        DailyReport report = new DailyReport();
        report.setReportId(rs.getString("report_id"));
        report.setSenderRole(rs.getString("sender_role"));
        report.setSenderId(rs.getString("sender_id"));
        report.setSenderName(rs.getString("sender_name"));
        report.setDepartment(rs.getString("department"));
        report.setReportDate(rs.getString("report_date"));
        report.setSummaryNotes(rs.getString("summary_notes"));
        report.setTotalPatients(rs.getInt("total_patients"));
        report.setTotalTasksCompleted(rs.getInt("total_tasks_completed"));
        report.setTotalPending(rs.getInt("total_pending"));
        report.setRevenueGenerated(rs.getDouble("revenue_generated"));
        report.setMetricsJson(rs.getString("metrics_json"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getString("created_at"));
        return report;
    }
}
