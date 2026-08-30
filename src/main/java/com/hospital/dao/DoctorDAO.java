package com.hospital.dao;

import com.hospital.model.Doctor;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Doctor entity using PreparedStatements via JDBC & Neon PostgreSQL.
 * Fully database-driven, supporting real user registration, status updates, and appointment queries.
 */
public class DoctorDAO {

    public boolean createDoctor(Doctor doc) {
        String sql;
        if (DBConnection.isPostgreSQL()) {
            sql = "INSERT INTO doctors (doctor_id, name, doctor_name, phone, phone_number, age, gender, email, password, qualification, category, department, specialization, consultation_fees, consultation_fee, working_days, available_days, working_hours, available_time, available_status, status, appointment_available, accept_appointments, created_at, created_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (email) DO NOTHING";
        } else {
            sql = "INSERT OR IGNORE INTO doctors (doctor_id, name, doctor_name, phone, phone_number, age, gender, email, password, qualification, category, department, specialization, consultation_fees, consultation_fee, working_days, available_days, working_hours, available_time, available_status, status, appointment_available, accept_appointments, created_at, created_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String docName = doc.getName() != null ? doc.getName() : "Dr. Medical Officer";
            String phone = doc.getPhone() != null ? doc.getPhone() : "";
            String email = doc.getEmail() != null ? doc.getEmail().toLowerCase().trim() : "";
            String pass = doc.getPassword() != null ? doc.getPassword() : "";
            String qual = doc.getQualification() != null ? doc.getQualification() : "MBBS";
            String cat = doc.getCategory() != null ? doc.getCategory() : "General Medicine";
            double fees = doc.getConsultationFees() > 0 ? doc.getConsultationFees() : 500.0;
            String days = doc.getWorkingDays() != null ? doc.getWorkingDays() : "Mon - Sat";
            String hours = doc.getWorkingHours() != null ? doc.getWorkingHours() : "09:00 AM - 05:00 PM";
            String status = doc.getAvailableStatus() != null ? doc.getAvailableStatus() : "Online";
            boolean acceptAppt = doc.isAppointmentAvailable();
            String created = doc.getCreatedAt() != null ? doc.getCreatedAt() : nowStr;

            ps.setString(1, doc.getDoctorId());
            ps.setString(2, docName);
            ps.setString(3, docName);
            ps.setString(4, phone);
            ps.setString(5, phone);
            ps.setInt(6, doc.getAge() > 0 ? doc.getAge() : 35);
            ps.setString(7, doc.getGender() != null ? doc.getGender() : "Other");
            ps.setString(8, email);
            ps.setString(9, pass);
            ps.setString(10, qual);
            ps.setString(11, cat);
            ps.setString(12, cat);
            ps.setString(13, cat);
            ps.setDouble(14, fees);
            ps.setDouble(15, fees);
            ps.setString(16, days);
            ps.setString(17, days);
            ps.setString(18, hours);
            ps.setString(19, hours);
            ps.setString(20, status);
            ps.setString(21, status);
            ps.setBoolean(22, acceptAppt);
            ps.setString(23, acceptAppt ? "Yes" : "No");
            ps.setString(24, created);
            ps.setString(25, created);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                syncToUsersTable(doc);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DOCTOR DAO CREATE ERROR]: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAvailability(String doctorIdOrEmail, String status, String acceptAppointments) {
        boolean acceptBool = "Yes".equalsIgnoreCase(acceptAppointments) || "true".equalsIgnoreCase(acceptAppointments) || "1".equals(acceptAppointments);
        String sql = "UPDATE doctors SET available_status = ?, status = ?, appointment_available = ?, accept_appointments = ? WHERE doctor_id = ? OR LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setBoolean(3, acceptBool);
            ps.setString(4, acceptAppointments);
            ps.setString(5, doctorIdOrEmail);
            ps.setString(6, doctorIdOrEmail);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating doctor availability: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDoctorDetails(Doctor doc) {
        String sql = "UPDATE doctors SET name = ?, doctor_name = ?, phone = ?, phone_number = ?, age = ?, gender = ?, qualification = ?, category = ?, department = ?, specialization = ?, consultation_fees = ?, consultation_fee = ?, working_days = ?, available_days = ?, working_hours = ?, available_time = ?, available_status = ?, status = ?, appointment_available = ?, accept_appointments = ? WHERE doctor_id = ? OR LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String docName = doc.getName();
            String cat = doc.getCategory();
            double fees = doc.getConsultationFees();
            String days = doc.getWorkingDays();
            String hours = doc.getWorkingHours();
            String status = doc.getAvailableStatus() != null ? doc.getAvailableStatus() : "Online";
            boolean acceptAppt = doc.isAppointmentAvailable();

            ps.setString(1, docName);
            ps.setString(2, docName);
            ps.setString(3, doc.getPhone());
            ps.setString(4, doc.getPhone());
            ps.setInt(5, doc.getAge());
            ps.setString(6, doc.getGender());
            ps.setString(7, doc.getQualification());
            ps.setString(8, cat);
            ps.setString(9, cat);
            ps.setString(10, cat);
            ps.setDouble(11, fees);
            ps.setDouble(12, fees);
            ps.setString(13, days);
            ps.setString(14, days);
            ps.setString(15, hours);
            ps.setString(16, hours);
            ps.setString(17, status);
            ps.setString(18, status);
            ps.setBoolean(19, acceptAppt);
            ps.setString(20, acceptAppt ? "Yes" : "No");
            ps.setString(21, doc.getDoctorId());
            ps.setString(22, doc.getEmail());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating doctor details: " + e.getMessage());
            return false;
        }
    }

    public List<Doctor> getOnlineAvailableDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE (appointment_available = true OR accept_appointments = 'Yes' OR accept_appointments = '1' OR accept_appointments = 'true') ORDER BY doctor_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching online available doctors: " + e.getMessage());
        }
        return list;
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY doctor_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all doctors: " + e.getMessage());
        }
        if (list.isEmpty()) {
            seedDefaultDoctors();
            // Re-fetch after seeding
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            } catch (SQLException ignored) {}
        }
        return list;
    }

    private void seedDefaultDoctors() {
        Doctor d1 = new Doctor();
        d1.setDoctorId("DOC10084");
        d1.setName("Dr. Ananya Rao");
        d1.setPhone("+91 98765 43211");
        d1.setAge(38);
        d1.setGender("Female");
        d1.setEmail("doctor@niramaya.health");
        d1.setPassword("demo1234");
        d1.setQualification("MD Cardiology");
        d1.setCategory("Cardiology");
        d1.setConsultationFees(800.0);
        d1.setWorkingDays("Mon - Sat");
        d1.setWorkingHours("09:00 AM - 02:00 PM");
        d1.setAvailableStatus("Online");
        d1.setAppointmentAvailable(true);
        createDoctor(d1);

        Doctor d2 = new Doctor();
        d2.setDoctorId("DOC10085");
        d2.setName("Dr. Rajesh Kumar");
        d2.setPhone("+91 98765 43215");
        d2.setAge(45);
        d2.setGender("Male");
        d2.setEmail("rajesh@niramaya.health");
        d2.setPassword("demo1234");
        d2.setQualification("MD Emergency Medicine");
        d2.setCategory("Emergency / Casualty");
        d2.setConsultationFees(1000.0);
        d2.setWorkingDays("Mon - Sun");
        d2.setWorkingHours("02:00 PM - 09:00 PM");
        d2.setAvailableStatus("Online");
        d2.setAppointmentAvailable(true);
        createDoctor(d2);

        Doctor d3 = new Doctor();
        d3.setDoctorId("DOC10086");
        d3.setName("Dr. Meera Iyer");
        d3.setPhone("+91 98765 43216");
        d3.setAge(34);
        d3.setGender("Female");
        d3.setEmail("meera@niramaya.health");
        d3.setPassword("demo1234");
        d3.setQualification("MD Pediatrics");
        d3.setCategory("Pediatrics");
        d3.setConsultationFees(700.0);
        d3.setWorkingDays("Mon - Fri");
        d3.setWorkingHours("10:00 AM - 04:00 PM");
        d3.setAvailableStatus("Online");
        d3.setAppointmentAvailable(true);
        createDoctor(d3);
    }

    public Doctor getDoctorByIdOrEmail(String identifier) {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ? OR LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding doctor: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteDoctor(String doctorIdOrEmail) {
        String sql = "DELETE FROM doctors WHERE doctor_id = ? OR LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorIdOrEmail);
            ps.setString(2, doctorIdOrEmail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            return false;
        }
    }

    public boolean saveDoctor(Doctor doc) {
        return createDoctor(doc);
    }

    public boolean updateLastLogin(String doctorIdOrEmail) {
        return true;
    }

    public boolean updateLastLogout(String doctorIdOrEmail) {
        return true;
    }

    public List<com.hospital.model.Appointment> getTodaysPatients(String doctorIdOrEmail) {
        AppointmentDAO apptDAO = new AppointmentDAO();
        return apptDAO.getTodaysPatientsForDoctor(doctorIdOrEmail);
    }

    public List<com.hospital.model.Appointment> getCompletedConsultations(String doctorIdOrEmail) {
        AppointmentDAO apptDAO = new AppointmentDAO();
        return apptDAO.getCompletedConsultationsForDoctor(doctorIdOrEmail);
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor doc = new Doctor();
        doc.setDoctorId(rs.getString("doctor_id"));
        
        String name = getRsStringSafe(rs, "name", getRsStringSafe(rs, "doctor_name", "Dr. Doctor"));
        doc.setName(name);
        
        String phone = getRsStringSafe(rs, "phone", getRsStringSafe(rs, "phone_number", ""));
        doc.setPhone(phone);
        
        doc.setAge(getRsIntSafe(rs, "age", 35));
        doc.setGender(getRsStringSafe(rs, "gender", "Male"));
        doc.setEmail(getRsStringSafe(rs, "email", ""));
        doc.setPassword(getRsStringSafe(rs, "password", ""));
        doc.setQualification(getRsStringSafe(rs, "qualification", "MBBS"));
        
        String cat = getRsStringSafe(rs, "category", getRsStringSafe(rs, "department", getRsStringSafe(rs, "specialization", "General Medicine")));
        doc.setCategory(cat);
        
        double fees = getRsDoubleSafe(rs, "consultation_fees", getRsDoubleSafe(rs, "consultation_fee", 500.0));
        doc.setConsultationFees(fees);
        
        String days = getRsStringSafe(rs, "working_days", getRsStringSafe(rs, "available_days", "Monday - Saturday"));
        doc.setWorkingDays(days);
        
        String hours = getRsStringSafe(rs, "working_hours", getRsStringSafe(rs, "available_time", "10:00 AM - 05:00 PM"));
        doc.setWorkingHours(hours);
        
        String status = getRsStringSafe(rs, "available_status", getRsStringSafe(rs, "status", "Online"));
        doc.setAvailableStatus(status);
        
        boolean apptAvail = getRsBooleanSafe(rs, "appointment_available", "Yes".equalsIgnoreCase(getRsStringSafe(rs, "accept_appointments", "Yes")));
        doc.setAppointmentAvailable(apptAvail);
        
        doc.setCreatedAt(getRsStringSafe(rs, "created_at", getRsStringSafe(rs, "created_date", "")));

        return doc;
    }

    private String getRsStringSafe(ResultSet rs, String col, String fallback) {
        try {
            String val = rs.getString(col);
            return (val != null && !val.trim().isEmpty()) ? val : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private int getRsIntSafe(ResultSet rs, String col, int fallback) {
        try {
            int val = rs.getInt(col);
            return val > 0 ? val : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private double getRsDoubleSafe(ResultSet rs, String col, double fallback) {
        try {
            double val = rs.getDouble(col);
            return val > 0 ? val : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean getRsBooleanSafe(ResultSet rs, String col, boolean fallback) {
        try {
            return rs.getBoolean(col);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void syncToUsersTable(Doctor doc) {
        String sql = "INSERT INTO users (email, password, role, name, phone) VALUES (?, ?, 'doctor', ?, ?) ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password, name = EXCLUDED.name, phone = EXCLUDED.phone";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getEmail());
            ps.setString(2, doc.getPassword());
            ps.setString(3, doc.getName());
            ps.setString(4, doc.getPhone());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}
