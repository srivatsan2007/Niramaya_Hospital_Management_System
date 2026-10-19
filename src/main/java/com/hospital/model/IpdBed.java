package com.hospital.model;

/**
 * Model representing a physical Hospital In-Patient Bed in Block -> Ward -> Room hierarchy.
 */
public class IpdBed {
    private String bedId;
    private String blockName;
    private String wardType;
    private String roomNumber;
    private String bedNumber;
    private double dailyCharge;
    private String status; // Available, Occupied, Under Maintenance
    private String currentAdmissionId;
    private String currentPatientName;
    private String currentPatientId;

    public IpdBed() {}

    public IpdBed(String bedId, String blockName, String wardType, String roomNumber, String bedNumber,
                  double dailyCharge, String status, String currentAdmissionId, String currentPatientName) {
        this(bedId, blockName, wardType, roomNumber, bedNumber, dailyCharge, status, currentAdmissionId, currentPatientName, null);
    }

    public IpdBed(String bedId, String blockName, String wardType, String roomNumber, String bedNumber,
                  double dailyCharge, String status, String currentAdmissionId, String currentPatientName, String currentPatientId) {
        this.bedId = bedId;
        this.blockName = blockName;
        this.wardType = wardType;
        this.roomNumber = roomNumber;
        this.bedNumber = bedNumber;
        this.dailyCharge = dailyCharge;
        this.status = status != null ? status : "Available";
        this.currentAdmissionId = currentAdmissionId;
        this.currentPatientName = currentPatientName;
        this.currentPatientId = currentPatientId;
    }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public double getDailyCharge() { return dailyCharge; }
    public void setDailyCharge(double dailyCharge) { this.dailyCharge = dailyCharge; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentAdmissionId() { return currentAdmissionId; }
    public void setCurrentAdmissionId(String currentAdmissionId) { this.currentAdmissionId = currentAdmissionId; }

    public String getCurrentPatientName() { return currentPatientName; }
    public void setCurrentPatientName(String currentPatientName) { this.currentPatientName = currentPatientName; }

    public String getCurrentPatientId() { return currentPatientId; }
    public void setCurrentPatientId(String currentPatientId) { this.currentPatientId = currentPatientId; }
}
