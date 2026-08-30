package com.hospital.model;

public class NurseShift {
    private String shiftId;
    private String nurseId;
    private String nurseName;
    private String shiftType;
    private String startTime;
    private String endTime;
    private String ward;
    private String handoverNotes;
    private String status;
    private String createdAt;

    public NurseShift() {}

    public NurseShift(String shiftId, String nurseId, String nurseName, String shiftType, String startTime,
                      String endTime, String ward, String handoverNotes, String status) {
        this.shiftId = shiftId;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.shiftType = shiftType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ward = ward;
        this.handoverNotes = handoverNotes;
        this.status = status;
    }

    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getShiftType() { return shiftType; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getHandoverNotes() { return handoverNotes; }
    public void setHandoverNotes(String handoverNotes) { this.handoverNotes = handoverNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
