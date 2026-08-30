package com.hospital.model;

public class NursingNote {
    private String noteId;
    private String patientId;
    private String nurseId;
    private String nurseName;
    private String observation;
    private String noteDate;
    private String noteTime;
    private String createdAt;

    public NursingNote() {}

    public NursingNote(String noteId, String patientId, String nurseId, String nurseName, String observation, String noteDate, String noteTime) {
        this.noteId = noteId;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.observation = observation;
        this.noteDate = noteDate;
        this.noteTime = noteTime;
    }

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public String getNoteDate() { return noteDate; }
    public void setNoteDate(String noteDate) { this.noteDate = noteDate; }

    public String getNoteTime() { return noteTime; }
    public void setNoteTime(String noteTime) { this.noteTime = noteTime; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
