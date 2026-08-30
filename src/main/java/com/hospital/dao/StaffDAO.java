package com.hospital.dao;

import com.hospital.model.Staff;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Data Access Object for Hospital Staff management with unified Employee Code (EMP-XXXXXX).
 */
public class StaffDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public synchronized String generateNextEmployeeCode() {
        String sql = "SELECT employee_code FROM staff WHERE employee_code LIKE 'EMP-%' ORDER BY employee_code DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int maxId = 0;
            while (rs.next()) {
                String code = rs.getString("employee_code");
                if (code != null && code.startsWith("EMP-")) {
                    try {
                        int num = Integer.parseInt(code.substring(4));
                        if (num > maxId) maxId = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return String.format("EMP-%06d", maxId + 1);
        } catch (Exception e) {
            System.err.println("Error generating Employee Code: " + e.getMessage());
            return "EMP-000001";
        }
    }

    public List<Staff> getAllStaff() {
        return searchStaff("");
    }

    public List<Staff> searchStaff(String query) {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT * FROM staff";
        if (query != null && !query.trim().isEmpty()) {
            sql += " WHERE LOWER(employee_code) LIKE ? OR LOWER(full_name) LIKE ? OR LOWER(department) LIKE ? OR LOWER(role) LIKE ?";
        }
        sql += " ORDER BY employee_code ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (query != null && !query.trim().isEmpty()) {
                String q = "%" + query.trim().toLowerCase() + "%";
                ps.setString(1, q);
                ps.setString(2, q);
                ps.setString(3, q);
                ps.setString(4, q);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStaff(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying staff: " + e.getMessage());
        }

        if (list.isEmpty() && (query == null || query.trim().isEmpty())) {
            list = seedDefaultStaff();
        }
        return list;
    }

    public Staff getStaffByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        String sql = "SELECT * FROM staff WHERE employee_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToStaff(rs);
            }
        } catch (Exception e) {
            System.err.println("Error fetching staff by code: " + e.getMessage());
        }
        return seedDefaultStaff().stream().filter(s -> s.getEmployeeCode().equalsIgnoreCase(code.trim())).findFirst().orElse(null);
    }

    public Staff getStaffByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String sql = "SELECT * FROM staff WHERE LOWER(email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToStaff(rs);
            }
        } catch (Exception e) {
            System.err.println("Error fetching staff by email: " + e.getMessage());
        }
        return seedDefaultStaff().stream().filter(s -> s.getEmail().equalsIgnoreCase(email.trim())).findFirst().orElse(null);
    }

    public Staff getStaffByCodeOrEmail(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) return null;
        Staff s = getStaffByCode(loginId);
        if (s != null) return s;
        return getStaffByEmail(loginId);
    }

    public boolean addStaff(Staff staff) {
        if (staff.getEmployeeCode() == null || staff.getEmployeeCode().trim().isEmpty()) {
            staff.setEmployeeCode(generateNextEmployeeCode());
        }
        String now = LocalDateTime.now().format(FMT);
        staff.setCreatedAt(now);
        staff.setUpdatedAt(now);

        String sql = "INSERT INTO staff (employee_code, role, full_name, email, mobile, department, designation, " +
                     "qualification, experience, status, created_at, updated_at, medical_reg_no, specialization, " +
                     "consultation_fee, license_no, office_extension, blood_group, joining_date, emergency_contact, validity, password) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staff.getEmployeeCode());
            ps.setString(2, staff.getRole());
            ps.setString(3, staff.getFullName());
            ps.setString(4, staff.getEmail());
            ps.setString(5, staff.getMobile());
            ps.setString(6, staff.getDepartment());
            ps.setString(7, staff.getDesignation());
            ps.setString(8, staff.getQualification());
            ps.setString(9, staff.getExperience());
            ps.setString(10, staff.getStatus() != null ? staff.getStatus() : "Active");
            ps.setString(11, staff.getCreatedAt());
            ps.setString(12, staff.getUpdatedAt());
            ps.setString(13, staff.getMedicalRegNo());
            ps.setString(14, staff.getSpecialization());
            ps.setDouble(15, staff.getConsultationFee());
            ps.setString(16, staff.getLicenseNo());
            ps.setString(17, staff.getOfficeExtension());
            ps.setString(18, staff.getBloodGroup() != null ? staff.getBloodGroup() : "O+");
            ps.setString(19, staff.getJoiningDate() != null ? staff.getJoiningDate() : "2026-01-15");
            ps.setString(20, staff.getEmergencyContact() != null ? staff.getEmergencyContact() : "+91 98765 43210");
            ps.setString(21, staff.getValidity() != null ? staff.getValidity() : "31-DEC-2028");
            ps.setString(22, staff.getPassword() != null ? staff.getPassword() : "demo1234");

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error adding staff: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStaff(Staff staff) {
        String now = LocalDateTime.now().format(FMT);
        staff.setUpdatedAt(now);

        String sql = "UPDATE staff SET role=?, full_name=?, email=?, mobile=?, department=?, designation=?, " +
                     "qualification=?, experience=?, status=?, updated_at=?, medical_reg_no=?, specialization=?, " +
                     "consultation_fee=?, license_no=?, office_extension=?, blood_group=?, joining_date=?, emergency_contact=?, validity=? " +
                     "WHERE employee_code=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staff.getRole());
            ps.setString(2, staff.getFullName());
            ps.setString(3, staff.getEmail());
            ps.setString(4, staff.getMobile());
            ps.setString(5, staff.getDepartment());
            ps.setString(6, staff.getDesignation());
            ps.setString(7, staff.getQualification());
            ps.setString(8, staff.getExperience());
            ps.setString(9, staff.getStatus());
            ps.setString(10, staff.getUpdatedAt());
            ps.setString(11, staff.getMedicalRegNo());
            ps.setString(12, staff.getSpecialization());
            ps.setDouble(13, staff.getConsultationFee());
            ps.setString(14, staff.getLicenseNo());
            ps.setString(15, staff.getOfficeExtension());
            ps.setString(16, staff.getBloodGroup());
            ps.setString(17, staff.getJoiningDate());
            ps.setString(18, staff.getEmergencyContact());
            ps.setString(19, staff.getValidity());
            ps.setString(20, staff.getEmployeeCode());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating staff: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(String employeeCode, String status) {
        String now = LocalDateTime.now().format(FMT);
        String sql = "UPDATE staff SET status=?, updated_at=? WHERE employee_code=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, now);
            ps.setString(3, employeeCode);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStaff(String employeeCode) {
        String sql = "DELETE FROM staff WHERE employee_code=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeCode);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setEmployeeCode(rs.getString("employee_code"));
        s.setRole(rs.getString("role"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setMobile(rs.getString("mobile"));
        s.setDepartment(rs.getString("department"));
        s.setDesignation(rs.getString("designation"));
        s.setQualification(rs.getString("qualification"));
        s.setExperience(rs.getString("experience"));
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getString("created_at"));
        s.setUpdatedAt(rs.getString("updated_at"));
        s.setMedicalRegNo(rs.getString("medical_reg_no"));
        s.setSpecialization(rs.getString("specialization"));
        try { s.setConsultationFee(rs.getDouble("consultation_fee")); } catch(Exception ignored){}
        s.setLicenseNo(rs.getString("license_no"));
        s.setOfficeExtension(rs.getString("office_extension"));
        s.setBloodGroup(rs.getString("blood_group"));
        s.setJoiningDate(rs.getString("joining_date"));
        s.setEmergencyContact(rs.getString("emergency_contact"));
        s.setValidity(rs.getString("validity"));
        s.setPassword(rs.getString("password"));
        return s;
    }

    private List<Staff> seedDefaultStaff() {
        List<Staff> list = new ArrayList<>();

        Staff doc = new Staff("EMP-000001", "Doctor", "Dr. Ananya Rao", "doctor@niramaya.health", "+91 98765 43211", "Cardiology", "Consultant Cardiologist", "Active");
        doc.setMedicalRegNo("MCI-2012-89412");
        doc.setSpecialization("Interventional Cardiology");
        doc.setQualification("MD, DM (Cardiology)");
        doc.setExperience("14 Yrs");
        doc.setConsultationFee(800.0);
        doc.setJoiningDate("2021-03-15");
        doc.setBloodGroup("A+");
        doc.setEmergencyContact("+91 98765 43211");
        list.add(doc);

        Staff tech = new Staff("EMP-000002", "Laboratory Technician", "Suresh Nair", "labtech@tech.in", "+91 98765 43219", "Pathology", "Senior Lab Technologist", "Active");
        tech.setQualification("B.Sc MLT, M.Sc Pathology");
        tech.setExperience("8 Yrs");
        tech.setJoiningDate("2022-06-01");
        tech.setBloodGroup("O+");
        tech.setEmergencyContact("+91 98765 43219");
        list.add(tech);

        Staff pharm = new Staff("EMP-000003", "Pharmacist", "Priya Sharma", "pharmacy@niramaya.health", "+91 98765 43220", "Pharmacy", "Chief Pharmacist", "Active");
        pharm.setLicenseNo("PHARM-DL-2020-90812");
        pharm.setQualification("M.Pharm, Registered Pharmacist");
        pharm.setExperience("10 Yrs");
        pharm.setJoiningDate("2020-01-10");
        pharm.setBloodGroup("B+");
        pharm.setEmergencyContact("+91 98765 43220");
        list.add(pharm);

        Staff adm = new Staff("EMP-000004", "Administrator", "Hospital Administrator", "admin@niramaya.health", "+91 98765 43200", "Administration", "Chief Operations Officer", "Active");
        adm.setOfficeExtension("Ext: 1004");
        adm.setQualification("MBA (Hospital Mgmt)");
        adm.setExperience("15 Yrs");
        adm.setJoiningDate("2019-11-01");
        adm.setBloodGroup("AB+");
        adm.setEmergencyContact("108 / 1800-425-0000");
        list.add(adm);

        Staff rec = new Staff("EMP-000005", "Receptionist", "Ramesh Verma", "reception@niramaya.health", "+91 98765 43221", "Front Desk", "Senior Receptionist", "Active");
        rec.setQualification("B.Com, Customer Relations");
        rec.setExperience("5 Yrs");
        rec.setJoiningDate("2023-02-15");
        rec.setBloodGroup("O+");
        rec.setEmergencyContact("+91 98765 43221");
        list.add(rec);

        Staff nurse = new Staff("EMP-000006", "Nurse", "Kavita Patel", "nurse@niramaya.health", "+91 98765 43222", "ICU & Casualty", "Head Staff Nurse", "Active");
        nurse.setQualification("B.Sc Nursing, Critical Care");
        nurse.setExperience("7 Yrs");
        nurse.setJoiningDate("2022-09-01");
        nurse.setBloodGroup("A+");
        nurse.setEmergencyContact("+91 98765 43222");
        list.add(nurse);

        return list;
    }
}
