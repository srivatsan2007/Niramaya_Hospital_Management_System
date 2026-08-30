package com.hospital.model;

/**
 * Prescription Entity Model.
 */
public class Prescription {
    private String prescriptionId;
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private String diagnosis;
    private String medicines;
    private String doctorNotes;
    private String followUp;
    private String createdDate;

    public Prescription() {}

    public Prescription(String prescriptionId, String appointmentId, String doctorId, String patientId,
                        String diagnosis, String medicines, String doctorNotes, String followUp, String createdDate) {
        this.prescriptionId = prescriptionId;
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.diagnosis = diagnosis;
        this.medicines = medicines;
        this.doctorNotes = doctorNotes;
        this.followUp = followUp;
        this.createdDate = createdDate;
    }

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getMedicines() { return medicines; }
    public void setMedicines(String medicines) { this.medicines = medicines; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    public String getFollowUp() { return followUp; }
    public void setFollowUp(String followUp) { this.followUp = followUp; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
