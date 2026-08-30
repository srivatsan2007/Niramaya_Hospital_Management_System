package com.hospital.model;

/**
 * Consultation Notes Model for Telemedicine Module.
 */
public class ConsultationNotes {
    private String noteId;
    private String consultationId;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String consultationSummary;
    private String diagnosis;
    private String advice;
    private String followUpDate;
    private String medicalCertificateRequired; // Yes / No
    private String createdAt;

    public ConsultationNotes() {}

    public ConsultationNotes(String noteId, String consultationId, String appointmentId, String patientId,
                             String doctorId, String consultationSummary, String diagnosis, String advice,
                             String followUpDate, String medicalCertificateRequired, String createdAt) {
        this.noteId = noteId;
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.consultationSummary = consultationSummary;
        this.diagnosis = diagnosis;
        this.advice = advice;
        this.followUpDate = followUpDate;
        this.medicalCertificateRequired = medicalCertificateRequired;
        this.createdAt = createdAt;
    }

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getConsultationSummary() { return consultationSummary; }
    public void setConsultationSummary(String consultationSummary) { this.consultationSummary = consultationSummary; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }

    public String getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }

    public String getMedicalCertificateRequired() { return medicalCertificateRequired; }
    public void setMedicalCertificateRequired(String medicalCertificateRequired) { this.medicalCertificateRequired = medicalCertificateRequired; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
