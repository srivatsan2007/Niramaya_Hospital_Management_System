package com.hospital.service;

import com.hospital.dao.MedicineDAO;
import com.hospital.model.Medicine;

import java.util.*;

/**
 * InventoryManagement handles inventory stock alerts, status evaluation, and CRUD.
 */
public class InventoryManagement {

    private MedicineDAO medicineDAO = new MedicineDAO();

    public List<Medicine> getAllMedicines() {
        return medicineDAO.getAllMedicines();
    }

    public String evaluateStockStatus(Medicine med) {
        if (med == null) return "Out of Stock";
        int qty = med.getStockQuantity();
        if (qty <= 0) return "🔴 Out of Stock";
        if (qty <= 15) return "🟡 Low Stock";
        return "🟢 In Stock";
    }

    public List<Medicine> getLowStockMedicines() {
        List<Medicine> all = getAllMedicines();
        List<Medicine> lowStock = new ArrayList<>();
        for (Medicine m : all) {
            if (m.getStockQuantity() <= 15) {
                lowStock.add(m);
            }
        }
        return lowStock;
    }

    public boolean addMedicine(Medicine med) {
        return medicineDAO.saveMedicine(med);
    }

    public boolean updateStock(String medicineId, int newQuantity) {
        return medicineDAO.updateStockQuantity(medicineId, newQuantity);
    }

    public boolean deleteMedicine(String medicineId) {
        return medicineDAO.deleteMedicine(medicineId);
    }
}
