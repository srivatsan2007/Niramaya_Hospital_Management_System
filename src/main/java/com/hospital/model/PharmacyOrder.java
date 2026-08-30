package com.hospital.model;

/**
 * PharmacyOrder Entity Model for Niramaya Hospitals.
 */
public class PharmacyOrder {
    private String orderId;
    private String pharmacyToken;
    private String patientId;
    private String doctorId;
    private String prescriptionId;
    private String appointmentId;
    private double totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private String paymentMethod;
    private String transactionId;
    private String orderDate;

    public PharmacyOrder() {}

    public PharmacyOrder(String orderId, String pharmacyToken, String patientId, String doctorId,
                         String prescriptionId, String appointmentId, double totalAmount,
                         String paymentStatus, String orderStatus, String paymentMethod,
                         String transactionId, String orderDate) {
        this.orderId = orderId;
        this.pharmacyToken = pharmacyToken;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.prescriptionId = prescriptionId;
        this.appointmentId = appointmentId;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.orderDate = orderDate;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPharmacyToken() { return pharmacyToken; }
    public void setPharmacyToken(String pharmacyToken) { this.pharmacyToken = pharmacyToken; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
}
