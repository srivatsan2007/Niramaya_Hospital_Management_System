package com.hospital.model;

/**
 * LabPayment Entity Model.
 */
public class LabPayment {
    private String paymentId;
    private String bookingId;
    private String patientId;
    private double amount;
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus;
    private String paymentDate;

    public LabPayment() {}

    public LabPayment(String paymentId, String bookingId, String patientId, double amount,
                      String paymentMethod, String transactionId, String paymentStatus, String paymentDate) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.patientId = patientId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
}
