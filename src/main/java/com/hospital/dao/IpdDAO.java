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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                // Update corresponding bed to Occupied
                updateBedStatus(adm.getBedId(), "Occupied", adm.getAdmissionId(), adm.getPatientName());
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error admitting IPD patient: " + e.getMessage());
            return false;
        }
    }

    public boolean dischargePatient(String admissionId, String actualDischargeDate) {
        String fetchBedSql = "SELECT bed_id FROM ipd_admissions WHERE admission_id = ?";
        String updateAdmSql = "UPDATE ipd_admissions SET status = 'Discharged', actual_discharge_date = ? WHERE admission_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            String bedId = null;
            try (PreparedStatement psFetch = conn.prepareStatement(fetchBedSql)) {
                psFetch.setString(1, admissionId);
                try (ResultSet rs = psFetch.executeQuery()) {
                    if (rs.next()) {
                        bedId = rs.getString("bed_id");
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateAdmSql)) {
                psUpdate.setString(1, actualDischargeDate);
                psUpdate.setString(2, admissionId);
                int rows = psUpdate.executeUpdate();
                if (rows > 0 && bedId != null) {
                    // Free the bed back to Available
                    updateBedStatus(bedId, "Available", null, null);
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
