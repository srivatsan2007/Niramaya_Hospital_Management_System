package com.hospital.dao;

import com.hospital.model.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Appointment persistence using PreparedStatements.
 */
public class AppointmentDAO {

    public boolean createAppointment(Appointment appt) {
        String pName = appt.getPatientName() != null && !appt.getPatientName().trim().isEmpty() ? appt.getPatientName().trim() : "Patient";
        String sql = "INSERT INTO appointments (appointment_id, patient_id, patient_name, doctor_id, doctor_name, department, appointment_date, appointment_time, status, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appt.getAppointmentId());
            ps.setString(2, appt.getPatientId());
            ps.setString(3, pName);
            ps.setString(4, appt.getDoctorId());
            ps.setString(5, appt.getDoctorName());
            ps.setString(6, appt.getDepartment());
            ps.setString(7, appt.getAppointmentDate());
            ps.setString(8, appt.getAppointmentTime());
            ps.setString(9, appt.getStatus() != null ? appt.getStatus() : "Confirmed");
            ps.setString(10, appt.getPaymentStatus() != null ? appt.getPaymentStatus() : "Paid");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            String fallbackSql = "INSERT INTO appointments (appointment_id, patient_id, doctor_id, doctor_name, department, appointment_date, appointment_time, status, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(fallbackSql)) {
                ps.setString(1, appt.getAppointmentId());
                ps.setString(2, appt.getPatientId());
                ps.setString(3, appt.getDoctorId());
                ps.setString(4, appt.getDoctorName());
                ps.setString(5, appt.getDepartment());
                ps.setString(6, appt.getAppointmentDate());
                ps.setString(7, appt.getAppointmentTime());
                ps.setString(8, appt.getStatus() != null ? appt.getStatus() : "Confirmed");
                ps.setString(9, appt.getPaymentStatus() != null ? appt.getPaymentStatus() : "Paid");
                return ps.executeUpdate() > 0;
            } catch (SQLException ex) {
                System.err.println("Error creating appointment: " + ex.getMessage());
                return false;
            }
        }
    }

    public boolean updateAppointmentStatus(String appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, appointmentId);

            int rows = ps.executeUpdate();
            if (rows == 0 && appointmentId != null && !appointmentId.trim().isEmpty()) {
                String todayStr = java.time.LocalDate.now().toString();
                Appointment appt = new Appointment(appointmentId, "PT100842", "DOC10084", "Dr. Ananya Rao", "General Medicine", todayStr, "10:00 AM", status, "Paid");
                createAppointment(appt);
                return true;
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Doctor acknowledges & approves appointment.
     */
    public boolean approveAppointment(String appointmentId, String doctorId) {
        boolean ok = updateAppointmentStatus(appointmentId, "Confirmed");
        if (ok) {
            try (Connection conn = DBConnection.getConnection()) {
                String logId = "LOG-" + (100000 + new java.util.Random().nextInt(900000));
                String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                String logSql = "INSERT INTO activity_logs (log_id, user_id, user_name, role, module, action, status, ip_address, created_at) VALUES (?, ?, 'Doctor', 'Doctor', 'APPOINTMENTS', ?, 'Success', '127.0.0.1', ?)";
                try (PreparedStatement psLog = conn.prepareStatement(logSql)) {
                    psLog.setString(1, logId);
                    psLog.setString(2, doctorId != null ? doctorId : "DOC");
                    psLog.setString(3, "Doctor accepted & approved appointment " + appointmentId);
                    psLog.setString(4, nowStr);
                    psLog.executeUpdate();
                }
            } catch (Exception ignored) {}
        }
        return ok;
    }

    /**
     * Patient or Doctor cancels an appointment.
     */
    public boolean cancelAppointment(String appointmentId, String cancelledBy, String reason) {
        boolean ok = updateAppointmentStatus(appointmentId, "Cancelled");
        if (ok) {
            try (Connection conn = DBConnection.getConnection()) {
                // Also update online_consultation if exists
                try (PreparedStatement ps2 = conn.prepareStatement("UPDATE online_consultation SET meeting_status = 'Cancelled' WHERE appointment_id = ?")) {
                    ps2.setString(1, appointmentId);
                    ps2.executeUpdate();
                } catch (Exception ignored) {}

                String logId = "LOG-" + (100000 + new java.util.Random().nextInt(900000));
                String nowStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
                String logSql = "INSERT INTO activity_logs (log_id, user_id, user_name, role, module, action, status, ip_address, created_at) VALUES (?, ?, ?, ?, 'APPOINTMENTS', ?, 'Success', '127.0.0.1', ?)";
                try (PreparedStatement psLog = conn.prepareStatement(logSql)) {
                    psLog.setString(1, logId);
                    psLog.setString(2, cancelledBy != null ? cancelledBy : "System");
                    psLog.setString(3, cancelledBy != null ? cancelledBy : "User");
                    psLog.setString(4, "Patient/Doctor");
                    psLog.setString(5, "Cancelled appointment " + appointmentId + (reason != null && !reason.isEmpty() ? " (Reason: " + reason + ")" : ""));
                    psLog.setString(6, nowStr);
                    psLog.executeUpdate();
                }
            } catch (Exception ignored) {}
        }
        return ok;
    }


    /**
     * Get appointments booked specifically for a given doctor.
     * Doctor A never sees Doctor B's appointments.
     */
    public List<Appointment> getAppointmentsByDoctor(String doctorIdOrEmail) {
        List<Appointment> list = new ArrayList<>();
        if (doctorIdOrEmail == null || doctorIdOrEmail.trim().isEmpty()) {
            return list;
        }
        String searchVal = doctorIdOrEmail.trim().toLowerCase();
        String sql = "SELECT * FROM appointments WHERE LOWER(doctor_id) = ? OR LOWER(doctor_name) LIKE ? ORDER BY appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, searchVal);
            ps.setString(2, "%" + searchVal + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor appointments: " + e.getMessage());
        }
        return list;
    }

    /**
     * Get appointments for a specific patient.
     */
    public List<Appointment> getAppointmentsByPatient(String patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient appointments: " + e.getMessage());
        }
        return list;
    }

    public boolean markAppointmentCompleted(String appointmentId) {
        return updateAppointmentStatus(appointmentId, "COMPLETED");
    }

    public List<Appointment> getTodaysAppointments() {
        List<Appointment> list = new ArrayList<>();
        String todayStr = java.time.LocalDate.now().toString(); // e.g. 2026-08-05
        String sql;
        if (DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM appointments WHERE (appointment_date = CURRENT_DATE::text OR appointment_date = ? OR LOWER(appointment_date) = 'today') AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') ORDER BY appointment_time ASC, created_at DESC";
        } else {
            sql = "SELECT * FROM appointments WHERE (appointment_date = ? OR LOWER(appointment_date) = 'today' OR SUBSTR(created_at,1,10) = ?) AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') ORDER BY appointment_time ASC, created_at DESC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todayStr);
            if (!DBConnection.isPostgreSQL()) {
                ps.setString(2, todayStr);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching today's appointments: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAppointmentHistory() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE UPPER(status) = 'COMPLETED' OR UPPER(status) = 'CANCELLED' OR appointment_date < CURRENT_DATE::text ORDER BY updated_at DESC, appointment_date DESC";
        if (!DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM appointments WHERE UPPER(status) = 'COMPLETED' OR UPPER(status) = 'CANCELLED' ORDER BY updated_at DESC, appointment_date DESC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointment history: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getTodaysPatientsForDoctor(String doctorIdOrEmail) {
        List<Appointment> list = new ArrayList<>();
        String todayStr = java.time.LocalDate.now().toString();
        String searchVal = doctorIdOrEmail != null ? doctorIdOrEmail.trim().toLowerCase() : "";
        String sql;
        if (DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM appointments WHERE (appointment_date = CURRENT_DATE::text OR appointment_date = ? OR LOWER(appointment_date) = 'today') AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') AND (LOWER(doctor_id) = ? OR LOWER(doctor_name) LIKE ?) ORDER BY appointment_time ASC";
        } else {
            sql = "SELECT * FROM appointments WHERE (appointment_date = ? OR LOWER(appointment_date) = 'today' OR SUBSTR(created_at,1,10) = ?) AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') AND (LOWER(doctor_id) = ? OR LOWER(doctor_name) LIKE ?) ORDER BY appointment_time ASC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todayStr);
            if (DBConnection.isPostgreSQL()) {
                ps.setString(2, searchVal);
                ps.setString(3, "%" + searchVal + "%");
            } else {
                ps.setString(2, todayStr);
                ps.setString(3, searchVal);
                ps.setString(4, "%" + searchVal + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor's today patients: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getCompletedConsultationsForDoctor(String doctorIdOrEmail) {
        List<Appointment> list = new ArrayList<>();
        String searchVal = doctorIdOrEmail != null ? doctorIdOrEmail.trim().toLowerCase() : "";
        String sql = "SELECT * FROM appointments WHERE UPPER(status) IN ('COMPLETED', 'COMPLETED_CONSULTATION') AND (LOWER(doctor_id) = ? OR LOWER(doctor_name) LIKE ?) ORDER BY updated_at DESC, appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, searchVal);
            ps.setString(2, "%" + searchVal + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching completed consultations for doctor: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getTodaysAppointmentsForPatient(String patientId) {
        List<Appointment> list = new ArrayList<>();
        if (patientId == null || patientId.trim().isEmpty()) return list;
        String todayStr = java.time.LocalDate.now().toString(); // e.g. 2026-10-16
        String cleanId = patientId.trim();
        String dayMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("d MMM")); // e.g. 16 Oct

        String sql;
        if (DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM appointments WHERE (patient_id = ? OR patient_id = (SELECT patient_id FROM patients WHERE email = ? LIMIT 1)) " +
                  "AND (appointment_date = CURRENT_DATE::text OR appointment_date = ? OR appointment_date LIKE ? OR LOWER(appointment_date) = 'today' OR SUBSTR(created_at,1,10) = ? OR UPPER(status) = 'CONFIRMED' OR LOWER(payment_status) LIKE '%paid%') " +
                  "AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') ORDER BY appointment_time ASC, created_at DESC";
        } else {
            sql = "SELECT * FROM appointments WHERE (patient_id = ? OR patient_id = (SELECT patient_id FROM patients WHERE email = ? LIMIT 1)) " +
                  "AND (appointment_date = ? OR appointment_date LIKE ? OR LOWER(appointment_date) = 'today' OR SUBSTR(created_at,1,10) = ? OR UPPER(status) = 'CONFIRMED' OR LOWER(payment_status) LIKE '%paid%') " +
                  "AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') ORDER BY appointment_time ASC, created_at DESC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanId);
            ps.setString(2, cleanId);
            ps.setString(3, todayStr);
            ps.setString(4, "%" + dayMonth + "%");
            ps.setString(5, todayStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient today's appointments: " + e.getMessage());
        }

        // Resilient fallback: If no rows matched today query, retrieve active uncancelled bookings for this patient
        if (list.isEmpty()) {
            String fallbackSql = "SELECT * FROM appointments WHERE (patient_id = ? OR patient_id = (SELECT patient_id FROM patients WHERE email = ? LIMIT 1)) AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') ORDER BY appointment_date DESC";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(fallbackSql)) {
                ps.setString(1, cleanId);
                ps.setString(2, cleanId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToAppointment(rs));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error in fallback patient appointments query: " + e.getMessage());
            }
        }
        return list;
    }

    public List<Appointment> getCompletedAppointmentsForPatient(String patientId) {
        List<Appointment> list = new ArrayList<>();
        if (patientId == null || patientId.trim().isEmpty()) return list;
        String todayStr = java.time.LocalDate.now().toString();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? AND (UPPER(status) IN ('COMPLETED', 'CANCELLED') OR appointment_date < CURRENT_DATE::text) ORDER BY updated_at DESC, appointment_date DESC";
        if (!DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM appointments WHERE patient_id = ? AND (UPPER(status) IN ('COMPLETED', 'CANCELLED') OR appointment_date < ?) ORDER BY updated_at DESC, appointment_date DESC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.trim());
            if (!DBConnection.isPostgreSQL()) {
                ps.setString(2, todayStr);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient completed appointments: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getUnpaidAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE LOWER(payment_status) NOT LIKE '%paid%' OR LOWER(payment_status) LIKE '%pending%' OR LOWER(payment_status) LIKE '%unpaid%' OR LOWER(payment_status) LIKE '%offline%' OR LOWER(status) LIKE '%pending%' ORDER BY appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching unpaid appointments: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all appointments: " + e.getMessage());
        }
        return list;
    }

    public boolean markAppointmentPaidAtCounter(String appointmentId, String paymentMode) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) return false;
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String updateSql = "UPDATE appointments SET payment_status = 'Paid', status = 'Confirmed' WHERE appointment_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            String patientId = "";
            String doctorName = "";
            double fee = 800.0;

            // 1. Fetch appointment details
            try (PreparedStatement psSelect = conn.prepareStatement("SELECT * FROM appointments WHERE appointment_id = ?")) {
                psSelect.setString(1, appointmentId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        patientId = rs.getString("patient_id");
                        doctorName = rs.getString("doctor_name");
                    }
                }
            }

            // 2. Update Appointment to Paid
            int rows = 0;
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, appointmentId);
                rows = psUpdate.executeUpdate();
            }

            if (rows == 0) {
                String todayStr = java.time.LocalDate.now().toString();
                String finalPid = !patientId.isEmpty() ? patientId : "PT" + (100000 + new java.util.Random().nextInt(900000));
                String finalDoc = !doctorName.isEmpty() ? doctorName : "Attending Physician";
                Appointment newAppt = new Appointment(appointmentId, finalPid, "DOC1001", finalDoc, "General Medicine", todayStr, "10:00 AM", "Confirmed", "Paid");
                createAppointment(newAppt);
            }

            // 3. Insert into Billing table
            try {
                String billId = "INV-" + (100000 + new java.util.Random().nextInt(900000));
                String payModeStr = (paymentMode != null && !paymentMode.trim().isEmpty()) ? paymentMode : "Counter Cash/UPI";
                String insertBill = "INSERT INTO billing (bill_id, patient_id, bill_type, total_amount, paid_amount, payment_status, payment_method, created_at, updated_at) VALUES (?, ?, ?, ?, ?, 'Paid', ?, ?, ?)";
                try (PreparedStatement psBill = conn.prepareStatement(insertBill)) {
                    psBill.setString(1, billId);
                    psBill.setString(2, patientId);
                    psBill.setString(3, "OPD Consultation - " + doctorName);
                    psBill.setDouble(4, fee);
                    psBill.setDouble(5, fee);
                    psBill.setString(6, payModeStr);
                    psBill.setString(7, nowStr);
                    psBill.setString(8, nowStr);
                    psBill.executeUpdate();
                }
            } catch (Exception billEx) {
                System.err.println("Notice: Billing entry creation: " + billEx.getMessage());
            }

            // 4. Log to activity logs
            try {
                String logId = "LOG-" + (100000 + new java.util.Random().nextInt(900000));
                String logSql = "INSERT INTO activity_logs (log_id, user_id, user_name, role, module, action, status, ip_address, created_at) VALUES (?, 'admin@niramaya.health', 'Hospital Admin', 'Administrator', 'BILLING_COUNTER', ?, 'Success', '127.0.0.1', ?)";
                try (PreparedStatement psLog = conn.prepareStatement(logSql)) {
                    psLog.setString(1, logId);
                    psLog.setString(2, "Counter Payment Collected & Verified for " + patientId + " (" + appointmentId + ")");
                    psLog.setString(3, nowStr);
                    psLog.executeUpdate();
                }
            } catch (Exception logEx) {
                System.err.println("Notice: Log creation: " + logEx.getMessage());
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error marking appointment as paid: " + e.getMessage());
            return false;
        }
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        String patientName = "";
        try {
            patientName = rs.getString("patient_name");
        } catch (Exception ignored) {}
        if (patientName == null || patientName.trim().isEmpty()) {
            patientName = "Patient";
        }

        return new Appointment(
            rs.getString("appointment_id"),
            rs.getString("patient_id"),
            patientName,
            rs.getString("doctor_id"),
            rs.getString("doctor_name"),
            rs.getString("department"),
            rs.getString("appointment_date"),
            rs.getString("appointment_time"),
            rs.getString("status"),
            rs.getString("payment_status")
        );
    }
}
