package com.hospital.model;

public class PatientVital {
    private String vitalId;
    private String appointmentId;
    private String patientId;
    private String nurseId;
    private String nurseName;
    private String temperature;
    private String bloodPressure;
    private String pulseRate;
    private String respiratoryRate;
    private String oxygenSaturation;
    private String bloodSugar;
    private String weight;
    private String height;
    private String symptoms;
    private String nurseNotes;
    private String recordedDate;
    private String recordedTime;
    private String createdAt;

    public PatientVital() {}

    public PatientVital(String vitalId, String patientId, String nurseId, String nurseName,
                        String temperature, String bloodPressure, String pulseRate, String respiratoryRate,
                        String oxygenSaturation, String bloodSugar, String weight, String height,
                        String recordedDate, String recordedTime) {
        this.vitalId = vitalId;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.temperature = temperature;
        this.bloodPressure = bloodPressure;
        this.pulseRate = pulseRate;
        this.respiratoryRate = respiratoryRate;
        this.oxygenSaturation = oxygenSaturation;
        this.bloodSugar = bloodSugar;
        this.weight = weight;
        this.height = height;
        this.recordedDate = recordedDate;
        this.recordedTime = recordedTime;
    }

    public String getVitalId() { return vitalId; }
    public void setVitalId(String vitalId) { this.vitalId = vitalId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public String getPulseRate() { return pulseRate; }
    public void setPulseRate(String pulseRate) { this.pulseRate = pulseRate; }

    public String getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(String respiratoryRate) { this.respiratoryRate = respiratoryRate; }

    public String getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(String oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }

    public String getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(String bloodSugar) { this.bloodSugar = bloodSugar; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }

    public String getRecordedDate() { return recordedDate; }
    public void setRecordedDate(String recordedDate) { this.recordedDate = recordedDate; }

    public String getRecordedTime() { return recordedTime; }
    public void setRecordedTime(String recordedTime) { this.recordedTime = recordedTime; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getNurseNotes() { return nurseNotes; }
    public void setNurseNotes(String nurseNotes) { this.nurseNotes = nurseNotes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
