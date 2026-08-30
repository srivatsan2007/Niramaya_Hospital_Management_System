package com.hospital.model;

/**
 * Medicine Entity Model for Niramaya Hospitals Pharmacy & Inventory.
 */
public class Medicine {
    private String medicineId;
    private String medicineName;
    private String genericName;
    private String category;
    private String strength;
    private String dosageForm;
    private String manufacturer;
    private String batchNumber;
    private String manufacturingDate;
    private String expiryDate;
    private double purchasePrice;
    private double sellingPrice;
    private double gstPercentage;
    private int stockQuantity;
    private int minimumStock;
    private String rackNumber;
    private String supplierName;
    private String supplierContact;
    private String storageInstructions;
    private String prescriptionRequired; // "Yes" or "No"
    private String description;
    private String status; // "In Stock", "Low Stock", "Out of Stock", "Expiring Soon", "Expired"
    private String createdAt;
    private String updatedAt;

    public Medicine() {
        this.prescriptionRequired = "No";
        this.status = "In Stock";
        this.gstPercentage = 12.0;
        this.minimumStock = 15;
    }

    public Medicine(String medicineId, String medicineName, String strength, double unitPrice, int stockQuantity, String manufacturer, String expiryDate) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = medicineName;
        this.category = "Tablet";
        this.strength = strength;
        this.dosageForm = "Tablet";
        this.manufacturer = manufacturer;
        this.batchNumber = "BN-" + (1000 + (int)(Math.random() * 9000));
        this.manufacturingDate = "2024-01-01";
        this.expiryDate = expiryDate;
        this.purchasePrice = unitPrice * 0.75;
        this.sellingPrice = unitPrice;
        this.gstPercentage = 12.0;
        this.stockQuantity = stockQuantity;
        this.minimumStock = 15;
        this.rackNumber = "R-101";
        this.supplierName = "Niramaya Medical Supplies";
        this.supplierContact = "+91 98765 00000";
        this.storageInstructions = "Store in a cool dry place";
        this.prescriptionRequired = "No";
        this.description = medicineName + " " + strength;
        this.status = stockQuantity <= 0 ? "Out of Stock" : (stockQuantity <= 15 ? "Low Stock" : "In Stock");
        this.createdAt = new java.util.Date().toString();
        this.updatedAt = new java.util.Date().toString();
    }

    // Full constructor
    public Medicine(String medicineId, String medicineName, String genericName, String category, String strength, String dosageForm, String manufacturer, String batchNumber, String manufacturingDate, String expiryDate, double purchasePrice, double sellingPrice, double gstPercentage, int stockQuantity, int minimumStock, String rackNumber, String supplierName, String supplierContact, String storageInstructions, String prescriptionRequired, String description, String status, String createdAt, String updatedAt) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.category = category;
        this.strength = strength;
        this.dosageForm = dosageForm;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.gstPercentage = gstPercentage;
        this.stockQuantity = stockQuantity;
        this.minimumStock = minimumStock;
        this.rackNumber = rackNumber;
        this.supplierName = supplierName;
        this.supplierContact = supplierContact;
        this.storageInstructions = storageInstructions;
        this.prescriptionRequired = prescriptionRequired;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(String manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    // Compatibility getter for unitPrice
    public double getUnitPrice() { return sellingPrice > 0 ? sellingPrice : purchasePrice; }
    public void setUnitPrice(double unitPrice) { this.sellingPrice = unitPrice; }

    public double getGstPercentage() { return gstPercentage; }
    public void setGstPercentage(double gstPercentage) { this.gstPercentage = gstPercentage; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }

    public String getRackNumber() { return rackNumber; }
    public void setRackNumber(String rackNumber) { this.rackNumber = rackNumber; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }

    public String getStorageInstructions() { return storageInstructions; }
    public void setStorageInstructions(String storageInstructions) { this.storageInstructions = storageInstructions; }

    public String getPrescriptionRequired() { return prescriptionRequired; }
    public void setPrescriptionRequired(String prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
