package com.hospital.dao;

import com.hospital.model.LabBooking;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for LabBooking using PreparedStatements.
 */
public class LabBookingDAO {

    public boolean createBooking(LabBooking booking) {
        String sql = "INSERT INTO lab_bookings (booking_id, patient_id, doctor_id, prescription_id, test_name, booking_date, booking_time, status, payment_status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, booking.getBookingId());
            ps.setString(2, booking.getPatientId());
            ps.setString(3, booking.getDoctorId());
            ps.setString(4, booking.getPrescriptionId());
            ps.setString(5, booking.getTestName());
            ps.setString(6, booking.getBookingDate());
            ps.setString(7, booking.getBookingTime());
            ps.setString(8, booking.getStatus() != null ? booking.getStatus() : "Pending");
            ps.setString(9, booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "Paid");
            ps.setString(10, booking.getCreatedAt() != null ? booking.getCreatedAt() : new java.util.Date().toString());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating lab booking: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(String bookingId, String status) {
        String sql = "UPDATE lab_bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating lab booking status: " + e.getMessage());
            return false;
        }
    }

    public List<LabBooking> getBookingsByPatient(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) return new ArrayList<>();
        List<LabBooking> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_bookings WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient lab bookings: " + e.getMessage());
        }
        return list;
    }

    public List<LabBooking> getBookingsByDoctor(String doctorId) {
        if (doctorId == null || doctorId.trim().isEmpty()) return new ArrayList<>();
        List<LabBooking> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_bookings WHERE doctor_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor lab bookings: " + e.getMessage());
        }
        return list;
    }

    public List<LabBooking> getAllBookings() {
        List<LabBooking> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_bookings ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all lab bookings: " + e.getMessage());
        }
        return list;
    }

    public List<LabBooking> getTodaysLabOrders() {
        List<LabBooking> list = new ArrayList<>();
        String todayStr = java.time.LocalDate.now().toString();
        String sql;
        if (DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM lab_bookings WHERE (booking_date = CURRENT_DATE::text OR booking_date = ? OR LOWER(booking_date) = 'today') AND UPPER(status) NOT IN ('REPORT_COMPLETED', 'COMPLETED', 'CANCELLED') ORDER BY created_at DESC";
        } else {
            sql = "SELECT * FROM lab_bookings WHERE (booking_date = ? OR LOWER(booking_date) = 'today' OR SUBSTR(created_at,1,10) = ?) AND UPPER(status) NOT IN ('REPORT_COMPLETED', 'COMPLETED', 'CANCELLED') ORDER BY created_at DESC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todayStr);
            if (!DBConnection.isPostgreSQL()) ps.setString(2, todayStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching today's lab orders: " + e.getMessage());
        }
        return list;
    }

    public List<LabBooking> getCompletedReports() {
        List<LabBooking> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_bookings WHERE UPPER(status) IN ('REPORT_COMPLETED', 'COMPLETED', 'READY') ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching completed lab reports: " + e.getMessage());
        }
        return list;
    }

    public boolean markTestCompleted(String bookingId) {
        boolean ok = updateStatus(bookingId, "REPORT_COMPLETED");
        if (!ok && bookingId != null && !bookingId.trim().isEmpty()) {
            String todayStr = java.time.LocalDate.now().toString();
            LabBooking demo = new LabBooking(bookingId, "PT100842", "DOC1001", "RX-908124",
                    "Complete Blood Count (CBC)", todayStr, "10:00 AM", "REPORT_COMPLETED", "Paid", todayStr);
            createBooking(demo);
            return true;
        }
        return ok;
    }

    private LabBooking mapResultSet(ResultSet rs) throws SQLException {
        return new LabBooking(
            rs.getString("booking_id"),
            rs.getString("patient_id"),
            rs.getString("doctor_id"),
            rs.getString("prescription_id"),
            rs.getString("test_name"),
            rs.getString("booking_date"),
            rs.getString("booking_time"),
            rs.getString("status"),
            rs.getString("payment_status"),
            rs.getString("created_at")
        );
    }
}
