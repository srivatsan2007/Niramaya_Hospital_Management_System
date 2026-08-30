package com.hospital.dao;

import com.hospital.model.PharmacyOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {
    
    // In Niramaya Hospitals, a Pharmacy Invoice is equivalent to a "Paid" PharmacyOrder.
    // This DAO handles fetching those specific records for the Invoice History module.

    public List<PharmacyOrder> getInvoicesByPatient(String patientId) {
        List<PharmacyOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacy_orders WHERE patient_id = ? AND payment_status = 'Paid' ORDER BY order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PharmacyOrder o = new PharmacyOrder(
                        rs.getString("order_id"),
                        rs.getString("pharmacy_token"),
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getString("prescription_id"),
                        rs.getString("appointment_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status"),
                        rs.getString("order_status"),
                        rs.getString("payment_method"),
                        rs.getString("transaction_id"),
                        rs.getString("order_date")
                );
                list.add(o);
            }
        } catch (Exception e) {
            System.err.println("InvoiceDAO Error: " + e.getMessage());
        }
        return list;
    }
}
