package com.hospital.model;

/**
 * Model representing a Post-Discharge Clinical Follow-up for discharged inpatients.
 */
public class PostDischargeFollowup {
    private String followupId;
    private String admissionId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String dischargeDate;
    private String followupDate;
    private String dischargeInstructions;
    private String clinicalNotes;
    private String status; // 'Pending', 'Completed', 'Overdue', 'Unable to Reach'
    private String outcomeNotes;
    private String contactedBy;
    private String contactedAt;
    private String followupAppointmentId;
    private String createdAt;

    public PostDischargeFollowup() {}

    public PostDischargeFollowup(String followupId, String admissionId, String patientId, String patientName,
                                 String doctorId, String doctorName, String dischargeDate, String followupDate,
                                 String dischargeInstructions, String clinicalNotes, String status,
                                 String outcomeNotes, String contactedBy, String contactedAt,
                                 String followupAppointmentId, String createdAt) {
        this.followupId = followupId;
        this.admissionId = admissionId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.dischargeDate = dischargeDate;
        this.followupDate = followupDate;
        this.dischargeInstructions = dischargeInstructions;
        this.clinicalNotes = clinicalNotes;
        this.status = status;
        this.outcomeNotes = outcomeNotes;
        this.contactedBy = contactedBy;
        this.contactedAt = contactedAt;
        this.followupAppointmentId = followupAppointmentId;
        this.createdAt = createdAt;
    }

    public String getFollowupId() { return followupId; }
    public void setFollowupId(String followupId) { this.followupId = followupId; }

    public String getAdmissionId() { return admissionId; }
    public void setAdmissionId(String admissionId) { this.admissionId = admissionId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }

    public String getFollowupDate() { return followupDate; }
    public void setFollowupDate(String followupDate) { this.followupDate = followupDate; }

    public String getDischargeInstructions() { return dischargeInstructions; }
    public void setDischargeInstructions(String dischargeInstructions) { this.dischargeInstructions = dischargeInstructions; }

    public String getClinicalNotes() { return clinicalNotes; }
    public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOutcomeNotes() { return outcomeNotes; }
    public void setOutcomeNotes(String outcomeNotes) { this.outcomeNotes = outcomeNotes; }

    public String getContactedBy() { return contactedBy; }
    public void setContactedBy(String contactedBy) { this.contactedBy = contactedBy; }

    public String getContactedAt() { return contactedAt; }
    public void setContactedAt(String contactedAt) { this.contactedAt = contactedAt; }

    public String getFollowupAppointmentId() { return followupAppointmentId; }
    public void setFollowupAppointmentId(String followupAppointmentId) { this.followupAppointmentId = followupAppointmentId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
