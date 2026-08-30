package com.hospital.dao;

import com.hospital.model.OnlineConsultation;
import com.hospital.model.ConsultationNotes;
import com.hospital.model.MeetingChat;
import com.hospital.model.MeetingLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Telemedicine & Online Consultation using PreparedStatements.
 * Ensures robust persistence in MySQL / SQLite.
 */
public class OnlineConsultationDAO {

    private static final java.util.Map<String, OnlineConsultation> memoryCache = new java.util.concurrent.ConcurrentHashMap<>();

    public boolean createConsultation(OnlineConsultation c) {
        if (c != null && c.getMeetingId() != null) {
            memoryCache.put(c.getMeetingId(), c);
            if (c.getConsultationId() != null) memoryCache.put(c.getConsultationId(), c);
            if (c.getAppointmentId() != null) memoryCache.put(c.getAppointmentId(), c);
        }

        String sql = "INSERT INTO online_consultation (consultation_id, appointment_id, patient_id, doctor_id, doctor_name, department, meeting_id, meeting_room, meeting_link, appointment_token, meeting_password, consultation_type, meeting_status, meeting_date, meeting_time, scheduled_start, scheduled_end, actual_start, actual_end, start_time, end_time, patient_join_time, doctor_join_time, duration_minutes, total_minutes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getConsultationId());
            ps.setString(2, c.getAppointmentId());
            ps.setString(3, c.getPatientId());
            ps.setString(4, c.getDoctorId());
            ps.setString(5, c.getDoctorName());
            ps.setString(6, c.getDepartment());
            ps.setString(7, c.getMeetingId());
            ps.setString(8, c.getMeetingRoom());
            ps.setString(9, c.getMeetingLink());
            ps.setString(10, c.getAppointmentToken());
            ps.setString(11, c.getMeetingPassword());
            ps.setString(12, c.getConsultationType() != null ? c.getConsultationType() : "Online Consultation");
            ps.setString(13, c.getMeetingStatus() != null ? c.getMeetingStatus() : "Scheduled");
            ps.setString(14, c.getMeetingDate());
            ps.setString(15, c.getMeetingTime());
            ps.setString(16, c.getScheduledStart());
            ps.setString(17, c.getScheduledEnd());
            ps.setString(18, c.getActualStart());
            ps.setString(19, c.getActualEnd());
            ps.setString(20, c.getStartTime());
            ps.setString(21, c.getEndTime());
            ps.setString(22, c.getPatientJoinTime());
            ps.setString(23, c.getDoctorJoinTime());
            ps.setInt(24, c.getDurationMinutes());
            ps.setInt(25, c.getTotalMinutes());
            ps.setString(26, c.getCreatedAt());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating OnlineConsultation in DB (using memory fallback): " + e.getMessage());
            return true;
        }
    }

    public void autoUpdateMeetingStatuses() {
        String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        try (Connection conn = DBConnection.getConnection()) {
            // 1. Mark expired meetings (current time > scheduled_end + 30 minutes or scheduled_start + 45 minutes)
            List<OnlineConsultation> activeMeetings = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM online_consultation WHERE meeting_status IN ('Scheduled', 'Waiting', 'In Progress')")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        activeMeetings.add(mapResultSetToConsultation(rs));
                    }
                }
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long nowMillis = System.currentTimeMillis();

            for (OnlineConsultation c : activeMeetings) {
                boolean isExpired = false;
                String endStr = c.getScheduledEnd();
                String startStr = c.getScheduledStart();

                if (endStr != null && !endStr.isEmpty()) {
                    try {
                        long endMillis = sdf.parse(endStr).getTime();
                        if (nowMillis > endMillis + (30 * 60 * 1000L)) {
                            isExpired = true;
                        }
                    } catch (Exception ignored) {}
                } else if (startStr != null && !startStr.isEmpty()) {
                    try {
                        long startMillis = sdf.parse(startStr).getTime();
                        if (nowMillis > startMillis + (45 * 60 * 1000L)) {
                            isExpired = true;
                        }
                    } catch (Exception ignored) {}
                }

                if (isExpired) {
                    try (PreparedStatement psUp = conn.prepareStatement("UPDATE online_consultation SET meeting_status = 'Expired' WHERE meeting_id = ?")) {
                        psUp.setString(1, c.getMeetingId());
                        psUp.executeUpdate();
                    }
                }
            }

            // 2. Mark cancelled meetings if associated appointment was cancelled
            try (PreparedStatement psCancel = conn.prepareStatement(
                    "UPDATE online_consultation SET meeting_status = 'Cancelled' WHERE appointment_id IN (SELECT appointment_id FROM appointments WHERE status = 'Cancelled') AND meeting_status != 'Cancelled'")) {
                psCancel.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error in autoUpdateMeetingStatuses: " + e.getMessage());
        }
    }

    public OnlineConsultation getConsultationByMeetingId(String meetingId) {
        autoUpdateMeetingStatuses();
        String sql = "SELECT * FROM online_consultation WHERE meeting_id = ? OR consultation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meetingId);
            ps.setString(2, meetingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OnlineConsultation c = mapResultSetToConsultation(rs);
                    if (c != null) memoryCache.put(meetingId, c);
                    return c;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching consultation by meetingId: " + e.getMessage());
        }
        return memoryCache.get(meetingId);
    }

    public OnlineConsultation getConsultationByAppointmentId(String apptId) {
        autoUpdateMeetingStatuses();
        String sql = "SELECT * FROM online_consultation WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, apptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OnlineConsultation c = mapResultSetToConsultation(rs);
                    if (c != null) memoryCache.put(apptId, c);
                    return c;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching consultation by apptId: " + e.getMessage());
        }
        return memoryCache.get(apptId);
    }

    public List<OnlineConsultation> getConsultationsByPatient(String patientId) {
        autoUpdateMeetingStatuses();
        List<OnlineConsultation> list = new ArrayList<>();
        String sql = "SELECT * FROM online_consultation WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToConsultation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient consultations: " + e.getMessage());
        }
        if (list.isEmpty()) {
            for (OnlineConsultation c : memoryCache.values()) {
                if (patientId.equalsIgnoreCase(c.getPatientId()) && !list.contains(c)) {
                    list.add(c);
                }
            }
        }
        return list;
    }

    public List<OnlineConsultation> getConsultationsByDoctor(String doctorIdOrEmail) {
        autoUpdateMeetingStatuses();
        List<OnlineConsultation> list = new ArrayList<>();
        String sql = "SELECT * FROM online_consultation WHERE doctor_id = ? OR doctor_name LIKE ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorIdOrEmail);
            ps.setString(2, "%" + doctorIdOrEmail + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToConsultation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor consultations: " + e.getMessage());
        }
        if (list.isEmpty()) {
            for (OnlineConsultation c : memoryCache.values()) {
                if ((doctorIdOrEmail.equalsIgnoreCase(c.getDoctorId()) || c.getDoctorName().toLowerCase().contains(doctorIdOrEmail.toLowerCase())) && !list.contains(c)) {
                    list.add(c);
                }
            }
        }
        return list;
    }

    public boolean updateMeetingStatus(String meetingId, String status) {
        OnlineConsultation c = memoryCache.get(meetingId);
        if (c != null) {
            c.setMeetingStatus(status);
        }

        String sql = "UPDATE online_consultation SET meeting_status = ? WHERE meeting_id = ? OR consultation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, meetingId);
            ps.setString(3, meetingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating meeting status: " + e.getMessage());
            return c != null;
        }
    }

    public boolean startConsultation(String meetingId) {
        String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        OnlineConsultation c = memoryCache.get(meetingId);
        if (c != null) {
            c.setMeetingStatus("In Progress");
            c.setActualStart(nowStr);
            c.setStartTime(nowStr);
            c.setDoctorJoinTime(nowStr);
        }

        String sql = "UPDATE online_consultation SET meeting_status = 'In Progress', actual_start = ?, start_time = ?, doctor_join_time = ? WHERE meeting_id = ? OR consultation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, nowStr);
            ps.setString(3, nowStr);
            ps.setString(4, meetingId);
            ps.setString(5, meetingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error starting consultation: " + e.getMessage());
            return c != null;
        }
    }

    public boolean logPatientJoin(String meetingId) {
        String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        OnlineConsultation c = memoryCache.get(meetingId);
        if (c != null) {
            c.setPatientJoinTime(nowStr);
            if ("Scheduled".equalsIgnoreCase(c.getMeetingStatus())) {
                c.setMeetingStatus("Waiting");
            }
        }

        String sql = "UPDATE online_consultation SET patient_join_time = ?, meeting_status = CASE WHEN meeting_status = 'Scheduled' THEN 'Waiting' ELSE meeting_status END WHERE meeting_id = ? OR consultation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, meetingId);
            ps.setString(3, meetingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging patient join: " + e.getMessage());
            return c != null;
        }
    }

    public boolean endConsultation(String meetingId, String startTime, String endTime, int totalMinutes) {
        String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        OnlineConsultation c = memoryCache.get(meetingId);
        if (c != null) {
            c.setMeetingStatus("Completed");
            c.setActualEnd(nowStr);
            c.setEndTime(endTime != null && !endTime.isEmpty() ? endTime : nowStr);
            c.setDurationMinutes(totalMinutes);
            c.setTotalMinutes(totalMinutes);
        }

        String sql = "UPDATE online_consultation SET meeting_status = 'Completed', actual_end = ?, end_time = ?, duration_minutes = ?, total_minutes = ? WHERE meeting_id = ? OR consultation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nowStr);
            ps.setString(2, endTime != null && !endTime.isEmpty() ? endTime : nowStr);
            ps.setInt(3, totalMinutes);
            ps.setInt(4, totalMinutes);
            ps.setString(5, meetingId);
            ps.setString(6, meetingId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                if (c != null && c.getAppointmentId() != null) {
                    try (PreparedStatement psAppt = conn.prepareStatement("UPDATE appointments SET status = 'Completed' WHERE appointment_id = ?")) {
                        psAppt.setString(1, c.getAppointmentId());
                        psAppt.executeUpdate();
                    }
                }
            }
            return updated > 0 || c != null;
        } catch (SQLException e) {
            System.err.println("Error ending consultation: " + e.getMessage());
            return c != null;
        }
    }

    public boolean saveConsultationNotes(ConsultationNotes notes) {
        String sql = "INSERT INTO consultation_notes (note_id, consultation_id, appointment_id, patient_id, doctor_id, consultation_summary, diagnosis, advice, follow_up_date, medical_certificate_required, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notes.getNoteId());
            ps.setString(2, notes.getConsultationId());
            ps.setString(3, notes.getAppointmentId());
            ps.setString(4, notes.getPatientId());
            ps.setString(5, notes.getDoctorId());
            ps.setString(6, notes.getConsultationSummary());
            ps.setString(7, notes.getDiagnosis());
            ps.setString(8, notes.getAdvice());
            ps.setString(9, notes.getFollowUpDate());
            ps.setString(10, notes.getMedicalCertificateRequired());
            ps.setString(11, notes.getCreatedAt());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving consultation notes: " + e.getMessage());
            return false;
        }
    }

    public ConsultationNotes getConsultationNotes(String consultationIdOrApptId) {
        String sql = "SELECT * FROM consultation_notes WHERE consultation_id = ? OR appointment_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, consultationIdOrApptId);
            ps.setString(2, consultationIdOrApptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ConsultationNotes(
                        rs.getString("note_id"),
                        rs.getString("consultation_id"),
                        rs.getString("appointment_id"),
                        rs.getString("patient_id"),
                        rs.getString("doctor_id"),
                        rs.getString("consultation_summary"),
                        rs.getString("diagnosis"),
                        rs.getString("advice"),
                        rs.getString("follow_up_date"),
                        rs.getString("medical_certificate_required"),
                        rs.getString("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching consultation notes: " + e.getMessage());
        }
        return null;
    }

    public boolean saveChatMessage(MeetingChat chat) {
        String sql = "INSERT INTO meeting_chat (chat_id, consultation_id, meeting_id, sender_id, sender_name, sender_role, message, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chat.getChatId());
            ps.setString(2, chat.getConsultationId());
            ps.setString(3, chat.getMeetingId());
            ps.setString(4, chat.getSenderId());
            ps.setString(5, chat.getSenderName());
            ps.setString(6, chat.getSenderRole());
            ps.setString(7, chat.getMessage());
            ps.setString(8, chat.getTimestamp());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving meeting chat: " + e.getMessage());
            return false;
        }
    }

    public List<MeetingChat> getChatMessages(String meetingId) {
        List<MeetingChat> list = new ArrayList<>();
        String sql = "SELECT * FROM meeting_chat WHERE meeting_id = ? OR consultation_id = ? ORDER BY timestamp ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meetingId);
            ps.setString(2, meetingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MeetingChat(
                        rs.getString("chat_id"),
                        rs.getString("consultation_id"),
                        rs.getString("meeting_id"),
                        rs.getString("sender_id"),
                        rs.getString("sender_name"),
                        rs.getString("sender_role"),
                        rs.getString("message"),
                        rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching chat messages: " + e.getMessage());
        }
        return list;
    }

    public boolean logEvent(MeetingLog log) {
        String sql = "INSERT INTO meeting_logs (log_id, consultation_id, meeting_id, user_id, user_role, event_type, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getLogId());
            ps.setString(2, log.getConsultationId());
            ps.setString(3, log.getMeetingId());
            ps.setString(4, log.getUserId());
            ps.setString(5, log.getUserRole());
            ps.setString(6, log.getEventType());
            ps.setString(7, log.getTimestamp());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error logging meeting event: " + e.getMessage());
            return false;
        }
    }

    private OnlineConsultation mapResultSetToConsultation(ResultSet rs) throws SQLException {
        OnlineConsultation c = new OnlineConsultation(
            rs.getString("consultation_id"),
            rs.getString("appointment_id"),
            rs.getString("patient_id"),
            rs.getString("doctor_id"),
            rs.getString("doctor_name"),
            rs.getString("department"),
            rs.getString("meeting_id"),
            rs.getString("meeting_room"),
            rs.getString("meeting_link"),
            rs.getString("appointment_token"),
            rs.getString("meeting_password"),
            rs.getString("consultation_type"),
            rs.getString("meeting_status"),
            rs.getString("meeting_date"),
            rs.getString("meeting_time"),
            rs.getString("start_time"),
            rs.getString("end_time"),
            rs.getInt("total_minutes"),
            rs.getString("created_at")
        );

        try { c.setScheduledStart(rs.getString("scheduled_start")); } catch (Exception ignored) {}
        try { c.setScheduledEnd(rs.getString("scheduled_end")); } catch (Exception ignored) {}
        try { c.setActualStart(rs.getString("actual_start")); } catch (Exception ignored) {}
        try { c.setActualEnd(rs.getString("actual_end")); } catch (Exception ignored) {}
        try { c.setPatientJoinTime(rs.getString("patient_join_time")); } catch (Exception ignored) {}
        try { c.setDoctorJoinTime(rs.getString("doctor_join_time")); } catch (Exception ignored) {}
        try { c.setDurationMinutes(rs.getInt("duration_minutes")); } catch (Exception ignored) {}

        return c;
    }
}
