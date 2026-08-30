package com.hospital.dao;

import com.hospital.model.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class NurseDAO {

    private boolean isPostgreSQL() {
        return DBConnection.isPostgreSQL();
    }

    // Nurse Authentication & Management
    public boolean createNurse(Nurse nurse) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO nurses (nurse_id, employee_code, name, full_name, gender, dob, phone, phone_number, email, department, qualification, experience_years, shift, joining_date, address, username, password, profile_photo, status, created_at, updated_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (nurse_id) DO UPDATE SET " +
                  "name = EXCLUDED.name, email = EXCLUDED.email, phone = EXCLUDED.phone, department = EXCLUDED.department, password = EXCLUDED.password;";
        } else {
            sql = "INSERT OR REPLACE INTO nurses (nurse_id, employee_code, name, full_name, gender, dob, phone, phone_number, email, department, qualification, experience_years, shift, joining_date, address, username, password, profile_photo, status, created_at, updated_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }

        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nurse.getNurseId());
            ps.setString(2, nurse.getEmployeeCode());
            ps.setString(3, nurse.getName());
            ps.setString(4, nurse.getFullName());
            ps.setString(5, nurse.getGender());
            ps.setString(6, nurse.getDob());
            ps.setString(7, nurse.getPhone());
            ps.setString(8, nurse.getPhoneNumber());
            ps.setString(9, nurse.getEmail());
            ps.setString(10, nurse.getDepartment());
            ps.setString(11, nurse.getQualification());
            ps.setInt(12, nurse.getExperienceYears());
            ps.setString(13, nurse.getShift());
            ps.setString(14, nurse.getJoiningDate());
            ps.setString(15, nurse.getAddress());
            ps.setString(16, nurse.getUsername());
            ps.setString(17, nurse.getPassword());
            ps.setString(18, nurse.getProfilePhoto());
            ps.setString(19, nurse.getStatus() != null ? nurse.getStatus() : "Active");
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            ps.setString(20, nurse.getCreatedAt() != null ? nurse.getCreatedAt() : now);
            ps.setString(21, now);

            int affected = ps.executeUpdate();

            // Also insert user into users table & staff table
            try {
                String uSql = isPostgreSQL() ?
                        "INSERT INTO users (email, password, role, name, phone) VALUES (?, ?, 'nurse', ?, ?) ON CONFLICT (email) DO NOTHING;" :
                        "INSERT OR IGNORE INTO users (email, password, role, name, phone) VALUES (?, ?, 'nurse', ?, ?);";
                try (PreparedStatement ups = conn.prepareStatement(uSql)) {
                    ups.setString(1, nurse.getEmail());
                    ups.setString(2, nurse.getPassword());
                    ups.setString(3, nurse.getName());
                    ups.setString(4, nurse.getPhone());
                    ups.executeUpdate();
                }

                String sSql = isPostgreSQL() ?
                        "INSERT INTO staff (employee_code, role, full_name, email, mobile, department, designation, qualification, experience, status, joining_date) VALUES (?, 'Nurse', ?, ?, ?, ?, 'Staff Nurse', ?, ?, 'Active', ?) ON CONFLICT (employee_code) DO NOTHING;" :
                        "INSERT OR IGNORE INTO staff (employee_code, role, full_name, email, mobile, department, designation, qualification, experience, status, joining_date) VALUES (?, 'Nurse', ?, ?, ?, ?, 'Staff Nurse', ?, ?, 'Active', ?);";
                try (PreparedStatement sps = conn.prepareStatement(sSql)) {
                    sps.setString(1, nurse.getEmployeeCode());
                    sps.setString(2, nurse.getName());
                    sps.setString(3, nurse.getEmail());
                    sps.setString(4, nurse.getPhone());
                    sps.setString(5, nurse.getDepartment());
                    sps.setString(6, nurse.getQualification());
                    sps.setString(7, nurse.getExperienceYears() + " Years");
                    sps.setString(8, nurse.getJoiningDate());
                    sps.executeUpdate();
                }
            } catch (Exception ignored) {}

            return affected > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public Nurse getNurseByEmail(String email) {
        if (email == null) return null;
        String sql = "SELECT * FROM nurses WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapNurse(rs);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return null;
    }

    public Nurse getNurseById(String nurseId) {
        if (nurseId == null) return null;
        String sql = "SELECT * FROM nurses WHERE nurse_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nurseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapNurse(rs);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return null;
    }

    public List<Nurse> getAllNurses() {
        List<Nurse> list = new ArrayList<>();
        String sql = "SELECT * FROM nurses ORDER BY nurse_id ASC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapNurse(rs));
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    public boolean updateNurseProfile(Nurse nurse) {
        String sql = "UPDATE nurses SET name=?, full_name=?, gender=?, dob=?, phone=?, phone_number=?, department=?, qualification=?, experience_years=?, shift=?, address=?, profile_photo=?, updated_at=? WHERE nurse_id=?";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nurse.getName());
            ps.setString(2, nurse.getFullName());
            ps.setString(3, nurse.getGender());
            ps.setString(4, nurse.getDob());
            ps.setString(5, nurse.getPhone());
            ps.setString(6, nurse.getPhoneNumber());
            ps.setString(7, nurse.getDepartment());
            ps.setString(8, nurse.getQualification());
            ps.setInt(9, nurse.getExperienceYears());
            ps.setString(10, nurse.getShift());
            ps.setString(11, nurse.getAddress());
            ps.setString(12, nurse.getProfilePhoto());
            ps.setString(13, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            ps.setString(14, nurse.getNurseId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public boolean changePassword(String email, String newPassword) {
        String sql = "UPDATE nurses SET password=?, updated_at=? WHERE LOWER(email)=LOWER(?)";
        String uSql = "UPDATE users SET password=? WHERE LOWER(email)=LOWER(?)";
        try (Connection conn = DBConnection.getValidatedConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newPassword);
                ps.setString(2, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                ps.setString(3, email.trim());
                ps.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(uSql)) {
                ps2.setString(1, newPassword);
                ps2.setString(2, email.trim());
                ps2.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    // Assigned Patients
    public List<NurseAssignment> getAssignedPatients(String nurseId, String search) {
        List<NurseAssignment> list = new ArrayList<>();
        // Query appointment_assignments table first
        String sqlAppt = "SELECT * FROM appointment_assignments WHERE (nurse_id = ? OR LOWER(nurse_name) LIKE ? OR nurse_id IS NULL OR nurse_id = '') ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAppt)) {
            String nid = nurseId != null ? nurseId.trim() : "";
            ps.setString(1, nid);
            ps.setString(2, "%" + nid.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NurseAssignment a = new NurseAssignment();
                    a.setAssignmentId(rs.getString("assignment_id"));
                    a.setNurseId(rs.getString("nurse_id"));
                    a.setNurseName(rs.getString("nurse_name"));
                    a.setPatientId(rs.getString("patient_id"));
                    a.setPatientName(rs.getString("patient_name"));
                    a.setDoctorName(rs.getString("doctor_name"));
                    a.setWard("OPD / Ward");
                    a.setRoomNumber("Cons-101");
                    a.setBedNumber("N/A");
                    a.setAdmissionDate(rs.getString("appointment_date"));
                    a.setStatus(rs.getString("status"));
                    a.setCreatedAt(rs.getString("created_at"));
                    list.add(a);
                }
            }
        } catch (Exception ignored) {}

        if (list.isEmpty()) {
            StringBuilder sql = new StringBuilder("SELECT * FROM nurse_assignments WHERE 1=1");
            if (nurseId != null && !nurseId.trim().isEmpty()) {
                sql.append(" AND (nurse_id = '").append(nurseId).append("' OR LOWER(nurse_name) LIKE '%").append(nurseId.toLowerCase()).append("%' OR nurse_id IS NULL OR nurse_id = '')");
            }
            if (search != null && !search.trim().isEmpty()) {
                String s = search.trim().toLowerCase();
                sql.append(" AND (LOWER(patient_id) LIKE '%").append(s).append("%' OR LOWER(patient_name) LIKE '%").append(s).append("%' OR LOWER(room_number) LIKE '%").append(s).append("%' OR LOWER(ward) LIKE '%").append(s).append("%')");
            }
            sql.append(" ORDER BY created_at DESC");

            try (Connection conn = DBConnection.getValidatedConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString());
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NurseAssignment a = new NurseAssignment();
                    a.setAssignmentId(rs.getString("assignment_id"));
                    a.setNurseId(rs.getString("nurse_id"));
                    a.setNurseName(rs.getString("nurse_name"));
                    a.setPatientId(rs.getString("patient_id"));
                    a.setPatientName(rs.getString("patient_name"));
                    a.setDoctorName(rs.getString("doctor_name"));
                    a.setWard(rs.getString("ward"));
                    a.setRoomNumber(rs.getString("room_number"));
                    a.setBedNumber(rs.getString("bed_number"));
                    a.setAdmissionDate(rs.getString("admission_date"));
                    a.setStatus(rs.getString("status"));
                    a.setCreatedAt(rs.getString("created_at"));
                    list.add(a);
                }
            } catch (SQLException e) {
                DBConnection.logSQLException(e);
            }
        }
        return list;
    }

    public List<NurseAssignment> getTodaysAssignedPatients(String nurseId) {
        List<NurseAssignment> list = new ArrayList<>();
        String todayStr = java.time.LocalDate.now().toString();
        String sql;
        if (isPostgreSQL()) {
            sql = "SELECT * FROM appointment_assignments WHERE (appointment_date = CURRENT_DATE::text OR appointment_date = ? OR LOWER(appointment_date) = 'today') AND UPPER(status) NOT IN ('VITALS_COMPLETED', 'COMPLETED', 'CANCELLED') ORDER BY created_at DESC";
        } else {
            sql = "SELECT * FROM appointment_assignments WHERE (appointment_date = ? OR LOWER(appointment_date) = 'today' OR SUBSTR(created_at,1,10) = ?) AND UPPER(status) NOT IN ('VITALS_COMPLETED', 'COMPLETED', 'CANCELLED') ORDER BY created_at DESC";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todayStr);
            if (!isPostgreSQL()) ps.setString(2, todayStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NurseAssignment a = new NurseAssignment();
                    a.setAssignmentId(rs.getString("assignment_id"));
                    a.setNurseId(rs.getString("nurse_id"));
                    a.setNurseName(rs.getString("nurse_name"));
                    a.setPatientId(rs.getString("patient_id"));
                    a.setPatientName(rs.getString("patient_name"));
                    a.setDoctorName(rs.getString("doctor_name"));
                    a.setWard("OPD / Ward");
                    a.setRoomNumber("Cons-101");
                    a.setBedNumber("N/A");
                    a.setAdmissionDate(rs.getString("appointment_date"));
                    a.setStatus(rs.getString("status"));
                    a.setCreatedAt(rs.getString("created_at"));
                    list.add(a);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching nurse today patients: " + e.getMessage());
        }
        if (list.isEmpty()) {
            return getAssignedPatients(nurseId, null);
        }
        return list;
    }

    public List<NurseAssignment> getCompletedVitalChecks(String nurseId) {
        List<NurseAssignment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointment_assignments WHERE UPPER(status) IN ('VITALS_COMPLETED', 'COMPLETED') OR UPPER(vital_check_status) = 'COMPLETED' ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NurseAssignment a = new NurseAssignment();
                a.setAssignmentId(rs.getString("assignment_id"));
                a.setNurseId(rs.getString("nurse_id"));
                a.setNurseName(rs.getString("nurse_name"));
                a.setPatientId(rs.getString("patient_id"));
                a.setPatientName(rs.getString("patient_name"));
                a.setDoctorName(rs.getString("doctor_name"));
                a.setWard("OPD / Ward");
                a.setRoomNumber("Cons-101");
                a.setBedNumber("N/A");
                a.setAdmissionDate(rs.getString("appointment_date"));
                a.setStatus(rs.getString("status"));
                a.setCreatedAt(rs.getString("created_at"));
                list.add(a);
            }
        } catch (Exception e) {
            System.err.println("Error fetching nurse completed vital checks: " + e.getMessage());
        }
        return list;
    }

    public boolean completeVitalCheck(String assignmentId, String appointmentId) {
        String sql1 = "UPDATE appointment_assignments SET status = 'VITALS_COMPLETED', vital_check_status = 'Completed' WHERE assignment_id = ? OR appointment_id = ?";
        String sql2 = "UPDATE appointments SET status = 'VITALS_COMPLETED' WHERE appointment_id = ?";
        String sql3 = "UPDATE nurse_assignments SET status = 'VITALS_COMPLETED' WHERE assignment_id = ? OR nurse_id = ?";
        try (Connection conn = DBConnection.getValidatedConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, assignmentId);
                ps.setString(2, appointmentId != null ? appointmentId : assignmentId);
                ps.executeUpdate();
            } catch(Exception ignored){}
            if (appointmentId != null) {
                try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                    ps2.setString(1, appointmentId);
                    ps2.executeUpdate();
                } catch(Exception ignored){}
            }
            try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                ps3.setString(1, assignmentId);
                ps3.setString(2, assignmentId);
                ps3.executeUpdate();
            } catch(Exception ignored){}
            return true;
        } catch (SQLException e) {
            System.err.println("Error completing vital check: " + e.getMessage());
            return false;
        }
    }

    /**
     * Dynamically assigns an available nurse from nurses table to a new appointment.
     */
    public Map<String, String> assignNurseToAppointment(String appointmentId, String patientId, String doctorId, String doctorName, String department, String appointmentDate, String appointmentTime) {
        Map<String, String> result = new HashMap<>();
        String nurseId = "NUR10084";
        String nurseName = "Nurse Priya Sharma";

        String selectNurseSql = "SELECT nurse_id, name, full_name FROM nurses WHERE status = 'Active' ORDER BY RANDOM() LIMIT 1";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(selectNurseSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                nurseId = rs.getString("nurse_id");
                nurseName = rs.getString("name") != null ? rs.getString("name") : rs.getString("full_name");
            }
        } catch (Exception ignored) {}

        String patientName = "Patient " + patientId;
        String patientAge = "30";
        String patientGender = "Other";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name, age, gender FROM patients WHERE patient_id = ? OR email = ? LIMIT 1")) {
            ps.setString(1, patientId);
            ps.setString(2, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    patientName = rs.getString("name");
                    patientAge = String.valueOf(rs.getInt("age"));
                    patientGender = rs.getString("gender");
                }
            }
        } catch (Exception ignored) {}

        String assignmentId = "ASN-" + System.currentTimeMillis();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String sql = isPostgreSQL() ?
            "INSERT INTO appointment_assignments (assignment_id, appointment_id, patient_id, patient_name, patient_age, patient_gender, doctor_id, doctor_name, nurse_id, nurse_name, appointment_time, appointment_date, status, vital_check_status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Assigned', 'Pending', ?) ON CONFLICT (assignment_id) DO NOTHING;" :
            "INSERT OR REPLACE INTO appointment_assignments (assignment_id, appointment_id, patient_id, patient_name, patient_age, patient_gender, doctor_id, doctor_name, nurse_id, nurse_name, appointment_time, appointment_date, status, vital_check_status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Assigned', 'Pending', ?);";

        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assignmentId);
            ps.setString(2, appointmentId);
            ps.setString(3, patientId);
            ps.setString(4, patientName);
            ps.setString(5, patientAge);
            ps.setString(6, patientGender);
            ps.setString(7, doctorId);
            ps.setString(8, doctorName);
            ps.setString(9, nurseId);
            ps.setString(10, nurseName);
            ps.setString(11, appointmentTime);
            ps.setString(12, appointmentDate);
            ps.setString(13, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }

        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(
                 isPostgreSQL() ?
                 "INSERT INTO nurse_assignments (assignment_id, nurse_id, nurse_name, patient_id, patient_name, doctor_name, ward, room_number, bed_number, admission_date, status, created_at) VALUES (?, ?, ?, ?, ?, ?, 'OPD Ward', 'Cons-101', 'N/A', ?, 'Active', ?) ON CONFLICT (assignment_id) DO NOTHING;" :
                 "INSERT OR REPLACE INTO nurse_assignments (assignment_id, nurse_id, nurse_name, patient_id, patient_name, doctor_name, ward, room_number, bed_number, admission_date, status, created_at) VALUES (?, ?, ?, ?, ?, ?, 'OPD Ward', 'Cons-101', 'N/A', ?, 'Active', ?);"
             )) {
            ps.setString(1, assignmentId);
            ps.setString(2, nurseId);
            ps.setString(3, nurseName);
            ps.setString(4, patientId);
            ps.setString(5, patientName);
            ps.setString(6, doctorName);
            ps.setString(7, appointmentDate);
            ps.setString(8, now);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        result.put("assignmentId", assignmentId);
        result.put("nurseId", nurseId);
        result.put("nurseName", nurseName);
        return result;
    }

    // Vital Signs Module
    public boolean recordVitals(PatientVital vital) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO patient_vitals (vital_id, patient_id, nurse_id, nurse_name, temperature, blood_pressure, pulse_rate, respiratory_rate, oxygen_saturation, blood_sugar, weight, height, recorded_date, recorded_time, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (vital_id) DO UPDATE SET " +
                  "temperature=EXCLUDED.temperature, blood_pressure=EXCLUDED.blood_pressure, pulse_rate=EXCLUDED.pulse_rate, oxygen_saturation=EXCLUDED.oxygen_saturation;";
        } else {
            sql = "INSERT OR REPLACE INTO patient_vitals (vital_id, patient_id, nurse_id, nurse_name, temperature, blood_pressure, pulse_rate, respiratory_rate, oxygen_saturation, blood_sugar, weight, height, recorded_date, recorded_time, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = vital.getVitalId() != null ? vital.getVitalId() : "VIT-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, vital.getPatientId());
            ps.setString(3, vital.getNurseId());
            ps.setString(4, vital.getNurseName());
            ps.setString(5, vital.getTemperature());
            ps.setString(6, vital.getBloodPressure());
            ps.setString(7, vital.getPulseRate());
            ps.setString(8, vital.getRespiratoryRate());
            ps.setString(9, vital.getOxygenSaturation());
            ps.setString(10, vital.getBloodSugar());
            ps.setString(11, vital.getWeight());
            ps.setString(12, vital.getHeight());
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String todayTime = new SimpleDateFormat("hh:mm a").format(new Date());
            ps.setString(13, vital.getRecordedDate() != null ? vital.getRecordedDate() : todayDate);
            ps.setString(14, vital.getRecordedTime() != null ? vital.getRecordedTime() : todayTime);
            ps.setString(15, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<PatientVital> getVitalsForPatient(String patientId) {
        List<PatientVital> list = new ArrayList<>();
        String sql = "SELECT * FROM patient_vitals WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientVital v = new PatientVital();
                    v.setVitalId(rs.getString("vital_id"));
                    v.setPatientId(rs.getString("patient_id"));
                    v.setNurseId(rs.getString("nurse_id"));
                    v.setNurseName(rs.getString("nurse_name"));
                    v.setTemperature(rs.getString("temperature"));
                    v.setBloodPressure(rs.getString("blood_pressure"));
                    v.setPulseRate(rs.getString("pulse_rate"));
                    v.setRespiratoryRate(rs.getString("respiratory_rate"));
                    v.setOxygenSaturation(rs.getString("oxygen_saturation"));
                    v.setBloodSugar(rs.getString("blood_sugar"));
                    v.setWeight(rs.getString("weight"));
                    v.setHeight(rs.getString("height"));
                    v.setRecordedDate(rs.getString("recorded_date"));
                    v.setRecordedTime(rs.getString("recorded_time"));
                    v.setCreatedAt(rs.getString("created_at"));
                    list.add(v);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    // Nursing Notes
    public boolean addNursingNote(NursingNote note) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO nursing_notes (note_id, patient_id, nurse_id, nurse_name, observation, note_date, note_time, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (note_id) DO UPDATE SET observation=EXCLUDED.observation;";
        } else {
            sql = "INSERT OR REPLACE INTO nursing_notes (note_id, patient_id, nurse_id, nurse_name, observation, note_date, note_time, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = note.getNoteId() != null ? note.getNoteId() : "NOTE-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, note.getPatientId());
            ps.setString(3, note.getNurseId());
            ps.setString(4, note.getNurseName());
            ps.setString(5, note.getObservation());
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String timeStr = new SimpleDateFormat("hh:mm a").format(new Date());
            ps.setString(6, note.getNoteDate() != null ? note.getNoteDate() : dateStr);
            ps.setString(7, note.getNoteTime() != null ? note.getNoteTime() : timeStr);
            ps.setString(8, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<NursingNote> getNursingNotesForPatient(String patientId) {
        List<NursingNote> list = new ArrayList<>();
        String sql = "SELECT * FROM nursing_notes WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NursingNote n = new NursingNote();
                    n.setNoteId(rs.getString("note_id"));
                    n.setPatientId(rs.getString("patient_id"));
                    n.setNurseId(rs.getString("nurse_id"));
                    n.setNurseName(rs.getString("nurse_name"));
                    n.setObservation(rs.getString("observation"));
                    n.setNoteDate(rs.getString("note_date"));
                    n.setNoteTime(rs.getString("note_time"));
                    n.setCreatedAt(rs.getString("created_at"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    // Medication Administration
    public List<MedicationAdmin> getMedicationAdmins(String patientId) {
        List<MedicationAdmin> list = new ArrayList<>();
        String sql = "SELECT * FROM medication_administration WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MedicationAdmin m = new MedicationAdmin();
                    m.setAdminId(rs.getString("admin_id"));
                    m.setPatientId(rs.getString("patient_id"));
                    m.setPrescriptionId(rs.getString("prescription_id"));
                    m.setMedicineName(rs.getString("medicine_name"));
                    m.setDosage(rs.getString("dosage"));
                    m.setStatus(rs.getString("status"));
                    m.setDosageTime(rs.getString("dosage_time"));
                    m.setMissedReason(rs.getString("missed_reason"));
                    m.setNurseId(rs.getString("nurse_id"));
                    m.setNurseName(rs.getString("nurse_name"));
                    m.setCreatedAt(rs.getString("created_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    public boolean updateMedicationStatus(String adminId, String status, String dosageTime, String missedReason, String nurseId, String nurseName) {
        String sql = "UPDATE medication_administration SET status=?, dosage_time=?, missed_reason=?, nurse_id=?, nurse_name=? WHERE admin_id=?";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, dosageTime);
            ps.setString(3, missedReason);
            ps.setString(4, nurseId);
            ps.setString(5, nurseName);
            ps.setString(6, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public boolean addMedicationAdmin(MedicationAdmin med) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO medication_administration (admin_id, patient_id, prescription_id, medicine_name, dosage, status, dosage_time, missed_reason, nurse_id, nurse_name, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (admin_id) DO UPDATE SET status=EXCLUDED.status, dosage_time=EXCLUDED.dosage_time;";
        } else {
            sql = "INSERT OR REPLACE INTO medication_administration (admin_id, patient_id, prescription_id, medicine_name, dosage, status, dosage_time, missed_reason, nurse_id, nurse_name, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = med.getAdminId() != null ? med.getAdminId() : "ADM-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, med.getPatientId());
            ps.setString(3, med.getPrescriptionId());
            ps.setString(4, med.getMedicineName());
            ps.setString(5, med.getDosage());
            ps.setString(6, med.getStatus() != null ? med.getStatus() : "Pending");
            ps.setString(7, med.getDosageTime());
            ps.setString(8, med.getMissedReason());
            ps.setString(9, med.getNurseId());
            ps.setString(10, med.getNurseName());
            ps.setString(11, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    // Patient Monitoring
    public boolean recordPatientMonitoring(PatientMonitoring mon) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO patient_monitoring (monitoring_id, patient_id, nurse_id, nurse_name, pain_level, food_intake, water_intake, sleep_quality, urine_output, bowel_movement, general_condition, observations, recorded_date, recorded_time, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (monitoring_id) DO UPDATE SET general_condition=EXCLUDED.general_condition;";
        } else {
            sql = "INSERT OR REPLACE INTO patient_monitoring (monitoring_id, patient_id, nurse_id, nurse_name, pain_level, food_intake, water_intake, sleep_quality, urine_output, bowel_movement, general_condition, observations, recorded_date, recorded_time, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = mon.getMonitoringId() != null ? mon.getMonitoringId() : "MON-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, mon.getPatientId());
            ps.setString(3, mon.getNurseId());
            ps.setString(4, mon.getNurseName());
            ps.setString(5, mon.getPainLevel());
            ps.setString(6, mon.getFoodIntake());
            ps.setString(7, mon.getWaterIntake());
            ps.setString(8, mon.getSleepQuality());
            ps.setString(9, mon.getUrineOutput());
            ps.setString(10, mon.getBowelMovement());
            ps.setString(11, mon.getGeneralCondition());
            ps.setString(12, mon.getObservations());
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String todayTime = new SimpleDateFormat("hh:mm a").format(new Date());
            ps.setString(13, mon.getRecordedDate() != null ? mon.getRecordedDate() : todayDate);
            ps.setString(14, mon.getRecordedTime() != null ? mon.getRecordedTime() : todayTime);
            ps.setString(15, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<PatientMonitoring> getPatientMonitoring(String patientId) {
        List<PatientMonitoring> list = new ArrayList<>();
        String sql = "SELECT * FROM patient_monitoring WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientMonitoring m = new PatientMonitoring();
                    m.setMonitoringId(rs.getString("monitoring_id"));
                    m.setPatientId(rs.getString("patient_id"));
                    m.setNurseId(rs.getString("nurse_id"));
                    m.setNurseName(rs.getString("nurse_name"));
                    m.setPainLevel(rs.getString("pain_level"));
                    m.setFoodIntake(rs.getString("food_intake"));
                    m.setWaterIntake(rs.getString("water_intake"));
                    m.setSleepQuality(rs.getString("sleep_quality"));
                    m.setUrineOutput(rs.getString("urine_output"));
                    m.setBowelMovement(rs.getString("bowel_movement"));
                    m.setGeneralCondition(rs.getString("general_condition"));
                    m.setObservations(rs.getString("observations"));
                    m.setRecordedDate(rs.getString("recorded_date"));
                    m.setRecordedTime(rs.getString("recorded_time"));
                    m.setCreatedAt(rs.getString("created_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    // Nurse Shifts
    public boolean recordShift(NurseShift shift) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO nurse_shift (shift_id, nurse_id, nurse_name, shift_type, start_time, end_time, ward, handover_notes, status, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (shift_id) DO UPDATE SET status=EXCLUDED.status, handover_notes=EXCLUDED.handover_notes;";
        } else {
            sql = "INSERT OR REPLACE INTO nurse_shift (shift_id, nurse_id, nurse_name, shift_type, start_time, end_time, ward, handover_notes, status, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = shift.getShiftId() != null ? shift.getShiftId() : "SFT-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, shift.getNurseId());
            ps.setString(3, shift.getNurseName());
            ps.setString(4, shift.getShiftType());
            ps.setString(5, shift.getStartTime());
            ps.setString(6, shift.getEndTime());
            ps.setString(7, shift.getWard());
            ps.setString(8, shift.getHandoverNotes());
            ps.setString(9, shift.getStatus() != null ? shift.getStatus() : "Active");
            ps.setString(10, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<NurseShift> getShiftHistory(String nurseId) {
        List<NurseShift> list = new ArrayList<>();
        String sql = "SELECT * FROM nurse_shift WHERE nurse_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nurseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NurseShift s = new NurseShift();
                    s.setShiftId(rs.getString("shift_id"));
                    s.setNurseId(rs.getString("nurse_id"));
                    s.setNurseName(rs.getString("nurse_name"));
                    s.setShiftType(rs.getString("shift_type"));
                    s.setStartTime(rs.getString("start_time"));
                    s.setEndTime(rs.getString("end_time"));
                    s.setWard(rs.getString("ward"));
                    s.setHandoverNotes(rs.getString("handover_notes"));
                    s.setStatus(rs.getString("status"));
                    s.setCreatedAt(rs.getString("created_at"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    // Injections
    public boolean recordInjection(InjectionRecord inj) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO injection_records (injection_id, patient_id, nurse_id, nurse_name, injection_name, dose, route, record_date, record_time, remarks, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (injection_id) DO NOTHING;";
        } else {
            sql = "INSERT OR REPLACE INTO injection_records (injection_id, patient_id, nurse_id, nurse_name, injection_name, dose, route, record_date, record_time, remarks, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = inj.getInjectionId() != null ? inj.getInjectionId() : "INJ-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, inj.getPatientId());
            ps.setString(3, inj.getNurseId());
            ps.setString(4, inj.getNurseName());
            ps.setString(5, inj.getInjectionName());
            ps.setString(6, inj.getDose());
            ps.setString(7, inj.getRoute());
            String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String todayTime = new SimpleDateFormat("hh:mm a").format(new Date());
            ps.setString(8, inj.getRecordDate() != null ? inj.getRecordDate() : todayDate);
            ps.setString(9, inj.getRecordTime() != null ? inj.getRecordTime() : todayTime);
            ps.setString(10, inj.getRemarks());
            ps.setString(11, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<InjectionRecord> getInjectionRecords(String patientId) {
        List<InjectionRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM injection_records WHERE patient_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InjectionRecord i = new InjectionRecord();
                    i.setInjectionId(rs.getString("injection_id"));
                    i.setPatientId(rs.getString("patient_id"));
                    i.setNurseId(rs.getString("nurse_id"));
                    i.setNurseName(rs.getString("nurse_name"));
                    i.setInjectionName(rs.getString("injection_name"));
                    i.setDose(rs.getString("dose"));
                    i.setRoute(rs.getString("route"));
                    i.setRecordDate(rs.getString("record_date"));
                    i.setRecordTime(rs.getString("record_time"));
                    i.setRemarks(rs.getString("remarks"));
                    i.setCreatedAt(rs.getString("created_at"));
                    list.add(i);
                }
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    // Inventory Requests
    public boolean createInventoryRequest(InventoryRequest req) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO inventory_requests (request_id, nurse_id, nurse_name, item_name, quantity, status, request_date, remarks, approved_by, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (request_id) DO NOTHING;";
        } else {
            sql = "INSERT OR REPLACE INTO inventory_requests (request_id, nurse_id, nurse_name, item_name, quantity, status, request_date, remarks, approved_by, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = req.getRequestId() != null ? req.getRequestId() : "REQ-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, req.getNurseId());
            ps.setString(3, req.getNurseName());
            ps.setString(4, req.getItemName());
            ps.setInt(5, req.getQuantity() > 0 ? req.getQuantity() : 1);
            ps.setString(6, req.getStatus() != null ? req.getStatus() : "Pending");
            ps.setString(7, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            ps.setString(8, req.getRemarks());
            ps.setString(9, req.getApprovedBy());
            ps.setString(10, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<InventoryRequest> getAllInventoryRequests() {
        List<InventoryRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_requests ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventoryRequest r = new InventoryRequest();
                r.setRequestId(rs.getString("request_id"));
                r.setNurseId(rs.getString("nurse_id"));
                r.setNurseName(rs.getString("nurse_name"));
                r.setItemName(rs.getString("item_name"));
                r.setQuantity(rs.getInt("quantity"));
                r.setStatus(rs.getString("status"));
                r.setRequestDate(rs.getString("request_date"));
                r.setRemarks(rs.getString("remarks"));
                r.setApprovedBy(rs.getString("approved_by"));
                r.setCreatedAt(rs.getString("created_at"));
                list.add(r);
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    public boolean updateInventoryRequestStatus(String requestId, String status, String approvedBy) {
        String sql = "UPDATE inventory_requests SET status=?, approved_by=? WHERE request_id=?";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, approvedBy);
            ps.setString(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    // Emergency Alerts
    public boolean createEmergencyAlert(EmergencyAlert alert) {
        String sql;
        if (isPostgreSQL()) {
            sql = "INSERT INTO emergency_alerts (alert_id, patient_id, patient_name, room_number, ward, nurse_id, nurse_name, alert_type, alert_time, status, resolved_by, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (alert_id) DO NOTHING;";
        } else {
            sql = "INSERT OR REPLACE INTO emergency_alerts (alert_id, patient_id, patient_name, room_number, ward, nurse_id, nurse_name, alert_type, alert_time, status, resolved_by, created_at) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = alert.getAlertId() != null ? alert.getAlertId() : "EMG-" + System.currentTimeMillis();
            ps.setString(1, id);
            ps.setString(2, alert.getPatientId());
            ps.setString(3, alert.getPatientName());
            ps.setString(4, alert.getRoomNumber());
            ps.setString(5, alert.getWard());
            ps.setString(6, alert.getNurseId());
            ps.setString(7, alert.getNurseName());
            ps.setString(8, alert.getAlertType());
            ps.setString(9, alert.getAlertTime() != null ? alert.getAlertTime() : new SimpleDateFormat("hh:mm a").format(new Date()));
            ps.setString(10, "Active");
            ps.setString(11, alert.getResolvedBy());
            ps.setString(12, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            // Send system notification
            try {
                String nSql = isPostgreSQL() ?
                        "INSERT INTO notifications (id, title, message, type, timestamp, is_read, created_at) VALUES (?, ?, ?, 'EMERGENCY', ?, 0, ?) ON CONFLICT (id) DO NOTHING;" :
                        "INSERT OR IGNORE INTO notifications (id, title, message, type, timestamp, is_read, created_at) VALUES (?, ?, ?, 'EMERGENCY', ?, 0, ?);";
                try (PreparedStatement nps = conn.prepareStatement(nSql)) {
                    nps.setString(1, "NOTIF-" + System.currentTimeMillis());
                    nps.setString(2, "🚨 EMERGENCY ALERT: " + alert.getAlertType());
                    nps.setString(3, "Patient: " + alert.getPatientName() + " | Room: " + alert.getRoomNumber() + " (" + alert.getWard() + ")");
                    nps.setString(4, new SimpleDateFormat("hh:mm a").format(new Date()));
                    nps.setString(5, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    nps.executeUpdate();
                }
            } catch (Exception ignored) {}

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    public List<EmergencyAlert> getActiveEmergencyAlerts() {
        List<EmergencyAlert> list = new ArrayList<>();
        String sql = "SELECT * FROM emergency_alerts WHERE status = 'Active' ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                EmergencyAlert a = new EmergencyAlert();
                a.setAlertId(rs.getString("alert_id"));
                a.setPatientId(rs.getString("patient_id"));
                a.setPatientName(rs.getString("patient_name"));
                a.setRoomNumber(rs.getString("room_number"));
                a.setWard(rs.getString("ward"));
                a.setNurseId(rs.getString("nurse_id"));
                a.setNurseName(rs.getString("nurse_name"));
                a.setAlertType(rs.getString("alert_type"));
                a.setAlertTime(rs.getString("alert_time"));
                a.setStatus(rs.getString("status"));
                a.setResolvedBy(rs.getString("resolved_by"));
                a.setCreatedAt(rs.getString("created_at"));
                list.add(a);
            }
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
        }
        return list;
    }

    public boolean resolveEmergencyAlert(String alertId, String resolvedBy) {
        String sql = "UPDATE emergency_alerts SET status='Resolved', resolved_by=? WHERE alert_id=?";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resolvedBy);
            ps.setString(2, alertId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            DBConnection.logSQLException(e);
            return false;
        }
    }

    // Helper mapper
    private Nurse mapNurse(ResultSet rs) throws SQLException {
        Nurse n = new Nurse();
        n.setNurseId(rs.getString("nurse_id"));
        n.setEmployeeCode(rs.getString("employee_code"));
        n.setName(rs.getString("name"));
        n.setFullName(rs.getString("full_name"));
        n.setGender(rs.getString("gender"));
        n.setDob(rs.getString("dob"));
        n.setPhone(rs.getString("phone"));
        n.setPhoneNumber(rs.getString("phone_number"));
        n.setEmail(rs.getString("email"));
        n.setDepartment(rs.getString("department"));
        n.setQualification(rs.getString("qualification"));
        n.setExperienceYears(rs.getInt("experience_years"));
        n.setShift(rs.getString("shift"));
        n.setJoiningDate(rs.getString("joining_date"));
        n.setAddress(rs.getString("address"));
        n.setUsername(rs.getString("username"));
        n.setPassword(rs.getString("password"));
        n.setProfilePhoto(rs.getString("profile_photo"));
        n.setStatus(rs.getString("status"));
        n.setCreatedAt(rs.getString("created_at"));
        n.setUpdatedAt(rs.getString("updated_at"));
        return n;
    }
}
