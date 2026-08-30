package com.hospital.dao;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Data Access Object for Database Manager Center.
 * Handles schema inspection, dynamic JTable queries, dynamic CRUD via PreparedStatements,
 * SQL Query Console execution, DB Health metrics, and SQL/CSV Backup & Restore operations.
 */
public class DatabaseManagerDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static String lastBackupTimestamp = "Never";
    private static String lastBackupPath = "None";

    // Allowed tables registry for sidebar icons and descriptions
    public static final List<Map<String, String>> ALL_TABLE_METADATA = Arrays.asList(
        createTableMeta("patients", "👤 Patients", "Patient Demographic & Medical Registry"),
        createTableMeta("staff", "👨‍⚕️ Staff", "Hospital Personnel & Medical Officers"),
        createTableMeta("doctors", "🩺 Doctors", "Doctor Profiles, Availability & Timings"),
        createTableMeta("appointments", "📅 Appointments", "Consultation Bookings & Tokens"),
        createTableMeta("online_consultations", "🎥 Telemedicine Consultations", "Telemedicine Meetings & Sessions"),
        createTableMeta("consultation_notes", "📋 Clinical Consult Notes", "Clinical Consultation Notes & Observations"),
        createTableMeta("prescriptions", "📝 Prescriptions", "Clinical Diagnosis & Rx Details"),
        createTableMeta("lab_reports", "📄 Lab Reports", "Diagnostic Test Results & Findings"),
        createTableMeta("lab_bookings", "🧪 Lab Bookings", "Patient Lab Orders & Tokens"),
        createTableMeta("lab_payments", "💳 Lab Payments", "Lab Billing Transactions & Receipts"),
        createTableMeta("pharmacy_orders", "🛒 Pharmacy Orders", "Medication Prescriptions & Orders"),
        createTableMeta("pharmacy_order_items", "📋 Order Items", "Pharmacy Order Medicine Breakdown"),
        createTableMeta("medicines", "📦 Medicine Inventory", "Drug Inventory & Stock Batches"),
        createTableMeta("billing", "💰 Payments & Billing", "Billing Transactions & Receipts"),
        createTableMeta("notifications", "🔔 Notifications", "System Notifications & Broadcasts"),
        createTableMeta("activity_logs", "📊 Activity Logs", "System Audit Trail & Access Logs"),
        createTableMeta("departments", "🏥 Departments", "Clinical Specialties & Units"),
        createTableMeta("hospital_settings", "⚙ Hospital Settings", "System Configuration Parameters"),
        createTableMeta("users", "🔑 User Accounts", "User Accounts & Authentication Credentials")
    );

    // Set of legacy duplicate table names to skip from sidebar display
    private static final Set<String> IGNORED_DUPLICATE_TABLES = new HashSet<>(Arrays.asList(
        "audit_logs",           // Primary: activity_logs
        "laboratory_reports",   // Primary: lab_reports
        "online_consultation",  // Primary: online_consultations
        "pharmacy_inventory",   // Primary: medicines
        "system_notifications"  // Primary: notifications
    ));

    public List<Map<String, String>> getAllTables() {
        List<Map<String, String>> tableList = new ArrayList<>();
        try (Connection conn = DBConnection.getValidatedConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            boolean isPG = DBConnection.isPostgreSQL();
            try (ResultSet rs = meta.getTables(null, isPG ? "public" : null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName == null) continue;
                    String lower = tableName.toLowerCase();
                    if (lower.startsWith("pg_") || lower.startsWith("sql_")) continue;
                    if (IGNORED_DUPLICATE_TABLES.contains(lower)) continue;
                    
                    tableList.add(createTableMeta(tableName, getTableIconLabel(tableName), "PostgreSQL Database Table: " + tableName));
                }
            }
        } catch (Exception e) {
            System.err.println("[SQL Exception] Error inspecting dynamic tables via JDBC: " + e.getMessage());
            e.printStackTrace();
        }
        if (tableList.isEmpty()) {
            return ALL_TABLE_METADATA;
        }
        return tableList;
    }

    private static String getTableIconLabel(String tableName) {
        if (tableName == null) return "📁 Unknown Table";
        String lower = tableName.toLowerCase();
        switch (lower) {
            case "patients": return "👤 Patients";
            case "doctors": return "🩺 Doctors";
            case "staff": return "👨‍⚕️ Staff";
            case "appointments": return "📅 Appointments";
            case "prescriptions": return "📝 Prescriptions";
            case "online_consultations":
            case "online_consultation": return "🎥 Telemedicine Consultations";
            case "consultation_notes": return "📋 Clinical Consult Notes";
            case "lab_bookings": return "🧪 Lab Bookings";
            case "lab_reports":
            case "laboratory_reports": return "📄 Lab Reports";
            case "lab_payments": return "💳 Lab Payments";
            case "pharmacy_orders": return "🛒 Pharmacy Orders";
            case "pharmacy_order_items": return "📋 Order Items";
            case "medicines":
            case "pharmacy_inventory": return "📦 Medicine Inventory";
            case "billing": return "💰 Payments & Billing";
            case "notifications":
            case "system_notifications": return "🔔 Notifications";
            case "activity_logs":
            case "audit_logs": return "📊 Activity Logs";
            case "meeting_chat": return "💬 Telemed Chat";
            case "meeting_logs": return "📹 Meeting Logs";
            case "departments": return "🏥 Departments";
            case "hospital_settings": return "⚙ Hospital Settings";
            case "users": return "🔑 User Accounts";
            default: break;
        }
        if (lower.contains("patient")) return "👤 " + formatTitle(tableName);
        if (lower.contains("doctor")) return "🩺 " + formatTitle(tableName);
        if (lower.contains("staff")) return "👨‍⚕️ " + formatTitle(tableName);
        if (lower.contains("appoint")) return "📅 " + formatTitle(tableName);
        if (lower.contains("prescr")) return "📝 " + formatTitle(tableName);
        if (lower.contains("report")) return "📄 " + formatTitle(tableName);
        if (lower.contains("lab")) return "🧪 " + formatTitle(tableName);
        if (lower.contains("pharm") || lower.contains("med")) return "📦 " + formatTitle(tableName);
        if (lower.contains("pay") || lower.contains("bill")) return "💳 " + formatTitle(tableName);
        if (lower.contains("notif")) return "🔔 " + formatTitle(tableName);
        if (lower.contains("log")) return "📊 " + formatTitle(tableName);
        if (lower.contains("dept")) return "🏥 " + formatTitle(tableName);
        if (lower.contains("setting")) return "⚙ " + formatTitle(tableName);
        return "📁 " + formatTitle(tableName);
    }

    private static String formatTitle(String str) {
        if (str == null || str.isEmpty()) return "";
        String[] parts = str.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static Map<String, String> createTableMeta(String name, String label, String desc) {
        Map<String, String> m = new HashMap<>();
        m.put("name", name);
        m.put("label", label);
        m.put("description", desc);
        return m;
    }

    // --- 1. DATABASE HEALTH & MONITOR STATS ---
    public Map<String, Object> getDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        try (Connection conn = DBConnection.getValidatedConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            boolean isPG = DBConnection.isPostgreSQL();
            boolean isMySQL = DBConnection.isMySQL();

            String dbName = conn.getCatalog();
            String serverVer = meta.getDatabaseProductVersion();

            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT current_database()")) {
                    if (rs.next()) dbName = rs.getString(1);
                } catch (Exception ignored) {}
                try (ResultSet rs = stmt.executeQuery("SELECT version()")) {
                    if (rs.next()) serverVer = rs.getString(1);
                } catch (Exception ignored) {}
            }

            if (dbName == null || dbName.trim().isEmpty()) {
                dbName = "neondb";
            }

            health.put("connected", true);
            health.put("databaseName", dbName);
            health.put("serverVersion", serverVer);
            health.put("productName", meta.getDatabaseProductName());
            health.put("driverName", meta.getDriverName());
            health.put("host", isPG ? "Neon Cloud (PostgreSQL)" : (isMySQL ? "localhost" : "Embedded SQLite"));
            health.put("port", isPG ? "5432" : (isMySQL ? "3306" : "N/A"));
            health.put("lastBackup", lastBackupTimestamp);
            health.put("errorMessage", "");

            int tableCount = 0;
            long totalRecords = 0;
            try (ResultSet rs = meta.getTables(null, isPG ? "public" : null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName.startsWith("pg_") || tableName.startsWith("sql_")) continue;
                    tableCount++;
                    totalRecords += getTableRowCount(conn, tableName);
                }
            } catch (SQLException e) {
                System.err.println("[SQL Exception] Error fetching tables meta: " + e.getMessage());
                e.printStackTrace();
            }
            health.put("totalTables", tableCount);
            health.put("totalRecords", totalRecords);
            health.put("storageUsed", String.format("%.2f KB", totalRecords * 1.5));
            health.put("activeConnections", 1);
        } catch (Exception e) {
            System.err.println("[SQL Exception] Database Connection Error in health check: " + e.getMessage());
            e.printStackTrace();
            health.put("connected", false);
            health.put("errorMessage", "Database Connection Error: " + (e.getMessage() != null ? e.getMessage() : "Unable to reach database"));
            health.put("databaseName", "neondb");
            health.put("serverVersion", "Disconnected");
            health.put("productName", "PostgreSQL");
            health.put("host", "Neon Cloud");
            health.put("port", "5432");
            health.put("totalTables", 0);
            health.put("totalRecords", 0);
            health.put("storageUsed", "0 KB");
            health.put("activeConnections", 0);
            health.put("lastBackup", lastBackupTimestamp);
        }
        return health;
    }

    public Map<String, Object> getDatabaseMonitorStats() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            long patients = getTableRowCount(conn, "patients");
            long doctors = getTableRowCount(conn, "doctors");
            if (doctors == 0) doctors = getTableRowCountWhere(conn, "staff", "LOWER(role) LIKE '%doctor%'");
            long staff = getTableRowCount(conn, "staff");
            long pharmacists = getTableRowCountWhere(conn, "staff", "LOWER(role) LIKE '%pharmacist%'");
            long labTechs = getTableRowCountWhere(conn, "staff", "LOWER(role) LIKE '%lab%' OR LOWER(role) LIKE '%technician%'");
            long medicines = getTableRowCount(conn, "medicines");
            if (medicines == 0) medicines = getTableRowCount(conn, "pharmacy_inventory");
            long appts = getTableRowCountWhere(conn, "appointments", "appointment_date = CURRENT_DATE OR DATE(created_at) = CURRENT_DATE OR appointment_date = TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD')");
            
            double revenue = getDoubleSum(conn, "SELECT COALESCE(SUM(total_amount), 0) FROM billing WHERE DATE(created_at) = CURRENT_DATE") + 
                             getDoubleSum(conn, "SELECT COALESCE(SUM(amount), 0) FROM lab_payments WHERE DATE(created_at) = CURRENT_DATE") +
                             getDoubleSum(conn, "SELECT COALESCE(SUM(total_amount), 0) FROM pharmacy_orders WHERE LOWER(payment_status)='paid' AND DATE(created_at) = CURRENT_DATE");

            long pendingBills = getTableRowCountWhere(conn, "pharmacy_orders", "LOWER(payment_status)='unpaid'");
            long onlineDocs = getTableRowCountWhere(conn, "doctors", "LOWER(status)='online'");
            long offlineDocs = getTableRowCountWhere(conn, "doctors", "LOWER(status)='offline'");
            long consults = getTableRowCountWhere(conn, "online_consultations", "LOWER(meeting_status)='completed'");
            if (consults == 0) consults = getTableRowCountWhere(conn, "online_consultation", "LOWER(meeting_status)='completed'");
            long labReports = getTableRowCountWhere(conn, "laboratory_reports", "LOWER(status)='pending'");
            if (labReports == 0) labReports = getTableRowCountWhere(conn, "lab_bookings", "LOWER(status)='pending'");
            long lowStock = getTableRowCountWhere(conn, "medicines", "stock_quantity <= minimum_stock");
            long expiredMeds = getTableRowCountWhere(conn, "medicines", "LOWER(status)='expired'");

            stats.put("totalPatients", patients);
            stats.put("totalStaff", staff);
            stats.put("totalDoctors", doctors);
            stats.put("totalPharmacists", pharmacists);
            stats.put("totalLabTechs", labTechs);
            stats.put("totalMedicines", medicines);
            stats.put("todayAppointments", appts);
            stats.put("todayRevenue", revenue);
            stats.put("pendingBills", pendingBills);
            stats.put("onlineDoctors", onlineDocs);
            stats.put("offlineDoctors", offlineDocs);
            stats.put("completedConsultations", consults);
            stats.put("pendingLabReports", labReports);
            stats.put("lowStockMedicines", lowStock);
            stats.put("expiredMedicines", expiredMeds);
        } catch (SQLException e) {
            System.err.println("[SQL Exception] Error fetching database monitor stats: " + e.getMessage());
            e.printStackTrace();
            stats.put("totalPatients", 0);
            stats.put("totalStaff", 0);
            stats.put("totalDoctors", 0);
            stats.put("totalPharmacists", 0);
            stats.put("totalLabTechs", 0);
            stats.put("totalMedicines", 0);
            stats.put("todayAppointments", 0);
            stats.put("todayRevenue", 0.0);
            stats.put("pendingBills", 0);
            stats.put("onlineDoctors", 0);
            stats.put("offlineDoctors", 0);
            stats.put("completedConsultations", 0);
            stats.put("pendingLabReports", 0);
            stats.put("lowStockMedicines", 0);
            stats.put("expiredMedicines", 0);
        }
        return stats;
    }

    private long getTableRowCount(Connection conn, String tableName) {
        return getTableRowCountWhere(conn, tableName, null);
    }

    private long getTableRowCountWhere(Connection conn, String tableName, String whereClause) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("[SQL Exception] Error executing count: " + sql + " | Reason: " + e.getMessage());
        }
        return 0;
    }

    private double getDoubleSum(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[SQL Exception] Error executing sum: " + sql + " | Reason: " + e.getMessage());
        }
        return 0.0;
    }

    // --- 2. TABLE SCHEMA & DATA INTROSPECTION ---
    public List<Map<String, String>> getTableColumns(String tableName) {
        List<Map<String, String>> columns = new ArrayList<>();
        try (Connection conn = DBConnection.getValidatedConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    Map<String, String> col = new HashMap<>();
                    col.put("name", rs.getString("COLUMN_NAME"));
                    col.put("type", rs.getString("TYPE_NAME"));
                    col.put("size", String.valueOf(rs.getInt("COLUMN_SIZE")));
                    col.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable ? "YES" : "NO");
                    columns.add(col);
                }
            }
        } catch (Exception e) {
            System.err.println("[SQL Exception] Error fetching columns for " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        }
        return columns;
    }

    public Map<String, Object> getTableData(String tableName, int page, int pageSize, String search, String filter) {
        Map<String, Object> result = new HashMap<>();
        List<String> columns = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();
        long totalRecords = 0;

        String sanitizeTable = sanitizeIdentifier(tableName);
        if (sanitizeTable == null) {
            sanitizeTable = "patients";
        }

        try (Connection conn = DBConnection.getValidatedConnection()) {
            // Get columns
            String querySql = "SELECT * FROM " + sanitizeTable;
            StringBuilder whereClause = new StringBuilder();

            if (search != null && !search.trim().isEmpty()) {
                String s = search.trim();
                // Build dynamic OR condition across text columns
                List<Map<String, String>> cols = getTableColumns(sanitizeTable);
                List<String> searchConds = new ArrayList<>();
                for (Map<String, String> c : cols) {
                    searchConds.add("LOWER(" + c.get("name") + ") LIKE ?");
                }
                if (!searchConds.isEmpty()) {
                    whereClause.append(" WHERE (").append(String.join(" OR ", searchConds)).append(")");
                }
            }

            if (filter != null && !filter.trim().isEmpty()) {
                String fClause = parseFilterClause(filter);
                if (fClause != null && !fClause.isEmpty()) {
                    if (whereClause.length() > 0) {
                        whereClause.append(" AND (").append(fClause).append(")");
                    } else {
                        whereClause.append(" WHERE (").append(fClause).append(")");
                    }
                }
            }

            // Total Count
            String countSql = "SELECT COUNT(*) FROM " + sanitizeTable + whereClause.toString();
            try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                if (search != null && !search.trim().isEmpty()) {
                    String q = "%" + search.trim().toLowerCase() + "%";
                    List<Map<String, String>> cols = getTableColumns(sanitizeTable);
                    for (int i = 1; i <= cols.size(); i++) {
                        psCount.setString(i, q);
                    }
                }
                try (ResultSet rsCount = psCount.executeQuery()) {
                    if (rsCount.next()) totalRecords = rsCount.getLong(1);
                }
            }

            // Data Query
            int offset = Math.max(0, (page - 1) * pageSize);
            String dataSql = querySql + whereClause.toString() + " LIMIT " + pageSize + " OFFSET " + offset;
            try (PreparedStatement psData = conn.prepareStatement(dataSql)) {
                if (search != null && !search.trim().isEmpty()) {
                    String q = "%" + search.trim().toLowerCase() + "%";
                    List<Map<String, String>> cols = getTableColumns(sanitizeTable);
                    for (int i = 1; i <= cols.size(); i++) {
                        psData.setString(i, q);
                    }
                }
                try (ResultSet rsData = psData.executeQuery()) {
                    ResultSetMetaData rsMeta = rsData.getMetaData();
                    int colCount = rsMeta.getColumnCount();
                    for (int i = 1; i <= colCount; i++) {
                        columns.add(rsMeta.getColumnName(i));
                    }
                    while (rsData.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rsData.getObject(i);
                            row.add(val != null ? val.toString() : "");
                        }
                        rows.add(row);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SQL Exception] Error querying table " + sanitizeTable + ": " + e.getMessage());
            e.printStackTrace();
        }

        if (columns.isEmpty()) {
            List<Map<String, String>> schemaCols = getTableColumns(sanitizeTable);
            for (Map<String, String> sc : schemaCols) {
                columns.add(sc.get("name"));
            }
        }

        result.put("tableName", sanitizeTable);
        result.put("columns", columns);
        result.put("rows", rows);
        result.put("totalRecords", totalRecords);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", totalRecords > 0 ? (int) Math.ceil((double) totalRecords / pageSize) : 1);
        return result;
    }

    private String parseFilterClause(String filter) {
        if ("today".equalsIgnoreCase(filter)) {
            return "created_at LIKE '" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "%' OR booking_date LIKE '%Today%' OR order_date LIKE '" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "%'";
        } else if ("pending".equalsIgnoreCase(filter)) {
            return "LOWER(status)='pending' OR LOWER(payment_status)='unpaid' OR LOWER(meeting_status)='scheduled'";
        } else if ("completed".equalsIgnoreCase(filter) || "approved".equalsIgnoreCase(filter)) {
            return "LOWER(status)='completed' OR LOWER(payment_status)='paid' OR LOWER(status)='confirmed' OR LOWER(meeting_status)='completed'";
        } else if ("cancelled".equalsIgnoreCase(filter)) {
            return "LOWER(status)='cancelled' OR LOWER(meeting_status)='cancelled'";
        } else if ("online".equalsIgnoreCase(filter)) {
            return "LOWER(status)='online'";
        } else if ("offline".equalsIgnoreCase(filter)) {
            return "LOWER(status)='offline'";
        }
        return "";
    }

    // --- 3. DYNAMIC CRUD VIA PREPAREDSTATEMENT ---
    public boolean insertRecord(String tableName, Map<String, String> data, String adminCode) {
        String safeTable = sanitizeIdentifier(tableName);
        if (safeTable == null || data == null || data.isEmpty()) return false;

        List<String> cols = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        for (Map.Entry<String, String> e : data.entrySet()) {
            String col = sanitizeIdentifier(e.getKey());
            if (col != null) {
                cols.add(col);
                placeholders.add("?");
                vals.add(e.getValue());
            }
        }

        if (cols.isEmpty()) return false;

        String sql = "INSERT INTO " + safeTable + " (" + String.join(", ", cols) + ") VALUES (" + String.join(", ", placeholders) + ")";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < vals.size(); i++) {
                ps.setString(i + 1, vals.get(i));
            }
            int updated = ps.executeUpdate();
            if (updated > 0) {
                logAdminAction(adminCode, "INSERT", safeTable, data.values().iterator().next());
                return true;
            }
        } catch (Exception e) {
            System.err.println("[SQL Exception] Error inserting into " + safeTable + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRecord(String tableName, String pkCol, String pkVal, Map<String, String> data, String adminCode) {
        String safeTable = sanitizeIdentifier(tableName);
        String safePk = sanitizeIdentifier(pkCol);
        if (safeTable == null || safePk == null || data == null || data.isEmpty()) return false;

        List<String> setPairs = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        for (Map.Entry<String, String> e : data.entrySet()) {
            String col = sanitizeIdentifier(e.getKey());
            if (col != null && !col.equalsIgnoreCase(safePk)) {
                setPairs.add(col + " = ?");
                vals.add(e.getValue());
            }
        }

        if (setPairs.isEmpty()) return false;
        vals.add(pkVal); // for WHERE clause

        String sql = "UPDATE " + safeTable + " SET " + String.join(", ", setPairs) + " WHERE " + safePk + " = ?";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < vals.size(); i++) {
                ps.setString(i + 1, vals.get(i));
            }
            int updated = ps.executeUpdate();
            if (updated > 0) {
                logAdminAction(adminCode, "UPDATE", safeTable, pkVal);
                return true;
            }
        } catch (Exception e) {
            System.err.println("[SQL Exception] Error updating " + safeTable + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRecord(String tableName, String pkCol, String pkVal, boolean softDelete, String adminCode) {
        String safeTable = sanitizeIdentifier(tableName);
        String safePk = sanitizeIdentifier(pkCol);
        if (safeTable == null || safePk == null || pkVal == null) return false;

        if (softDelete) {
            // Attempt soft delete by setting status='Deleted' or is_deleted=1
            String sql = "UPDATE " + safeTable + " SET status = 'Deleted' WHERE " + safePk + " = ?";
            try (Connection conn = DBConnection.getValidatedConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pkVal);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    logAdminAction(adminCode, "SOFT DELETE", safeTable, pkVal);
                    return true;
                }
            } catch (Exception ignored) {}
        }

        // Hard Delete
        String sql = "DELETE FROM " + safeTable + " WHERE " + safePk + " = ?";
        try (Connection conn = DBConnection.getValidatedConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pkVal);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                logAdminAction(adminCode, "DELETE", safeTable, pkVal);
                return true;
            }
        } catch (Exception e) {
            System.err.println("[SQL Exception] Error deleting from " + safeTable + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // --- 4. SQL QUERY CONSOLE ---
    public Map<String, Object> executeCustomQuery(String querySql, String adminRole) {
        Map<String, Object> res = new HashMap<>();
        long start = System.currentTimeMillis();

        if (querySql == null || querySql.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Query string cannot be empty.");
            return res;
        }

        String clean = querySql.trim();
        String upper = clean.toUpperCase();

        // Security check
        if (upper.contains("DROP DATABASE") || upper.contains("TRUNCATE DATABASE")) {
            res.put("success", false);
            res.put("message", "Security Alert: Destructive query 'DROP / TRUNCATE DATABASE' is prohibited.");
            return res;
        }

        boolean isSelect = upper.startsWith("SELECT") || upper.startsWith("SHOW") || upper.startsWith("DESCRIBE") || upper.startsWith("EXPLAIN");

        try (Connection conn = DBConnection.getValidatedConnection();
             Statement stmt = conn.createStatement()) {

            if (isSelect) {
                try (ResultSet rs = stmt.executeQuery(clean)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();
                    List<String> cols = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        cols.add(meta.getColumnName(i));
                    }
                    List<List<Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            row.add(val != null ? val.toString() : "");
                        }
                        rows.add(row);
                    }
                    long elapsed = System.currentTimeMillis() - start;
                    res.put("success", true);
                    res.put("columns", cols);
                    res.put("rows", rows);
                    res.put("rowsReturned", rows.size());
                    res.put("executionTimeMs", elapsed);
                    res.put("status", "Query executed successfully (" + rows.size() + " rows returned in " + elapsed + "ms)");
                }
            } else {
                int affected = stmt.executeUpdate(clean);
                long elapsed = System.currentTimeMillis() - start;
                res.put("success", true);
                res.put("rowsAffected", affected);
                res.put("executionTimeMs", elapsed);
                res.put("status", "Statement executed successfully (" + affected + " rows affected in " + elapsed + "ms)");
            }
        } catch (Exception e) {
            System.err.println("[SQL Exception] Error executing custom query: " + e.getMessage());
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "SQL Error: " + e.getMessage());
            res.put("executionTimeMs", System.currentTimeMillis() - start);
        }
        return res;
    }

    // --- 5. EXPORT & IMPORT ---
    public String exportTableToSQL(String tableName) {
        String safeTable = sanitizeIdentifier(tableName);
        if (safeTable == null) return "-- Invalid table name";

        StringBuilder sb = new StringBuilder();
        sb.append("-- Niramaya Hospitals Database Dump\n");
        sb.append("-- Table: ").append(safeTable).append("\n");
        sb.append("-- Generated At: ").append(LocalDateTime.now().format(FMT)).append("\n\n");

        try (Connection conn = DBConnection.getValidatedConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + safeTable)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            List<String> cols = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) cols.add(meta.getColumnName(i));

            while (rs.next()) {
                List<String> vals = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    Object obj = rs.getObject(i);
                    if (obj == null) {
                        vals.add("NULL");
                    } else {
                        vals.add("'" + obj.toString().replace("'", "''") + "'");
                    }
                }
                sb.append("INSERT INTO ").append(safeTable).append(" (")
                  .append(String.join(", ", cols)).append(") VALUES (")
                  .append(String.join(", ", vals)).append(");\n");
            }
        } catch (Exception e) {
            sb.append("-- Error exporting SQL: ").append(e.getMessage());
        }
        return sb.toString();
    }

    public String exportTableToCSV(String tableName) {
        String safeTable = sanitizeIdentifier(tableName);
        if (safeTable == null) return "Invalid Table";

        StringBuilder sb = new StringBuilder();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + safeTable)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                sb.append("\"").append(meta.getColumnName(i)).append("\"").append(i < colCount ? "," : "\n");
            }

            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    Object obj = rs.getObject(i);
                    String val = obj != null ? obj.toString().replace("\"", "\"\"") : "";
                    sb.append("\"").append(val).append("\"").append(i < colCount ? "," : "\n");
                }
            }
        } catch (Exception e) {
            sb.append("Error exporting CSV: ").append(e.getMessage());
        }
        return sb.toString();
    }

    // --- 6. BACKUP & RESTORE ---
    public Map<String, Object> createFullBackup() {
        Map<String, Object> result = new HashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "Niramaya_DB_Backup_" + timestamp + ".sql";
        File dir = new File("Reports/Backups");
        if (!dir.exists()) dir.mkdirs();

        File backupFile = new File(dir, backupFileName);
        try (FileWriter fw = new FileWriter(backupFile)) {
            fw.write("-- NIRAMAYA HOSPITALS MASTER DATABASE BACKUP\n");
            fw.write("-- Date: " + LocalDateTime.now().format(FMT) + "\n\n");

            for (Map<String, String> meta : ALL_TABLE_METADATA) {
                String tableName = meta.get("name");
                fw.write(exportTableToSQL(tableName));
                fw.write("\n\n");
            }

            lastBackupTimestamp = LocalDateTime.now().format(FMT);
            lastBackupPath = backupFile.getAbsolutePath();

            result.put("success", true);
            result.put("backupName", backupFileName);
            result.put("backupDate", lastBackupTimestamp);
            result.put("backupPath", lastBackupPath);
            result.put("fileSize", String.format("%.2f KB", backupFile.length() / 1024.0));

            logAdminAction("EMP-000004", "BACKUP DATABASE", "ALL TABLES", backupFileName);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error generating database backup: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> restoreFullBackup(String backupContent) {
        Map<String, Object> result = new HashMap<>();
        if (backupContent == null || backupContent.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Backup SQL content is empty.");
            return result;
        }

        int executedCount = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String statement : backupContent.split(";")) {
                String st = statement.trim();
                if (!st.isEmpty() && !st.startsWith("--")) {
                    stmt.execute(st);
                    executedCount++;
                }
            }
            result.put("success", true);
            result.put("executedStatements", executedCount);
            result.put("message", "Database successfully restored (" + executedCount + " SQL statements executed).");
            logAdminAction("EMP-000004", "RESTORE DATABASE", "ALL TABLES", "Backup SQL File");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error during database restore: " + e.getMessage());
        }
        return result;
    }

    // --- HELPER UTILITIES ---
    private void logAdminAction(String adminCode, String action, String tableName, String recordId) {
        ActivityLogDAO actDAO = new ActivityLogDAO();
        actDAO.logActivity(
            adminCode != null ? adminCode : "EMP-000004",
            "Admin",
            "Administrator",
            "DATABASE_MANAGER",
            action + " [" + tableName + " - ID: " + recordId + "]",
            "Success",
            "127.0.0.1"
        );
    }

    private String sanitizeIdentifier(String name) {
        if (name == null) return null;
        String clean = name.replaceAll("[^a-zA-Z0-9_]", "");
        return clean.isEmpty() ? null : clean;
    }
}
