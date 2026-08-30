package com.hospital.model;

public class EmergencyAlert {
    private String alertId;
    private String patientId;
    private String patientName;
    private String roomNumber;
    private String ward;
    private String nurseId;
    private String nurseName;
    private String alertType; // Cardiac Emergency, Low Oxygen, High Fever, Low BP, Critical Patient, Other
    private String alertTime;
    private String status; // Active, Resolved
    private String resolvedBy;
    private String createdAt;

    public EmergencyAlert() {}

    public EmergencyAlert(String alertId, String patientId, String patientName, String roomNumber,
                          String ward, String nurseId, String nurseName, String alertType, String alertTime) {
        this.alertId = alertId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.roomNumber = roomNumber;
        this.ward = ward;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.alertType = alertType;
        this.alertTime = alertTime;
        this.status = "Active";
    }

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getAlertTime() { return alertTime; }
    public void setAlertTime(String alertTime) { this.alertTime = alertTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
