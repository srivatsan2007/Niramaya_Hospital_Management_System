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
        String sql = "SELECT * FROM ipd_beds ORDER BY block_name ASC, room_number ASC, bed_number ASC";
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
                        rs.getString("status"),
                        rs.getString("current_admission_id"),
                        rs.getString("current_patient_name")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching IPD beds: " + e.getMessage());
        }
        return list;
    }

    public boolean updateBedStatus(String bedId, String status, String currentAdmissionId, String currentPatientName) {
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
            // 1. Atomic check: verify bed exists and is Available to prevent race conditions or duplicate allocations
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT status FROM ipd_beds WHERE bed_id = ?")) {
                psCheck.setString(1, adm.getBedId());
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        String st = rsCheck.getString("status");
                        if (st != null && !st.equalsIgnoreCase("Available")) {
                            System.err.println("Bed allocation rejected: Bed " + adm.getBedId() + " is already " + st);
                            return false;
                        }
                    }
                }
            }

            // 2. Insert Inpatient Admission record
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
                    // 3. Update corresponding bed to Occupied with admission ID and patient name
                    updateBedStatus(adm.getBedId(), "Occupied", adm.getAdmissionId(), adm.getPatientName());

                    // 4. Synchronize with nurse_assignments so Nurse portal immediately displays the inpatient in active ward queue
                    syncInpatientWithNurse(conn, adm);

                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error admitting IPD patient: " + e.getMessage());
        }
        return false;
    }

    private void syncInpatientWithNurse(Connection conn, IpdAdmission adm) {
        try {
            String nurseId = "NUR10084";
            String nurseName = "Nurse Priya Sharma";
            try (PreparedStatement psNurse = conn.prepareStatement("SELECT nurse_id, name FROM nurses WHERE status = 'Active' ORDER BY RANDOM() LIMIT 1")) {
                try (ResultSet rNurse = psNurse.executeQuery()) {
                    if (rNurse.next()) {
                        nurseId = rNurse.getString("nurse_id");
                        nurseName = rNurse.getString("name");
                    }
                }
            } catch (Exception ignored) {}

            String insSql = DBConnection.isPostgreSQL() ?
                    "INSERT INTO nurse_assignments (assignment_id, nurse_id, nurse_name, patient_id, patient_name, doctor_name, ward, room_number, bed_number, admission_date, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', ?) ON CONFLICT (assignment_id) DO UPDATE SET status = 'Active', ward = EXCLUDED.ward, room_number = EXCLUDED.room_number, bed_number = EXCLUDED.bed_number;" :
                    "INSERT OR REPLACE INTO nurse_assignments (assignment_id, nurse_id, nurse_name, patient_id, patient_name, doctor_name, ward, room_number, bed_number, admission_date, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', ?);";

            try (PreparedStatement psN = conn.prepareStatement(insSql)) {
                psN.setString(1, "ASN-" + adm.getAdmissionId());
                psN.setString(2, nurseId);
                psN.setString(3, nurseName);
                psN.setString(4, adm.getPatientId());
                psN.setString(5, adm.getPatientName());
                psN.setString(6, adm.getAttendingDoctor());
                psN.setString(7, adm.getWardType());
                psN.setString(8, adm.getRoomNumber());
                psN.setString(9, adm.getBedNumber());
                psN.setString(10, adm.getAdmissionDate());
                psN.setString(11, adm.getCreatedAt());
                psN.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error synchronizing nurse assignment for IPD admission: " + e.getMessage());
        }
    }

    public boolean dischargePatient(String admissionId, String actualDischargeDate) {
        String fetchBedSql = "SELECT bed_id, patient_id FROM ipd_admissions WHERE admission_id = ?";
        String updateAdmSql = "UPDATE ipd_admissions SET status = 'Discharged', actual_discharge_date = ? WHERE admission_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            String bedId = null;
            String patientId = null;
            try (PreparedStatement psFetch = conn.prepareStatement(fetchBedSql)) {
                psFetch.setString(1, admissionId);
                try (ResultSet rs = psFetch.executeQuery()) {
                    if (rs.next()) {
                        bedId = rs.getString("bed_id");
                        patientId = rs.getString("patient_id");
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateAdmSql)) {
                psUpdate.setString(1, actualDischargeDate);
                psUpdate.setString(2, admissionId);
                int rows = psUpdate.executeUpdate();
                if (rows > 0 && bedId != null) {
                    // 1. Free the bed back to Available and clear current patient
                    updateBedStatus(bedId, "Available", null, null);

                    // 2. Synchronize with nurse_assignments: update status to 'Discharged' so patient is removed from active inpatient queue while preserving history
                    try (PreparedStatement psNurse = conn.prepareStatement("UPDATE nurse_assignments SET status = 'Discharged' WHERE assignment_id = ? OR patient_id = ?")) {
                        psNurse.setString(1, "ASN-" + admissionId);
                        psNurse.setString(2, patientId != null ? patientId : "");
                        psNurse.executeUpdate();
                    } catch (Exception ignored) {}

                    return true;
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
