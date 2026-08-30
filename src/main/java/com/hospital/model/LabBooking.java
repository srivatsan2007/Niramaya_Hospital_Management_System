package com.hospital.model;

/**
 * LabBooking Entity Model with full patient & doctor metadata.
 */
public class LabBooking {
    private String bookingId;
    private String patientId;
    private String patientName;
    private String patientAge;
    private String patientGender;
    private String doctorId;
    private String doctorName;
    private String department;
    private String prescriptionId;
    private String testName;
    private String bookingDate;
    private String bookingTime;
    private String status; // 'Pending', 'Sample Collected', 'Testing', 'Completed'
    private String paymentStatus;
    private String createdAt;

    public LabBooking() {}

    public LabBooking(String bookingId, String patientId, String doctorId, String prescriptionId,
                      String testName, String bookingDate, String bookingTime,
                      String status, String paymentStatus, String createdAt) {
        this.bookingId = bookingId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.prescriptionId = prescriptionId;
        this.testName = testName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public LabBooking(String bookingId, String patientId, String patientName, String patientAge, String patientGender,
                      String doctorId, String doctorName, String department, String prescriptionId,
                      String testName, String bookingDate, String bookingTime,
                      String status, String paymentStatus, String createdAt) {
        this.bookingId = bookingId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.department = department;
        this.prescriptionId = prescriptionId;
        this.testName = testName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName != null ? patientName : "Rekha Prasad"; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientAge() { return patientAge != null ? patientAge : "28 Yrs"; }
    public void setPatientAge(String patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender != null ? patientGender : "Female"; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName != null ? doctorName : "Dr. Srivatsan R"; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartment() { return department != null ? department : "General Medicine"; }
    public void setDepartment(String department) { this.department = department; }

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
