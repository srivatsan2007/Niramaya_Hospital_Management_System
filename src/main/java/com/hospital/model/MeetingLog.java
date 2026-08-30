package com.hospital.model;

/**
 * Meeting Audit Log Model for Telemedicine Module.
 */
public class MeetingLog {
    private String logId;
    private String consultationId;
    private String meetingId;
    private String userId;
    private String userRole;
    private String eventType; // JOINED / LEFT / PRESCRIPTION_SAVED / LAB_ORDERED / ENDED
    private String timestamp;

    public MeetingLog() {}

    public MeetingLog(String logId, String consultationId, String meetingId, String userId,
                      String userRole, String eventType, String timestamp) {
        this.logId = logId;
        this.consultationId = consultationId;
        this.meetingId = meetingId;
        this.userId = userId;
        this.userRole = userRole;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
