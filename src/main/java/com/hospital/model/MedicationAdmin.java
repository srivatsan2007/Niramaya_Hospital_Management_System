package com.hospital.model;

public class MedicationAdmin {
    private String adminId;
    private String patientId;
    private String prescriptionId;
    private String medicineName;
    private String dosage;
    private String status; // Pending, Given, Missed
    private String dosageTime;
    private String missedReason;
    private String nurseId;
    private String nurseName;
    private String createdAt;

    public MedicationAdmin() {}

    public MedicationAdmin(String adminId, String patientId, String prescriptionId, String medicineName,
                           String dosage, String status, String dosageTime, String missedReason,
                           String nurseId, String nurseName) {
        this.adminId = adminId;
        this.patientId = patientId;
        this.prescriptionId = prescriptionId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.status = status;
        this.dosageTime = dosageTime;
        this.missedReason = missedReason;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
    }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDosageTime() { return dosageTime; }
    public void setDosageTime(String dosageTime) { this.dosageTime = dosageTime; }

    public String getMissedReason() { return missedReason; }
    public void setMissedReason(String missedReason) { this.missedReason = missedReason; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
