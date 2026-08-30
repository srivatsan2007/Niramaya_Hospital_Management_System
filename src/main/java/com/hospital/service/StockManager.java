package com.hospital.service;

import com.hospital.dao.DBConnection;
import com.hospital.dao.MedicineDAO;
import com.hospital.model.Medicine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * StockManager Service for Niramaya Hospitals Pharmacy.
 * Handles inventory validation, status evaluation, stock adjustment, and deletion checks.
 */
public class StockManager {

    private MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * Calculate inventory status based on stock & expiry dates.
     */
    public static String calculateStatus(int stockQuantity, int minimumStock, String expiryDate) {
        try {
            if (expiryDate != null && !expiryDate.trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date exp = sdf.parse(expiryDate.trim());
                Date today = new Date();
                long diffInMillies = exp.getTime() - today.getTime();
                long daysLeft = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                if (daysLeft < 0) {
                    return "Expired";
                } else if (daysLeft <= 60) {
                    return "Expiring Soon";
                }
            }
        } catch (Exception ignored) {}

        if (stockQuantity <= 0) {
            return "Out of Stock";
        } else if (stockQuantity <= minimumStock) {
            return "Low Stock";
        }
        return "In Stock";
    }

    /**
     * Validate medicine attributes before saving.
     */
    public String validateMedicine(Medicine med, boolean isEdit) {
        if (med.getMedicineName() == null || med.getMedicineName().trim().isEmpty()) {
            return "Medicine Name cannot be empty.";
        }
        if (med.getBatchNumber() == null || med.getBatchNumber().trim().isEmpty()) {
            return "Batch Number cannot be empty.";
        }
        if (!isEdit && isBatchNumberExists(med.getBatchNumber(), med.getMedicineId())) {
            return "Batch Number must be unique. '" + med.getBatchNumber() + "' is already registered.";
        }
        if (med.getManufacturingDate() != null && med.getExpiryDate() != null && !med.getManufacturingDate().isEmpty() && !med.getExpiryDate().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date mfg = sdf.parse(med.getManufacturingDate().trim());
                Date exp = sdf.parse(med.getExpiryDate().trim());
                if (!exp.after(mfg)) {
                    return "Expiry Date must be later than Manufacturing Date.";
                }
            } catch (Exception ignored) {}
        }
        if (med.getPurchasePrice() < 0) {
            return "Purchase Price cannot be negative.";
        }
        if (med.getSellingPrice() < med.getPurchasePrice()) {
            return "Selling Price cannot be less than Purchase Price.";
        }
        if (med.getStockQuantity() < 0) {
            return "Stock Quantity cannot be negative.";
        }
        if (med.getMinimumStock() > med.getStockQuantity()) {
            return "Minimum Stock cannot exceed Stock Quantity.";
        }
        return null; // Valid
    }

    private boolean isBatchNumberExists(String batchNo, String currentMedId) {
        String sql = "SELECT COUNT(*) FROM medicines WHERE LOWER(batch_number) = ? AND medicine_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, batchNo.trim().toLowerCase());
            ps.setString(2, currentMedId == null ? "" : currentMedId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Check if a medicine can be deleted:
     * Allowed only if Medicine has NEVER been prescribed AND Current Stock == 0.
     */
    public String checkCanDelete(String medicineId) {
        // Check current stock
        Medicine med = medicineDAO.getMedicineById(medicineId);
        if (med != null && med.getStockQuantity() > 0) {
            return "This medicine cannot be deleted because it has prescription history or available stock.";
        }

        // Check prescription item history
        String sql = "SELECT COUNT(*) FROM pharmacy_order_items WHERE medicine_id = ? OR LOWER(medicine_name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicineId);
            ps.setString(2, med != null ? med.getMedicineName().toLowerCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "This medicine cannot be deleted because it has prescription history or available stock.";
                }
            }
        } catch (Exception ignored) {}

        return null; // Can delete
    }

    /**
     * Update Stock (Increase, Decrease, Adjust).
     */
    public boolean updateStock(String medicineId, String actionType, int quantity, String reason, String remarks) {
        Medicine med = medicineDAO.getMedicineById(medicineId);
        if (med == null) return false;

        int currentStock = med.getStockQuantity();
        int newStock = currentStock;

        if ("Increase".equalsIgnoreCase(actionType)) {
            newStock += quantity;
        } else if ("Decrease".equalsIgnoreCase(actionType)) {
            newStock = Math.max(0, currentStock - quantity);
        } else if ("Adjust".equalsIgnoreCase(actionType)) {
            newStock = Math.max(0, quantity);
        }

        String newStatus = calculateStatus(newStock, med.getMinimumStock(), med.getExpiryDate());
        return medicineDAO.updateStockAndStatus(medicineId, newStock, newStatus);
    }
}
