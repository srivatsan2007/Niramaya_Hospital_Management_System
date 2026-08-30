package com.hospital.model;

/**
 * Model representing Daily Activity & Performance Reports sent by Doctor, Nurse, Pharmacy & Lab to Admin.
 */
public class DailyReport {
    private String reportId;
    private String senderRole;
    private String senderId;
    private String senderName;
    private String department;
    private String reportDate;
    private String summaryNotes;
    private int totalPatients;
    private int totalTasksCompleted;
    private int totalPending;
    private double revenueGenerated;
    private String metricsJson;
    private String status;
    private String createdAt;

    public DailyReport() {}

    public DailyReport(String reportId, String senderRole, String senderId, String senderName, String department,
                       String reportDate, String summaryNotes, int totalPatients, int totalTasksCompleted,
                       int totalPending, double revenueGenerated, String metricsJson, String status, String createdAt) {
        this.reportId = reportId;
        this.senderRole = senderRole;
        this.senderId = senderId;
        this.senderName = senderName;
        this.department = department;
        this.reportDate = reportDate;
        this.summaryNotes = summaryNotes;
        this.totalPatients = totalPatients;
        this.totalTasksCompleted = totalTasksCompleted;
        this.totalPending = totalPending;
        this.revenueGenerated = revenueGenerated;
        this.metricsJson = metricsJson;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }

    public String getSummaryNotes() { return summaryNotes; }
    public void setSummaryNotes(String summaryNotes) { this.summaryNotes = summaryNotes; }

    public int getTotalPatients() { return totalPatients; }
    public void setTotalPatients(int totalPatients) { this.totalPatients = totalPatients; }

    public int getTotalTasksCompleted() { return totalTasksCompleted; }
    public void setTotalTasksCompleted(int totalTasksCompleted) { this.totalTasksCompleted = totalTasksCompleted; }

    public int getTotalPending() { return totalPending; }
    public void setTotalPending(int totalPending) { this.totalPending = totalPending; }

    public double getRevenueGenerated() { return revenueGenerated; }
    public void setRevenueGenerated(double revenueGenerated) { this.revenueGenerated = revenueGenerated; }

    public String getMetricsJson() { return metricsJson; }
    public void setMetricsJson(String metricsJson) { this.metricsJson = metricsJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
