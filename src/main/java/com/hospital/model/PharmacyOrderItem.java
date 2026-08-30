package com.hospital.model;

/**
 * PharmacyOrderItem Entity Model for Niramaya Hospitals.
 */
public class PharmacyOrderItem {
    private String itemId;
    private String orderId;
    private String medicineId;
    private String medicineName;
    private String strength;
    private String dosage;
    private int morning;
    private int afternoon;
    private int night;
    private String duration;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    private String medicineSource = "Inventory";

    public PharmacyOrderItem() {}

    public PharmacyOrderItem(String itemId, String orderId, String medicineId, String medicineName,
                             String strength, String dosage, int morning, int afternoon, int night,
                             String duration, int quantity, double unitPrice, double subtotal) {
        this(itemId, orderId, medicineId, medicineName, strength, dosage, morning, afternoon, night, duration, quantity, unitPrice, subtotal, "Inventory");
    }

    public PharmacyOrderItem(String itemId, String orderId, String medicineId, String medicineName,
                             String strength, String dosage, int morning, int afternoon, int night,
                             String duration, int quantity, double unitPrice, double subtotal, String medicineSource) {
        this.itemId = itemId;
        this.orderId = orderId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.strength = strength;
        this.dosage = dosage;
        this.morning = morning;
        this.afternoon = afternoon;
        this.night = night;
        this.duration = duration;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.medicineSource = medicineSource != null ? medicineSource : "Inventory";
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public int getMorning() { return morning; }
    public void setMorning(int morning) { this.morning = morning; }

    public int getAfternoon() { return afternoon; }
    public void setAfternoon(int afternoon) { this.afternoon = afternoon; }

    public int getNight() { return night; }
    public void setNight(int night) { this.night = night; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getMedicineSource() { return medicineSource; }
    public void setMedicineSource(String medicineSource) { this.medicineSource = medicineSource; }
}
