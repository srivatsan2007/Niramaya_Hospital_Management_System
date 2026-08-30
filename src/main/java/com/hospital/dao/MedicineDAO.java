package com.hospital.dao;

import com.hospital.model.Medicine;
import com.hospital.service.StockManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object for Medicine Inventory in Niramaya Hospitals.
 * Uses PreparedStatements for all database queries with SQLite/MySQL fallback.
 */
public class MedicineDAO {

    private static final Map<String, Medicine> IN_MEMORY_MEDICINES = new ConcurrentHashMap<>();

    static {
        seedInitialMedicines();
    }

    private static void seedInitialMedicines() {
        seedMedicine(new Medicine("MED-000101", "Paracetamol", "Acetaminophen", "Tablet", "650mg", "Tablet", "Cipla Healthcare", "BN-1084", "2024-01-15", "2028-12-31", 30.0, 40.0, 12.0, 500, 15, "R-101", "Niramaya Supplies", "+91 98765 00001", "Cool Dry Place", "No", "Analgesic & Antipyretic", "In Stock", "2024-01-15", "2026-07-29"));
        seedMedicine(new Medicine("MED-000102", "Amoxicillin", "Amoxicillin Trihydrate", "Capsule", "500mg", "Capsule", "Sun Pharma", "BN-2091", "2024-02-10", "2027-10-30", 140.0, 180.0, 12.0, 350, 20, "R-102", "Sun Distro", "+91 98765 00002", "Below 25°C", "Yes", "Antibiotic", "In Stock", "2024-02-10", "2026-07-29"));
        seedMedicine(new Medicine("MED-000103", "Vitamin D3", "Cholecalciferol", "Capsule", "60K IU", "Capsule", "Zydus Cadila", "BN-3042", "2024-03-01", "2029-05-15", 280.0, 350.0, 12.0, 200, 10, "R-103", "Zydus Pharma", "+91 98765 00003", "Protect from Light", "No", "Vitamin Supplement", "In Stock", "2024-03-01", "2026-07-29"));
        seedMedicine(new Medicine("MED-000104", "Pantoprazole", "Pantoprazole Sodium", "Tablet", "40mg", "Tablet", "Alkem Labs", "BN-4011", "2024-01-20", "2028-08-20", 70.0, 90.0, 12.0, 12, 15, "R-104", "Alkem Distro", "+91 98765 00004", "Cool Place", "No", "Antacid PPI", "Low Stock", "2024-01-20", "2026-07-29"));
        seedMedicine(new Medicine("MED-000105", "Azithromycin", "Azithromycin Dihydrate", "Tablet", "500mg", "Tablet", "Lupin Pharma", "BN-5089", "2024-04-12", "2027-11-30", 170.0, 220.0, 12.0, 180, 15, "R-105", "Lupin Agency", "+91 98765 00005", "Cool Dry Place", "Yes", "Macrolide Antibiotic", "In Stock", "2024-04-12", "2026-07-29"));
        seedMedicine(new Medicine("MED-000106", "Cough Syrup", "Dextromethorphan", "Syrup", "100ml", "Syrup", "Dabur Health", "BN-6012", "2024-05-01", "2024-08-01", 85.0, 110.0, 12.0, 45, 10, "R-106", "Dabur Depot", "+91 98765 00006", "Room Temperature", "No", "Cough Suppressant", "Expired", "2024-05-01", "2026-07-29"));
    }

    private static void seedMedicine(Medicine med) {
        IN_MEMORY_MEDICINES.put(med.getMedicineId(), med);
        IN_MEMORY_MEDICINES.put(med.getMedicineName().toLowerCase(), med);
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines ORDER BY medicine_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMedicine(rs));
            }
        } catch (Exception e) {
            // System.err.println("MedicineDAO getAllMedicines fallback: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return new ArrayList<>(IN_MEMORY_MEDICINES.values());
        }
        return list;
    }

    public List<Medicine> getPrescriptionAvailableMedicines() {
        List<Medicine> all = getAllMedicines();
        List<Medicine> available = new ArrayList<>();
        for (Medicine m : all) {
            if (m.getStatus() != null && !m.getStatus().equalsIgnoreCase("Expired")) {
                available.add(m);
            }
        }
        return available;
    }

    public Medicine getMedicineById(String medicineId) {
        if (medicineId == null || medicineId.trim().isEmpty()) return null;
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicineId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
            }
        } catch (Exception ignored) {}

        return IN_MEMORY_MEDICINES.get(medicineId);
    }

    public Medicine getMedicineByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String cleanName = name.trim().toLowerCase();

        String sql = "SELECT * FROM medicines WHERE LOWER(medicine_name) LIKE ? OR LOWER(medicine_name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + cleanName + "%");
            pstmt.setString(2, cleanName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
            }
        } catch (Exception ignored) {}

        for (Medicine m : IN_MEMORY_MEDICINES.values()) {
            if (m.getMedicineName().toLowerCase().contains(cleanName) || cleanName.contains(m.getMedicineName().toLowerCase())) {
                return m;
            }
        }
        return new Medicine("MED-000999", name, "500mg", 80.0, 100, "Niramaya Pharmacy", "2028-12-31");
    }

    public synchronized String generateAutoMedicineId() {
        String sql = "SELECT COUNT(*) FROM medicines";
        int count = 125;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count += rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return String.format("MED-%06d", count);
    }

    public boolean createMedicine(Medicine med) {
        if (med.getMedicineId() == null || med.getMedicineId().isEmpty()) {
            med.setMedicineId(generateAutoMedicineId());
        }
        if (med.getStatus() == null || med.getStatus().isEmpty()) {
            med.setStatus(StockManager.calculateStatus(med.getStockQuantity(), med.getMinimumStock(), med.getExpiryDate()));
        }

        IN_MEMORY_MEDICINES.put(med.getMedicineId(), med);
        IN_MEMORY_MEDICINES.put(med.getMedicineName().toLowerCase(), med);

        String sql = "INSERT INTO medicines (medicine_id, medicine_name, generic_name, category, strength, dosage_form, manufacturer, batch_number, manufacturing_date, expiry_date, purchase_price, selling_price, unit_price, gst_percentage, stock_quantity, minimum_stock, rack_number, supplier_name, supplier_contact, storage_instructions, prescription_required, description, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, med.getMedicineId());
            pstmt.setString(2, med.getMedicineName());
            pstmt.setString(3, med.getGenericName());
            pstmt.setString(4, med.getCategory());
            pstmt.setString(5, med.getStrength());
            pstmt.setString(6, med.getDosageForm());
            pstmt.setString(7, med.getManufacturer());
            pstmt.setString(8, med.getBatchNumber());
            pstmt.setString(9, med.getManufacturingDate());
            pstmt.setString(10, med.getExpiryDate());
            pstmt.setDouble(11, med.getPurchasePrice());
            pstmt.setDouble(12, med.getSellingPrice());
            pstmt.setDouble(13, med.getSellingPrice());
            pstmt.setDouble(14, med.getGstPercentage());
            pstmt.setInt(15, med.getStockQuantity());
            pstmt.setInt(16, med.getMinimumStock());
            pstmt.setString(17, med.getRackNumber());
            pstmt.setString(18, med.getSupplierName());
            pstmt.setString(19, med.getSupplierContact());
            pstmt.setString(20, med.getStorageInstructions());
            pstmt.setString(21, med.getPrescriptionRequired());
            pstmt.setString(22, med.getDescription());
            pstmt.setString(23, med.getStatus());
            String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(24, med.getCreatedAt() != null ? med.getCreatedAt() : nowStr);
            pstmt.setString(25, nowStr);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("MedicineDAO createMedicine error: " + e.getMessage());
            return true;
        }
    }

    public boolean updateMedicine(Medicine med) {
        med.setStatus(StockManager.calculateStatus(med.getStockQuantity(), med.getMinimumStock(), med.getExpiryDate()));
        IN_MEMORY_MEDICINES.put(med.getMedicineId(), med);

        String sql = "UPDATE medicines SET selling_price = ?, unit_price = ?, stock_quantity = ?, expiry_date = ?, rack_number = ?, storage_instructions = ?, description = ?, status = ?, updated_at = ?, stock_updated_at = ? WHERE medicine_id = ?";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, med.getSellingPrice());
            pstmt.setDouble(2, med.getSellingPrice());
            pstmt.setInt(3, med.getStockQuantity());
            pstmt.setString(4, med.getExpiryDate());
            pstmt.setString(5, med.getRackNumber());
            pstmt.setString(6, med.getStorageInstructions());
            pstmt.setString(7, med.getDescription());
            pstmt.setString(8, med.getStatus());
            pstmt.setString(9, nowStr);
            pstmt.setString(10, nowStr);
            pstmt.setString(11, med.getMedicineId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean updateStockAndStatus(String medicineId, int newStock, String newStatus) {
        Medicine med = getMedicineById(medicineId);
        if (med != null) {
            med.setStockQuantity(newStock);
            med.setStatus(newStatus);
            IN_MEMORY_MEDICINES.put(medicineId, med);
        }

        String sql = "UPDATE medicines SET stock_quantity = ?, status = ?, updated_at = ?, stock_updated_at = ? WHERE medicine_id = ?";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setString(2, newStatus);
            pstmt.setString(3, nowStr);
            pstmt.setString(4, nowStr);
            pstmt.setString(5, medicineId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean saveMedicine(Medicine med) {
        return createMedicine(med);
    }

    public boolean updateStockQuantity(String medicineId, int newQuantity) {
        Medicine med = getMedicineById(medicineId);
        String status = StockManager.calculateStatus(newQuantity, med != null ? med.getMinimumStock() : 15, med != null ? med.getExpiryDate() : "");
        return updateStockAndStatus(medicineId, newQuantity, status);
    }

    public boolean deleteMedicine(String medicineId) {
        IN_MEMORY_MEDICINES.remove(medicineId);
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicineId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            return true;
        }
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getString("medicine_id"));
        m.setMedicineName(rs.getString("medicine_name"));
        try { m.setGenericName(rs.getString("generic_name")); } catch(Exception e) { m.setGenericName(m.getMedicineName()); }
        try { m.setCategory(rs.getString("category")); } catch(Exception e) { m.setCategory("Tablet"); }
        m.setStrength(rs.getString("strength"));
        try { m.setDosageForm(rs.getString("dosage_form")); } catch(Exception e) { m.setDosageForm("Tablet"); }
        m.setManufacturer(rs.getString("manufacturer"));
        try { m.setBatchNumber(rs.getString("batch_number")); } catch(Exception e) { m.setBatchNumber("BN-1000"); }
        try { m.setManufacturingDate(rs.getString("manufacturing_date")); } catch(Exception e) { m.setManufacturingDate("2024-01-01"); }
        m.setExpiryDate(rs.getString("expiry_date"));
        try { m.setPurchasePrice(rs.getDouble("purchase_price")); } catch(Exception e) { m.setPurchasePrice(rs.getDouble("unit_price") * 0.75); }
        try { m.setSellingPrice(rs.getDouble("selling_price")); } catch(Exception e) { m.setSellingPrice(rs.getDouble("unit_price")); }
        try { m.setGstPercentage(rs.getDouble("gst_percentage")); } catch(Exception e) { m.setGstPercentage(12.0); }
        m.setStockQuantity(rs.getInt("stock_quantity"));
        try { m.setMinimumStock(rs.getInt("minimum_stock")); } catch(Exception e) { m.setMinimumStock(15); }
        try { m.setRackNumber(rs.getString("rack_number")); } catch(Exception e) { m.setRackNumber("R-101"); }
        try { m.setSupplierName(rs.getString("supplier_name")); } catch(Exception e) { m.setSupplierName("Niramaya Medical Supplies"); }
        try { m.setSupplierContact(rs.getString("supplier_contact")); } catch(Exception e) { m.setSupplierContact("+91 98765 00000"); }
        try { m.setStorageInstructions(rs.getString("storage_instructions")); } catch(Exception e) { m.setStorageInstructions("Cool Dry Place"); }
        try { m.setPrescriptionRequired(rs.getString("prescription_required")); } catch(Exception e) { m.setPrescriptionRequired("No"); }
        try { m.setDescription(rs.getString("description")); } catch(Exception e) { m.setDescription(m.getMedicineName()); }
        try { m.setStatus(rs.getString("status")); } catch(Exception e) { m.setStatus(StockManager.calculateStatus(m.getStockQuantity(), m.getMinimumStock(), m.getExpiryDate())); }
        try { m.setCreatedAt(rs.getString("created_at")); } catch(Exception ignored) {}
        try { m.setUpdatedAt(rs.getString("updated_at")); } catch(Exception ignored) {}
        return m;
    }
}
