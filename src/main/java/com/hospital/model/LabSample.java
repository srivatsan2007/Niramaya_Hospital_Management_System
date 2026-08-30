package com.hospital.model;

public class LabSample {
    private String sampleId;
    private String patientId;
    private String patientName;
    private String nurseId;
    private String nurseName;
    private String sampleType; // Blood, Urine, Stool, Swab
    private String status; // Pending, Collected, Sent to Lab, Completed
    private String collectedTime;
    private String createdAt;

    public LabSample() {}

    public LabSample(String sampleId, String patientId, String patientName, String nurseId,
                     String nurseName, String sampleType, String status, String collectedTime) {
        this.sampleId = sampleId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.sampleType = sampleType;
        this.status = status;
        this.collectedTime = collectedTime;
    }

    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getSampleType() { return sampleType; }
    public void setSampleType(String sampleType) { this.sampleType = sampleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCollectedTime() { return collectedTime; }
    public void setCollectedTime(String collectedTime) { this.collectedTime = collectedTime; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
