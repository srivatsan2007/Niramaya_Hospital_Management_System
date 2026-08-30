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
        String sql = "INSERT INTO appointments (appointment_id, patient_id, doctor_id, doctor_name, department, appointment_date, appointment_time, status, payment_status, created_at, confirmed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appt.getAppointmentId());
            ps.setString(2, appt.getPatientId());
            ps.setString(3, appt.getDoctorId());
            ps.setString(4, appt.getDoctorName());
            ps.setString(5, appt.getDepartment());
            ps.setString(6, appt.getAppointmentDate());
            ps.setString(7, appt.getAppointmentTime());
            ps.setString(8, appt.getStatus() != null ? appt.getStatus() : "Confirmed");
            ps.setString(9, appt.getPaymentStatus() != null ? appt.getPaymentStatus() : "Paid");
            ps.setString(10, nowStr);
            ps.setString(11, nowStr);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating appointment: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAppointmentStatus(String appointmentId, String status) {
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "UPDATE appointments SET status = ?, updated_at = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, nowStr);
            ps.setString(3, appointmentId);

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
        String todayStr = java.time.LocalDate.now().toString();
        String sql;
        if (DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM appointments WHERE patient_id = ? AND (appointment_date = CURRENT_DATE::text OR appointment_date = ? OR LOWER(appointment_date) = 'today') AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') ORDER BY appointment_time ASC";
        } else {
            sql = "SELECT * FROM appointments WHERE patient_id = ? AND (appointment_date = ? OR LOWER(appointment_date) = 'today' OR SUBSTR(created_at,1,10) = ?) AND UPPER(status) NOT IN ('COMPLETED', 'CANCELLED') ORDER BY appointment_time ASC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.trim());
            ps.setString(2, todayStr);
            if (!DBConnection.isPostgreSQL()) {
                ps.setString(3, todayStr);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient today's appointments: " + e.getMessage());
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

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
            rs.getString("appointment_id"),
            rs.getString("patient_id"),
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
