package com.hospital.model;

public class PatientMonitoring {
    private String monitoringId;
    private String patientId;
    private String nurseId;
    private String nurseName;
    private String painLevel;
    private String foodIntake;
    private String waterIntake;
    private String sleepQuality;
    private String urineOutput;
    private String bowelMovement;
    private String generalCondition;
    private String observations;
    private String recordedDate;
    private String recordedTime;
    private String createdAt;

    public PatientMonitoring() {}

    public PatientMonitoring(String monitoringId, String patientId, String nurseId, String nurseName,
                             String painLevel, String foodIntake, String waterIntake, String sleepQuality,
                             String urineOutput, String bowelMovement, String generalCondition,
                             String observations, String recordedDate, String recordedTime) {
        this.monitoringId = monitoringId;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.nurseName = nurseName;
        this.painLevel = painLevel;
        this.foodIntake = foodIntake;
        this.waterIntake = waterIntake;
        this.sleepQuality = sleepQuality;
        this.urineOutput = urineOutput;
        this.bowelMovement = bowelMovement;
        this.generalCondition = generalCondition;
        this.observations = observations;
        this.recordedDate = recordedDate;
        this.recordedTime = recordedTime;
    }

    public String getMonitoringId() { return monitoringId; }
    public void setMonitoringId(String monitoringId) { this.monitoringId = monitoringId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getNurseName() { return nurseName; }
    public void setNurseName(String nurseName) { this.nurseName = nurseName; }

    public String getPainLevel() { return painLevel; }
    public void setPainLevel(String painLevel) { this.painLevel = painLevel; }

    public String getFoodIntake() { return foodIntake; }
    public void setFoodIntake(String foodIntake) { this.foodIntake = foodIntake; }

    public String getWaterIntake() { return waterIntake; }
    public void setWaterIntake(String waterIntake) { this.waterIntake = waterIntake; }

    public String getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(String sleepQuality) { this.sleepQuality = sleepQuality; }

    public String getUrineOutput() { return urineOutput; }
    public void setUrineOutput(String urineOutput) { this.urineOutput = urineOutput; }

    public String getBowelMovement() { return bowelMovement; }
    public void setBowelMovement(String bowelMovement) { this.bowelMovement = bowelMovement; }

    public String getGeneralCondition() { return generalCondition; }
    public void setGeneralCondition(String generalCondition) { this.generalCondition = generalCondition; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getRecordedDate() { return recordedDate; }
    public void setRecordedDate(String recordedDate) { this.recordedDate = recordedDate; }

    public String getRecordedTime() { return recordedTime; }
    public void setRecordedTime(String recordedTime) { this.recordedTime = recordedTime; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
