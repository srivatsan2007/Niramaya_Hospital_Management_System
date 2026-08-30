package com.hospital.model;

/**
 * Appointment Entity Model.
 */
public class Appointment {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private String department;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private String paymentStatus;

    public Appointment() {}

    public Appointment(String appointmentId, String patientId, String doctorId, String doctorName,
                       String department, String appointmentDate, String appointmentTime,
                       String status, String paymentStatus) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.department = department;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.paymentStatus = paymentStatus;
    }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
