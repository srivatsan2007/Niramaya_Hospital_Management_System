package com.hospital.model;

public class DailyNurseTask {
    private String taskId;
    private String nurseId;
    private String patientId;
    private String taskName;
    private String category;
    private String status; // Pending, In Progress, Completed
    private String taskTime;
    private String createdAt;

    public DailyNurseTask() {}

    public DailyNurseTask(String taskId, String nurseId, String patientId, String taskName, String category, String status, String taskTime) {
        this.taskId = taskId;
        this.nurseId = nurseId;
        this.patientId = patientId;
        this.taskName = taskName;
        this.category = category;
        this.status = status;
        this.taskTime = taskTime;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTaskTime() { return taskTime; }
    public void setTaskTime(String taskTime) { this.taskTime = taskTime; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
