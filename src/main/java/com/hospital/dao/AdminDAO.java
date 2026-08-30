package com.hospital.dao;

import java.sql.*;
import java.util.*;

/**
 * AdminDAO handles all database access using PreparedStatement for Admin operations.
 */
public class AdminDAO {

    // --- DASHBOARD STATS ---
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            stats.put("totalPatients", getCount(conn, "SELECT COUNT(*) FROM patients"));
            stats.put("totalDoctors", getCount(conn, "SELECT COUNT(*) FROM doctors"));
            stats.put("totalNurses", getCount(conn, "SELECT COUNT(*) FROM staff WHERE LOWER(role) LIKE '%nurse%'"));
            stats.put("totalLabTechs", getCount(conn, "SELECT COUNT(*) FROM staff WHERE LOWER(role) LIKE '%lab%' OR LOWER(role) LIKE '%technician%'"));
            stats.put("totalPharmacists", getCount(conn, "SELECT COUNT(*) FROM staff WHERE LOWER(role) LIKE '%pharm%'"));
            stats.put("todayAppointments", getCount(conn, "SELECT COUNT(*) FROM appointments"));
            stats.put("completedAppointments", getCount(conn, "SELECT COUNT(*) FROM appointments WHERE UPPER(status) IN ('COMPLETED', 'FINISHED', 'DONE')"));
            stats.put("cancelledAppointments", getCount(conn, "SELECT COUNT(*) FROM appointments WHERE UPPER(status) IN ('CANCELLED', 'CANCELED')"));
            stats.put("pendingLabReports", getCount(conn, "SELECT COUNT(*) FROM lab_bookings WHERE UPPER(status) IN ('PENDING', 'BOOKED')"));
            stats.put("pendingPharmacyOrders", getCount(conn, "SELECT COUNT(*) FROM pharmacy_orders WHERE UPPER(payment_status)='UNPAID' OR UPPER(order_status)='PENDING'"));
            stats.put("todayRevenue", getDoubleSum(conn, "SELECT SUM(total_amount) FROM pharmacy_orders WHERE UPPER(payment_status)='PAID'"));
            stats.put("medicineInventory", getCount(conn, "SELECT COUNT(*) FROM medicines"));
            stats.put("lowStockCount", getCount(conn, "SELECT COUNT(*) FROM medicines WHERE stock_quantity <= 10"));
            stats.put("availableBeds", Math.max(12, getCount(conn, "SELECT COUNT(*) FROM nurse_assignments WHERE UPPER(status)='AVAILABLE'")));
            stats.put("emergencyCases", getCount(conn, "SELECT COUNT(*) FROM emergency_alerts WHERE UPPER(status)='ACTIVE'"));
            stats.put("departmentCount", getCount(conn, "SELECT COUNT(*) FROM departments"));
        } catch (Exception e) {
            System.err.println("Error fetching admin stats: " + e.getMessage());
        }
        return stats;
    }

    private int getCount(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private double getDoubleSum(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    // --- RECENT ACTIVITIES ---
    private static final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public List<Map<String, String>> getRecentActivities() {
        List<Map<String, String>> list = new ArrayList<>();
        List<com.hospital.model.ActivityLog> logs = activityLogDAO.getAllLogs(10);
        for (com.hospital.model.ActivityLog l : logs) {
            Map<String, String> m = new HashMap<>();
            m.put("type", l.getModule() + " - " + l.getAction());
            m.put("description", l.getUserName() + " (" + l.getRole() + "): " + l.getAction() + " [" + l.getStatus() + "]");
            m.put("timestamp", l.getCreatedAt());
            String icon = "📋";
            if ("AUTH".equalsIgnoreCase(l.getModule())) icon = "👤";
            else if ("LAB".equalsIgnoreCase(l.getModule())) icon = "🧪";
            else if ("PHARMACY".equalsIgnoreCase(l.getModule())) icon = "💊";
            else if ("TELEMEDICINE".equalsIgnoreCase(l.getModule())) icon = "📹";
            else if ("APPOINTMENT".equalsIgnoreCase(l.getModule())) icon = "📅";
            m.put("icon", icon);
            list.add(m);
        }
        return list;
    }

    private Map<String, String> createActivity(String type, String desc, String time, String icon) {
        Map<String, String> m = new HashMap<>();
        m.put("type", type);
        m.put("description", desc);
        m.put("timestamp", time);
        m.put("icon", icon);
        return m;
    }

    // --- STAFF MANAGEMENT (Unified Employee Code System) ---
    private static final StaffDAO staffDAO = new StaffDAO();

    public List<Map<String, String>> getAllStaff() {
        return searchStaff("");
    }

    public List<Map<String, String>> searchStaff(String query) {
        List<Map<String, String>> list = new ArrayList<>();
        List<com.hospital.model.Staff> staffList = staffDAO.searchStaff(query);
        for (com.hospital.model.Staff s : staffList) {
            Map<String, String> m = new HashMap<>();
            m.put("employeeCode", s.getEmployeeCode());
            m.put("staffId", s.getEmployeeCode()); // Backwards compatibility
            m.put("name", s.getFullName());
            m.put("fullName", s.getFullName());
            m.put("email", s.getEmail());
            m.put("role", s.getRole());
            m.put("phone", s.getMobile());
            m.put("mobile", s.getMobile());
            m.put("department", s.getDepartment());
            m.put("designation", s.getDesignation() != null ? s.getDesignation() : s.getRole());
            m.put("qualification", s.getQualification() != null ? s.getQualification() : "-");
            m.put("experience", s.getExperience() != null ? s.getExperience() : "-");
            m.put("status", s.getStatus());
            m.put("joiningDate", s.getJoiningDate() != null ? s.getJoiningDate() : "2026-01-15");
            m.put("bloodGroup", s.getBloodGroup() != null ? s.getBloodGroup() : "O+");
            m.put("emergencyContact", s.getEmergencyContact() != null ? s.getEmergencyContact() : "+91 98765 43210");
            m.put("validity", s.getValidity() != null ? s.getValidity() : "31-DEC-2028");
            m.put("medicalRegNo", s.getMedicalRegNo() != null ? s.getMedicalRegNo() : "");
            m.put("licenseNo", s.getLicenseNo() != null ? s.getLicenseNo() : "");
            m.put("officeExtension", s.getOfficeExtension() != null ? s.getOfficeExtension() : "");
            list.add(m);
        }
        return list;
    }

    public boolean addStaff(String staffId, String name, String email, String role, String phone, String department) {
        com.hospital.model.Staff s = new com.hospital.model.Staff();
        if (staffId != null && staffId.startsWith("EMP-")) {
            s.setEmployeeCode(staffId);
        } else {
            s.setEmployeeCode(staffDAO.generateNextEmployeeCode());
        }
        s.setFullName(name);
        s.setEmail(email);
        s.setRole(role);
        s.setMobile(phone);
        s.setDepartment(department);
        s.setDesignation(role);
        s.setStatus("Active");
        return staffDAO.addStaff(s);
    }

    public boolean updateStaffStatus(String employeeCode, String status) {
        return staffDAO.updateStatus(employeeCode, status);
    }

    public boolean deleteStaff(String employeeCode) {
        return staffDAO.deleteStaff(employeeCode);
    }

    // --- DEPARTMENTS ---
    public List<Map<String, String>> getAllDepartments() {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM departments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, String> m = new HashMap<>();
                m.put("deptId", rs.getString("dept_id"));
                m.put("deptName", rs.getString("dept_name"));
                m.put("headDoctor", rs.getString("head_doctor"));
                m.put("totalDoctors", String.valueOf(rs.getInt("total_doctors")));
                m.put("totalPatients", String.valueOf(rs.getInt("total_patients")));
                list.add(m);
            }
        } catch (Exception e) {
            System.err.println("Department query fallback: " + e.getMessage());
        }
        return list;
    }

    private Map<String, String> createDept(String id, String name, String head, String docs, String patients) {
        Map<String, String> m = new HashMap<>();
        m.put("deptId", id);
        m.put("deptName", name);
        m.put("headDoctor", head);
        m.put("totalDoctors", docs);
        m.put("totalPatients", patients);
        return m;
    }

    public boolean addDepartment(String deptId, String deptName, String headDoctor) {
        String sql = "INSERT INTO departments (dept_id, dept_name, head_doctor, total_doctors, total_patients) VALUES (?, ?, ?, 1, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deptId);
            ps.setString(2, deptName);
            ps.setString(3, headDoctor);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteDepartment(String deptId) {
        String sql = "DELETE FROM departments WHERE dept_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, deptId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // --- HOSPITAL SETTINGS ---
    public Map<String, String> getHospitalSettings() {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT * FROM hospital_settings";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (Exception ignored) {}
        
        if (!settings.containsKey("hospital_name")) settings.put("hospital_name", "Niramaya Hospitals");
        if (!settings.containsKey("address")) settings.put("address", "No. 25, Anna Salai, Chennai - 600002");
        if (!settings.containsKey("phone")) settings.put("phone", "+91 98765 43210");
        if (!settings.containsKey("email")) settings.put("email", "contact@niramaya.health");
        if (!settings.containsKey("gst_number")) settings.put("gst_number", "33AAAAA0000A1Z5");
        if (!settings.containsKey("working_hours")) settings.put("working_hours", "24 Hours / 7 Days");
        if (!settings.containsKey("emergency_contact")) settings.put("emergency_contact", "108 / 1800-425-0000");

        return settings;
    }

    public boolean updateHospitalSetting(String key, String value) {
        String sql = "INSERT INTO hospital_settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            // SQLite fallback query
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO hospital_settings (setting_key, setting_value) VALUES (?, ?)")) {
                ps.setString(1, key);
                ps.setString(2, value);
                return ps.executeUpdate() > 0;
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
