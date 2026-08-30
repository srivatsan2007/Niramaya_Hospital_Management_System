package com.hospital.model;

/**
 * Online Consultation Model for Telemedicine Module.
 */
public class OnlineConsultation {
    private String consultationId;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private String department;
    private String meetingId;
    private String meetingRoom;
    private String meetingLink;
    private String appointmentToken;
    private String meetingPassword;
    private String consultationType; // Hospital Visit / Online Consultation
    private String meetingStatus;   // Scheduled / Ongoing / Completed / Cancelled
    private String meetingDate;
    private String meetingTime;
    private String startTime;
    private String endTime;
    private int totalMinutes;
    private String createdAt;

    // Meeting Lifecycle & Timestamps
    private String scheduledStart;
    private String scheduledEnd;
    private String actualStart;
    private String actualEnd;
    private String patientJoinTime;
    private String doctorJoinTime;
    private int durationMinutes;

    public OnlineConsultation() {}

    public OnlineConsultation(String consultationId, String appointmentId, String patientId, String doctorId,
                              String doctorName, String department, String meetingId, String meetingRoom,
                              String meetingLink, String appointmentToken, String meetingPassword,
                              String consultationType, String meetingStatus, String meetingDate,
                              String meetingTime, String startTime, String endTime, int totalMinutes, String createdAt) {
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.department = department;
        this.meetingId = meetingId;
        this.meetingRoom = meetingRoom;
        this.meetingLink = meetingLink;
        this.appointmentToken = appointmentToken;
        this.meetingPassword = meetingPassword;
        this.consultationType = consultationType;
        this.meetingStatus = meetingStatus;
        this.meetingDate = meetingDate;
        this.meetingTime = meetingTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalMinutes = totalMinutes;
        this.durationMinutes = totalMinutes;
        this.createdAt = createdAt;
    }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getMeetingRoom() { return meetingRoom; }
    public void setMeetingRoom(String meetingRoom) { this.meetingRoom = meetingRoom; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getAppointmentToken() { return appointmentToken; }
    public void setAppointmentToken(String appointmentToken) { this.appointmentToken = appointmentToken; }

    public String getMeetingPassword() { return meetingPassword; }
    public void setMeetingPassword(String meetingPassword) { this.meetingPassword = meetingPassword; }

    public String getConsultationType() { return consultationType; }
    public void setConsultationType(String consultationType) { this.consultationType = consultationType; }

    public String getMeetingStatus() { return meetingStatus; }
    public void setMeetingStatus(String meetingStatus) { this.meetingStatus = meetingStatus; }

    public String getMeetingDate() { return meetingDate; }
    public void setMeetingDate(String meetingDate) { this.meetingDate = meetingDate; }

    public String getMeetingTime() { return meetingTime; }
    public void setMeetingTime(String meetingTime) { this.meetingTime = meetingTime; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(int totalMinutes) { this.totalMinutes = totalMinutes; this.durationMinutes = totalMinutes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(String scheduledStart) { this.scheduledStart = scheduledStart; }

    public String getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(String scheduledEnd) { this.scheduledEnd = scheduledEnd; }

    public String getActualStart() { return actualStart != null ? actualStart : startTime; }
    public void setActualStart(String actualStart) { this.actualStart = actualStart; this.startTime = actualStart; }

    public String getActualEnd() { return actualEnd != null ? actualEnd : endTime; }
    public void setActualEnd(String actualEnd) { this.actualEnd = actualEnd; this.endTime = actualEnd; }

    public String getPatientJoinTime() { return patientJoinTime; }
    public void setPatientJoinTime(String patientJoinTime) { this.patientJoinTime = patientJoinTime; }

    public String getDoctorJoinTime() { return doctorJoinTime; }
    public void setDoctorJoinTime(String doctorJoinTime) { this.doctorJoinTime = doctorJoinTime; }

    public int getDurationMinutes() { return durationMinutes > 0 ? durationMinutes : totalMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; this.totalMinutes = durationMinutes; }
}
