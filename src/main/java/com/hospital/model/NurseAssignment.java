package com.hospital.model;

public class NurseAssignment {
    private String assignmentId;
    private String nurseId;
    private String nurseName;
    private String patientId;
    private String patientName;
    private String doctorName;
    private String ward;
    private String roomNumber;
    private String bedNumber;
    private String admissionDate;
    private String status;
    private String createdAt;

    public NurseAssignment() {}

    public NurseAssignment(String assignmentId, String nurseId, String nurseName, String patientId, String patientName,
                           String doctorName, String ward, String roomNumber, String bedNumber, String admissionDate) {
        this.assignmentId = assignmentId;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.ward = ward;
        this.roomNumber = roomNumber;
        this.bedNumber = bedNumber;
        this.admissionDate = admissionDate;
        this.status = "Active";
    }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
