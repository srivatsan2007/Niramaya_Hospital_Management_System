package com.hospital.dao;

import com.hospital.model.LabPayment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for LabPayment using PreparedStatements.
 */
public class LabPaymentDAO {

    public boolean createPayment(LabPayment payment) {
        String sql = "INSERT INTO lab_payments (payment_id, booking_id, patient_id, amount, payment_method, transaction_id, payment_status, payment_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, payment.getPaymentId());
            ps.setString(2, payment.getBookingId());
            ps.setString(3, payment.getPatientId());
            ps.setDouble(4, payment.getAmount());
            ps.setString(5, payment.getPaymentMethod());
            ps.setString(6, payment.getTransactionId());
            ps.setString(7, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "Success");
            ps.setString(8, payment.getPaymentDate() != null ? payment.getPaymentDate() : new java.util.Date().toString());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving lab payment: " + e.getMessage());
            return false;
        }
    }

    public List<LabPayment> getPaymentsByPatient(String patientId) {
        List<LabPayment> list = new ArrayList<>();
        String sql = "SELECT * FROM lab_payments WHERE patient_id = ? ORDER BY payment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LabPayment(
                        rs.getString("payment_id"),
                        rs.getString("booking_id"),
                        rs.getString("patient_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        rs.getString("transaction_id"),
                        rs.getString("payment_status"),
                        rs.getString("payment_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient lab payments: " + e.getMessage());
        }
        return list;
    }
}
