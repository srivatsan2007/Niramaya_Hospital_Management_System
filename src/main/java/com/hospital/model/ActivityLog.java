package com.hospital.model;

/**
 * Model representing system activity logs.
 */
public class ActivityLog {
    private String logId;
    private String userId;
    private String userName;
    private String role;
    private String module;
    private String action;
    private String status;
    private String ipAddress;
    private String createdAt;

    public ActivityLog() {}

    public ActivityLog(String logId, String userId, String userName, String role, String module, String action, String status, String ipAddress, String createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.userName = userName;
        this.role = role;
        this.module = module;
        this.action = action;
        this.status = status;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
