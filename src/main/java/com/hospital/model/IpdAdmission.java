package com.hospital.model;

/**
 * Model representing an In-Patient Department (IPD) Admission and stay tracker.
 */
public class IpdAdmission {
    private String admissionId;
    private String patientId;
    private String patientName;
    private String patientGender;
    private int patientAge;
    private String patientPhone;
    private String bedId;
    private String blockName;
    private String wardType;
    private String roomNumber;
    private String bedNumber;
    private String attendingDoctor;
    private String admissionDate;
    private String expectedDischargeDate;
    private String actualDischargeDate;
    private String diagnosis;
    private String status; // Admitted, Discharged, Transferred
    private double advancePaid;
    private String createdAt;

    public IpdAdmission() {}

    public IpdAdmission(String admissionId, String patientId, String patientName, String patientGender,
                        int patientAge, String patientPhone, String bedId, String blockName,
                        String wardType, String roomNumber, String bedNumber, String attendingDoctor,
                        String admissionDate, String expectedDischargeDate, String actualDischargeDate,
                        String diagnosis, String status, double advancePaid, String createdAt) {
        this.admissionId = admissionId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientGender = patientGender;
        this.patientAge = patientAge;
        this.patientPhone = patientPhone;
        this.bedId = bedId;
        this.blockName = blockName;
        this.wardType = wardType;
        this.roomNumber = roomNumber;
        this.bedNumber = bedNumber;
        this.attendingDoctor = attendingDoctor;
        this.admissionDate = admissionDate;
        this.expectedDischargeDate = expectedDischargeDate;
        this.actualDischargeDate = actualDischargeDate;
        this.diagnosis = diagnosis;
        this.status = status != null ? status : "Admitted";
        this.advancePaid = advancePaid;
        this.createdAt = createdAt;
    }

    public String getAdmissionId() { return admissionId; }
    public void setAdmissionId(String admissionId) { this.admissionId = admissionId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public int getPatientAge() { return patientAge; }
    public void setPatientAge(int patientAge) { this.patientAge = patientAge; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

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

    public String getAttendingDoctor() { return attendingDoctor; }
    public void setAttendingDoctor(String attendingDoctor) { this.attendingDoctor = attendingDoctor; }

    public String getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

    public String getExpectedDischargeDate() { return expectedDischargeDate; }
    public void setExpectedDischargeDate(String expectedDischargeDate) { this.expectedDischargeDate = expectedDischargeDate; }

    public String getActualDischargeDate() { return actualDischargeDate; }
    public void setActualDischargeDate(String actualDischargeDate) { this.actualDischargeDate = actualDischargeDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(double advancePaid) { this.advancePaid = advancePaid; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
