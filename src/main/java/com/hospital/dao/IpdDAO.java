package com.hospital.dao;

import com.hospital.model.IpdBed;
import com.hospital.model.IpdAdmission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Hospital In-Patient Department (IPD) Bed Allocations and Inpatient Admissions.
 */
public class IpdDAO {

    // --- BEDS MANAGEMENT ---

    public boolean createBed(IpdBed bed) {
        String sql = "INSERT INTO ipd_beds (bed_id, block_name, ward_type, room_number, bed_number, daily_charge, status, current_admission_id, current_patient_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bed.getBedId());
            ps.setString(2, bed.getBlockName());
            ps.setString(3, bed.getWardType());
            ps.setString(4, bed.getRoomNumber());
            ps.setString(5, bed.getBedNumber());
            ps.setDouble(6, bed.getDailyCharge());
            ps.setString(7, bed.getStatus() != null ? bed.getStatus() : "Available");
            ps.setString(8, bed.getCurrentAdmissionId());
            ps.setString(9, bed.getCurrentPatientName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating IPD bed: " + e.getMessage());
            return false;
        }
    }

    public List<IpdBed> getAllBeds() {
        List<IpdBed> list = new ArrayList<>();
        // Authoritative query joining with active admissions and patient records to ensure 100% Admin-Receptionist consistency
        String sql = "SELECT b.bed_id, b.block_name, b.ward_type, b.room_number, b.bed_number, b.daily_charge, " +
                     "CASE " +
                     "  WHEN b.status = 'Under Maintenance' THEN 'Under Maintenance' " +
                     "  WHEN a.status = 'Admitted' THEN 'Occupied' " +
                     "  WHEN b.status = 'Occupied' AND a.admission_id IS NULL THEN 'Available' " +
                     "  ELSE b.status " +
                     "END AS live_status, " +
                     "CASE " +
                     "  WHEN b.status = 'Under Maintenance' THEN NULL " +
                     "  WHEN a.status = 'Admitted' THEN a.admission_id " +
                     "  ELSE NULL " +
                     "END AS live_admission_id, " +
                     "CASE " +
                     "  WHEN b.status = 'Under Maintenance' THEN NULL " +
                     "  WHEN a.status = 'Admitted' THEN COALESCE(p.name, a.patient_name, b.current_patient_name) " +
                     "  ELSE NULL " +
                     "END AS live_patient_name, " +
                     "CASE " +
                     "  WHEN b.status = 'Under Maintenance' THEN NULL " +
                     "  WHEN a.status = 'Admitted' THEN COALESCE(a.patient_id, p.patient_id) " +
                     "  ELSE NULL " +
                     "END AS live_patient_id " +
                     "FROM ipd_beds b " +
                     "LEFT JOIN ipd_admissions a ON b.bed_id = a.bed_id AND a.status = 'Admitted' " +
                     "LEFT JOIN patients p ON a.patient_id = p.patient_id " +
                     "ORDER BY b.block_name ASC, b.room_number ASC, b.bed_number ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new IpdBed(
                        rs.getString("bed_id"),
                        rs.getString("block_name"),
                        rs.getString("ward_type"),
                        rs.getString("room_number"),
                        rs.getString("bed_number"),
                        rs.getDouble("daily_charge"),
                        rs.getString("live_status") != null ? rs.getString("live_status") : "Available",
                        rs.getString("live_admission_id"),
                        rs.getString("live_patient_name"),
                        rs.getString("live_patient_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching authoritative IPD beds: " + e.getMessage());
        }
        return list;
    }

    public boolean updateBedStatus(String bedId, String status, String currentAdmissionId, String currentPatientName) {
        if ("Under Maintenance".equalsIgnoreCase(status)) {
            // Cannot put bed under maintenance if an active inpatient is admitted
            String chkSql = "SELECT COUNT(*) FROM ipd_admissions WHERE bed_id = ? AND status = 'Admitted'";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psChk = conn.prepareStatement(chkSql)) {
                psChk.setString(1, bedId);
                try (ResultSet rs = psChk.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("Cannot mark bed " + bedId + " as Under Maintenance: Patient currently admitted.");
                        return false;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error checking active admission for maintenance toggle: " + e.getMessage());
            }
        }

        String sql = "UPDATE ipd_beds SET status = ?, current_admission_id = ?, current_patient_name = ? WHERE bed_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, currentAdmissionId);
            ps.setString(3, currentPatientName);
            ps.setString(4, bedId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating IPD bed status: " + e.getMessage());
            return false;
        }
    }

    // --- ADMISSION MANAGEMENT ---

    public boolean admitPatient(IpdAdmission adm) {
        String sql = "INSERT INTO ipd_admissions (admission_id, patient_id, patient_name, patient_gender, patient_age, patient_phone, bed_id, block_name, ward_type, room_number, bed_number, attending_doctor, admission_date, expected_discharge_date, actual_discharge_date, diagnosis, status, advance_paid, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Authoritative patient lookup from patients table to link actual patient record
            if (adm.getPatientId() != null && !adm.getPatientId().isEmpty()) {
                try (PreparedStatement psPt = conn.prepareStatement("SELECT name, gender, age, phone FROM patients WHERE patient_id = ? OR email = ? LIMIT 1")) {
                    psPt.setString(1, adm.getPatientId());
                    psPt.setString(2, adm.getPatientId());
                    try (ResultSet rsPt = psPt.executeQuery()) {
                        if (rsPt.next()) {
                            String realName = rsPt.getString("name");
                            if (realName != null && !realName.isEmpty()) adm.setPatientName(realName);
                            String realGen = rsPt.getString("gender");
                            if (realGen != null && !realGen.isEmpty()) adm.setPatientGender(realGen);
                            int realAge = rsPt.getInt("age");
                            if (realAge > 0) adm.setPatientAge(realAge);
                            String realPhone = rsPt.getString("phone");
                            if (realPhone != null && !realPhone.isEmpty()) adm.setPatientPhone(realPhone);
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 2. Concurrency check: verify bed exists and is Available (not Occupied, not Under Maintenance)
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT status FROM ipd_beds WHERE bed_id = ? FOR UPDATE")) {
                psCheck.setString(1, adm.getBedId());
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        String st = rsCheck.getString("status");
                        if (st != null && !st.equalsIgnoreCase("Available")) {
                            System.err.println("Bed allocation rejected: Bed " + adm.getBedId() + " is currently " + st);
                            conn.rollback();
                            return false;
                        }
                    } else {
                        System.err.println("Bed allocation rejected: Bed " + adm.getBedId() + " does not exist.");
                        conn.rollback();
                        return false;
                    }
                }
            } catch (SQLException fallbackSql) {
                // In case DBMS does not support FOR UPDATE (e.g. SQLite), standard check
                try (PreparedStatement psCheckSimple = conn.prepareStatement("SELECT status FROM ipd_beds WHERE bed_id = ?")) {
                    psCheckSimple.setString(1, adm.getBedId());
                    try (ResultSet rsCheckSimple = psCheckSimple.executeQuery()) {
                        if (rsCheckSimple.next()) {
                            String st = rsCheckSimple.getString("status");
                            if (st != null && !st.equalsIgnoreCase("Available")) {
                                conn.rollback();
                                return false;
                            }
                        }
                    }
                }
            }

            // 3. Double-check no existing active admission for this bed ID
            try (PreparedStatement psDup = conn.prepareStatement("SELECT COUNT(*) FROM ipd_admissions WHERE bed_id = ? AND status = 'Admitted'")) {
                psDup.setString(1, adm.getBedId());
                try (ResultSet rsDup = psDup.executeQuery()) {
                    if (rsDup.next() && rsDup.getInt(1) > 0) {
                        System.err.println("Concurrent admission rejected: Bed " + adm.getBedId() + " already has an active admission.");
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 4. Insert Inpatient Admission record
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, adm.getAdmissionId());
                ps.setString(2, adm.getPatientId());
                ps.setString(3, adm.getPatientName());
                ps.setString(4, adm.getPatientGender());
                ps.setInt(5, adm.getPatientAge());
                ps.setString(6, adm.getPatientPhone());
                ps.setString(7, adm.getBedId());
                ps.setString(8, adm.getBlockName());
                ps.setString(9, adm.getWardType());
                ps.setString(10, adm.getRoomNumber());
                ps.setString(11, adm.getBedNumber());
                ps.setString(12, adm.getAttendingDoctor());
                ps.setString(13, adm.getAdmissionDate());
                ps.setString(14, adm.getExpectedDischargeDate());
                ps.setString(15, adm.getActualDischargeDate());
                ps.setString(16, adm.getDiagnosis());
                ps.setString(17, adm.getStatus() != null ? adm.getStatus() : "Admitted");
                ps.setDouble(18, adm.getAdvancePaid());
                ps.setString(19, adm.getCreatedAt());

                int res = ps.executeUpdate();
                if (res > 0) {
                    // 5. Update corresponding bed to Occupied with admission ID and patient name in same transaction
                    try (PreparedStatement psBed = conn.prepareStatement("UPDATE ipd_beds SET status = 'Occupied', current_admission_id = ?, current_patient_name = ? WHERE bed_id = ?")) {
                        psBed.setString(1, adm.getAdmissionId());
                        psBed.setString(2, adm.getPatientName());
                        psBed.setString(3, adm.getBedId());
                        psBed.executeUpdate();
                    }

                    conn.commit();

                    // 6. Synchronize with nurse_assignments in isolated attempt
                    try {
                        syncInpatientWithNurse(conn, adm);
                    } catch (Exception ignored) {}

                    return true;
                } else {
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error admitting IPD patient: " + e.getMessage());
        }
        return false;
    }

    public boolean dischargePatient(String admissionId, String actualDischargeDate) {
        String fetchBedSql = "SELECT admission_id, bed_id, patient_id, patient_name, attending_doctor, diagnosis FROM ipd_admissions WHERE admission_id = ?";
        String updateAdmSql = "UPDATE ipd_admissions SET status = 'Discharged', actual_discharge_date = ? WHERE admission_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            String bedId = null;
            String patientId = null;
            String patientName = null;
            String attendingDoctor = null;
            String diagnosis = null;

            try (PreparedStatement psFetch = conn.prepareStatement(fetchBedSql)) {
                psFetch.setString(1, admissionId);
                try (ResultSet rs = psFetch.executeQuery()) {
                    if (rs.next()) {
                        bedId = rs.getString("bed_id");
                        patientId = rs.getString("patient_id");
                        patientName = rs.getString("patient_name");
                        attendingDoctor = rs.getString("attending_doctor");
                        diagnosis = rs.getString("diagnosis");
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateAdmSql)) {
                psUpdate.setString(1, actualDischargeDate);
                psUpdate.setString(2, admissionId);
                int rows = psUpdate.executeUpdate();
                if (rows > 0 && bedId != null) {
                    // 1. Free the bed back to Available and clear current patient in same transaction
                    try (PreparedStatement psBed = conn.prepareStatement("UPDATE ipd_beds SET status = 'Available', current_admission_id = NULL, current_patient_name = NULL WHERE bed_id = ?")) {
                        psBed.setString(1, bedId);
                        psBed.executeUpdate();
                    }

                    // 2. Synchronize with nurse_assignments
                    try (PreparedStatement psNurse = conn.prepareStatement("UPDATE nurse_assignments SET status = 'Discharged' WHERE assignment_id = ? OR patient_id = ?")) {
                        psNurse.setString(1, "ASN-" + admissionId);
                        psNurse.setString(2, patientId != null ? patientId : "");
                        psNurse.executeUpdate();
                    } catch (Exception ignored) {}

                    conn.commit();

                    // 3. Automated Post-Discharge Follow-up Trigger (Requirement 4)
                    try {
                        FollowUpDAO followUpDAO = new FollowUpDAO();
                        String folId = "FOL-" + (100000 + new java.util.Random().nextInt(900000));
                        String folDate;
                        try {
                            folDate = java.time.LocalDate.parse(actualDischargeDate).plusDays(7).toString();
                        } catch (Exception ex) {
                            folDate = java.time.LocalDate.now().plusDays(7).toString();
                        }
                        String docName = attendingDoctor != null && !attendingDoctor.isEmpty() ? attendingDoctor : "Attending Physician";
                        com.hospital.model.PostDischargeFollowup fol = new com.hospital.model.PostDischargeFollowup(
                                folId,
                                admissionId,
                                patientId != null ? patientId : "PT100842",
                                patientName != null ? patientName : "Patient",
                                "DOC10084",
                                docName,
                                actualDischargeDate,
                                folDate,
                                "Follow-up recovery check, vital signs assessment, and medication reconciliation.",
                                "Patient discharged in stable clinical condition. Follow-up scheduled for Day 7 post-discharge.",
                                "Pending",
                                "",
                                "",
                                "",
                                "",
                                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())
                        );
                        followUpDAO.createFollowUp(fol);
                    } catch (Exception folEx) {
                        System.err.println("Error creating post-discharge follow-up: " + folEx.getMessage());
                    }

                    return true;
                } else {
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error discharging IPD patient: " + e.getMessage());
        }
        return false;
    }



    public List<IpdAdmission> getAllAdmissions() {
        List<IpdAdmission> list = new ArrayList<>();
        String sql = "SELECT * FROM ipd_admissions ORDER BY admission_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAdmission(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching IPD admissions: " + e.getMessage());
        }
        return list;
    }

    public List<IpdAdmission> getActiveAdmissions() {
        List<IpdAdmission> list = new ArrayList<>();
        String sql = "SELECT * FROM ipd_admissions WHERE status = 'Admitted' ORDER BY admission_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAdmission(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active IPD admissions: " + e.getMessage());
        }
        return list;
    }

    public List<IpdAdmission> getAdmissionsByDoctor(String doctorName) {
        List<IpdAdmission> list = new ArrayList<>();
        String sql = "SELECT * FROM ipd_admissions WHERE attending_doctor LIKE ? ORDER BY admission_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + doctorName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAdmission(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor IPD admissions: " + e.getMessage());
        }
        return list;
    }

    public List<IpdAdmission> getAdmissionsByPatient(String patientId) {
        List<IpdAdmission> list = new ArrayList<>();
        String sql = "SELECT * FROM ipd_admissions WHERE patient_id = ? ORDER BY admission_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAdmission(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient IPD admissions: " + e.getMessage());
        }
        return list;
    }

    private void syncInpatientWithNurse(Connection conn, IpdAdmission adm) {
        String sql = "INSERT INTO nurse_assignments (assignment_id, nurse_id, nurse_name, patient_id, patient_name, doctor_name, ward, room_number, bed_number, admission_date, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "ASN-" + adm.getAdmissionId());
            ps.setString(2, "NUR10084");
            ps.setString(3, "Nurse Priya Sharma");
            ps.setString(4, adm.getPatientId());
            ps.setString(5, adm.getPatientName());
            ps.setString(6, adm.getAttendingDoctor());
            ps.setString(7, adm.getWardType());
            ps.setString(8, adm.getRoomNumber());
            ps.setString(9, adm.getBedNumber());
            ps.setString(10, adm.getAdmissionDate());
            ps.setString(11, "Active");
            ps.setString(12, adm.getCreatedAt());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private IpdAdmission mapAdmission(ResultSet rs) throws SQLException {
        return new IpdAdmission(
                rs.getString("admission_id"),
                rs.getString("patient_id"),
                rs.getString("patient_name"),
                rs.getString("patient_gender"),
                rs.getInt("patient_age"),
                rs.getString("patient_phone"),
                rs.getString("bed_id"),
                rs.getString("block_name"),
                rs.getString("ward_type"),
                rs.getString("room_number"),
                rs.getString("bed_number"),
                rs.getString("attending_doctor"),
                rs.getString("admission_date"),
                rs.getString("expected_discharge_date"),
                rs.getString("actual_discharge_date"),
                rs.getString("diagnosis"),
                rs.getString("status"),
                rs.getDouble("advance_paid"),
                rs.getString("created_at")
        );
    }
}

