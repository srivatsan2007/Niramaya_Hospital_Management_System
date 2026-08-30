package com.hospital.model;

public class InjectionRecord {
    private String injectionId;
    private String patientId;
    private String nurseId;
    private String nurseName;
    private String injectionName;
    private String dose;
    private String route;
    private String recordDate;
    private String recordTime;
    private String remarks;
    private String createdAt;

    public InjectionRecord() {}

    public InjectionRecord(String injectionId, String patientId, String nurseId, String nurseName,
                           String injectionName, String dose, String route, String recordDate,
                           String recordTime, String remarks) {
        this.injectionId = injectionId;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.injectionName = injectionName;
        this.dose = dose;
        this.route = route;
        this.recordDate = recordDate;
        this.recordTime = recordTime;
        this.remarks = remarks;
    }

    public String getInjectionId() { return injectionId; }
    public void setInjectionId(String injectionId) { this.injectionId = injectionId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getInjectionName() { return injectionName; }
    public void setInjectionName(String injectionName) { this.injectionName = injectionName; }

    public String getDose() { return dose; }
    public void setDose(String dose) { this.dose = dose; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }

    public String getRecordTime() { return recordTime; }
    public void setRecordTime(String recordTime) { this.recordTime = recordTime; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
