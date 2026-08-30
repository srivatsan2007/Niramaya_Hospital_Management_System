package com.hospital.model;

/**
 * Meeting Chat Model for Telemedicine Module.
 */
public class MeetingChat {
    private String chatId;
    private String consultationId;
    private String meetingId;
    private String senderId;
    private String senderName;
    private String senderRole; // Doctor / Patient
    private String message;
    private String timestamp;

    public MeetingChat() {}

    public MeetingChat(String chatId, String consultationId, String meetingId, String senderId,
                       String senderName, String senderRole, String message, String timestamp) {
        this.chatId = chatId;
        this.consultationId = consultationId;
        this.meetingId = meetingId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
