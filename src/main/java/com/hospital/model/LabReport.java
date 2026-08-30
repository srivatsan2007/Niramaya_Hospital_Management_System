package com.hospital.model;

import java.util.HashMap;
import java.util.Map;

/**
 * LabReport Entity Model for Niramaya Hospitals Laboratory Module.
 */
public class LabReport {
    private String reportId;
    private String bookingId;
    private String patientId;
    private String patientName;
    private String patientAge;
    private String patientGender;
    private String doctorId;
    private String doctorName;
    private String department;
    private String testName;
    private String result;
    private String observation;
    private String remarks;
    private String reportFile;
    private String uploadedBy;
    private String verifiedBy;
    private String reportDate;
    private String status;
    private String testDataJson; // JSON or formatted key-value pair string for dynamic test parameters

    public LabReport() {
        this.status = "Ready";
        this.verifiedBy = "Verified by Pathologist";
    }

    public LabReport(String reportId, String bookingId, String patientId, String doctorId,
                      String testName, String result, String observation, String remarks,
                      String reportFile, String uploadedBy, String reportDate, String status) {
        this.reportId = reportId;
        this.bookingId = bookingId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testName = testName;
        this.result = result;
        this.observation = observation;
        this.remarks = remarks;
        this.reportFile = reportFile;
        this.uploadedBy = uploadedBy;
        this.reportDate = reportDate;
        this.status = status != null ? status : "Ready";
        this.verifiedBy = "Verified by Pathologist";
    }

    public LabReport(String reportId, String bookingId, String patientId, String patientName,
                      String patientAge, String patientGender, String doctorId, String doctorName,
                      String department, String testName, String result, String observation,
                      String remarks, String testDataJson, String reportFile, String uploadedBy,
                      String verifiedBy, String reportDate, String status) {
        this.reportId = reportId;
        this.bookingId = bookingId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.department = department;
        this.testName = testName;
        this.result = result;
        this.observation = observation;
        this.remarks = remarks;
        this.testDataJson = testDataJson;
        this.reportFile = reportFile;
        this.uploadedBy = uploadedBy;
        this.verifiedBy = verifiedBy != null ? verifiedBy : (doctorName != null ? doctorName + " (Verified)" : "Verified by Pathologist");
        this.reportDate = reportDate;
        this.status = status != null ? status : "Ready";
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

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

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getTestDataJson() { return testDataJson; }
    public void setTestDataJson(String testDataJson) { this.testDataJson = testDataJson; }

    public String getReportFile() { return reportFile; }
    public void setReportFile(String reportFile) { this.reportFile = reportFile; }

    public String getUploadedBy() { return uploadedBy != null ? uploadedBy : "Senior Lab Technologist"; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getVerifiedBy() { return verifiedBy != null ? verifiedBy : (getDoctorName() + " (Verified)"); }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
