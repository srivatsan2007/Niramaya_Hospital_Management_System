package com.hospital.model;

/**
 * Model representing an Operation Theater (OT) Surgical Report.
 */
public class OperationReport {
    private String reportId;
    private String patientId;
    private String patientName;
    private String surgeonId;
    private String surgeonName;
    private String anesthetistName;
    private String procedureName;
    private String otRoom;
    private String operationDate;
    private String startTime;
    private String endTime;
    private String anesthesiaType;
    private String status;
    private String findingsNotes;
    private String createdAt;

    public OperationReport() {}

    public OperationReport(String reportId, String patientId, String patientName, String surgeonId,
                           String surgeonName, String anesthetistName, String procedureName, String otRoom,
                           String operationDate, String startTime, String endTime, String anesthesiaType,
                           String status, String findingsNotes, String createdAt) {
        this.reportId = reportId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.surgeonId = surgeonId;
        this.surgeonName = surgeonName;
        this.anesthetistName = anesthetistName;
        this.procedureName = procedureName;
        this.otRoom = otRoom;
        this.operationDate = operationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.anesthesiaType = anesthesiaType;
        this.status = status != null ? status : "Scheduled";
        this.findingsNotes = findingsNotes;
        this.createdAt = createdAt;
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getSurgeonId() { return surgeonId; }
    public void setSurgeonId(String surgeonId) { this.surgeonId = surgeonId; }

    public String getSurgeonName() { return surgeonName; }
    public void setSurgeonName(String surgeonName) { this.surgeonName = surgeonName; }

    public String getAnesthetistName() { return anesthetistName; }
    public void setAnesthetistName(String anesthetistName) { this.anesthetistName = anesthetistName; }

    public String getProcedureName() { return procedureName; }
    public void setProcedureName(String procedureName) { this.procedureName = procedureName; }

    public String getOtRoom() { return otRoom; }
    public void setOtRoom(String otRoom) { this.otRoom = otRoom; }

    public String getOperationDate() { return operationDate; }
    public void setOperationDate(String operationDate) { this.operationDate = operationDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getAnesthesiaType() { return anesthesiaType; }
    public void setAnesthesiaType(String anesthesiaType) { this.anesthesiaType = anesthesiaType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFindingsNotes() { return findingsNotes; }
    public void setFindingsNotes(String findingsNotes) { this.findingsNotes = findingsNotes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
