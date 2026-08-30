package com.hospital.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.HashMap;

/**
 * JDBC Database Connection Manager for Niramaya Hospitals.
 * Supports MySQL & SQLite embedded fallback for immediate zero-config execution.
 */
public class DBConnection {

    // Neon PostgreSQL configuration parameters
    private static final String DEFAULT_NEON_URL = "jdbc:postgresql://ep-old-boat-axlruvbe-pooler.c-4.us-east-2.aws.neon.tech:5432/neondb?sslmode=require&connectTimeout=5&socketTimeout=5";
    private static final String POSTGRES_URL = System.getenv("NEON_DB_URL") != null ? 
            System.getenv("NEON_DB_URL") : 
            (System.getenv("POSTGRES_URL") != null ? System.getenv("POSTGRES_URL") : DEFAULT_NEON_URL);
    private static final String POSTGRES_USER = System.getenv("NEON_DB_USER") != null ? 
            System.getenv("NEON_DB_USER") : 
            (System.getenv("POSTGRES_USER") != null ? System.getenv("POSTGRES_USER") : "neondb_owner");
    private static final String POSTGRES_PASS = System.getenv("NEON_DB_PASS") != null ? 
            System.getenv("NEON_DB_PASS") : 
            (System.getenv("POSTGRES_PASS") != null ? System.getenv("POSTGRES_PASS") : "npg_u1LNWJ5fVmvs");

    // Default MySQL configuration parameters
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/niramaya_hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true&connectTimeout=2000";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "root";

    // SQLite embedded database fallback file
    private static final String SQLITE_URL = "jdbc:sqlite:niramaya_hospitals.db";

    private static boolean isPostgreSQLAvailable = false;
    private static boolean isMySQLAvailable = false;
    private static volatile long lastPostgresAttemptTime = 0;
    private static final long POSTGRES_RETRY_INTERVAL_MS = 60000; // 60s cooldown if PostgreSQL failed

    static {
        // Verify Neon PostgreSQL connection when application starts
        verifyNeonPostgreSQLConnection();

        // Initialize tables on startup
        try {
            initTables();
            verifyTablesExist();
        } catch (Exception e) {
            System.err.println("[SQL Exception] DB Connection Init Warning: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final long SERVER_START_TIME = System.currentTimeMillis();
    private static volatile String lastSQLException = "None";
    private static volatile String lastQueryExecuted = "None";

    public static String getLastSQLException() {
        return lastSQLException;
    }

    public static String getLastQueryExecuted() {
        return lastQueryExecuted;
    }

    public static void logQuery(String sql) {
        if (sql != null && !sql.trim().isEmpty()) {
            lastQueryExecuted = sql;
            System.out.println("[SQL QUERY] " + sql);
        }
    }

    public static void logSQLException(Throwable ex) {
        if (ex == null) return;
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        lastSQLException = msg;
        System.err.println("[SQL EXCEPTION] " + msg);
        ex.printStackTrace();
    }

    private static Connection wrapConnection(Connection conn, String dbType) {
        if (conn == null) return null;
        System.out.println("[JDBC OPEN] Connection opened to " + dbType + " at " + new java.util.Date());
        return (Connection) java.lang.reflect.Proxy.newProxyInstance(
            DBConnection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, methodArgs) -> {
                String name = method.getName();
                if ("close".equals(name)) {
                    System.out.println("[JDBC CLOSE] Connection closed for " + dbType + " at " + new java.util.Date());
                } else if ("prepareStatement".equals(name) || "createStatement".equals(name) || "prepareCall".equals(name)) {
                    if (methodArgs != null && methodArgs.length > 0 && methodArgs[0] instanceof String) {
                        logQuery((String) methodArgs[0]);
                    }
                }
                try {
                    Object result = method.invoke(conn, methodArgs);
                    if (result instanceof Statement && !("createStatement".equals(name) || "prepareStatement".equals(name))) {
                        return wrapStatement((Statement) result);
                    }
                    return result;
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
                    logSQLException(cause);
                    throw cause;
                }
            }
        );
    }

    private static Statement wrapStatement(Statement stmt) {
        if (stmt == null) return null;
        Class<?>[] interfaces = stmt instanceof java.sql.PreparedStatement ? 
            new Class<?>[]{java.sql.PreparedStatement.class} : new Class<?>[]{Statement.class};
        return (Statement) java.lang.reflect.Proxy.newProxyInstance(
            DBConnection.class.getClassLoader(),
            interfaces,
            (proxy, method, methodArgs) -> {
                String name = method.getName();
                if (name.startsWith("execute")) {
                    if (methodArgs != null && methodArgs.length > 0 && methodArgs[0] instanceof String) {
                        logQuery((String) methodArgs[0]);
                    }
                }
                try {
                    return method.invoke(stmt, methodArgs);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    Throwable cause = ite.getCause() != null ? ite.getCause() : ite;
                    logSQLException(cause);
                    throw cause;
                }
            }
        );
    }

    public static boolean isPostgreSQL() {
        return isPostgreSQLAvailable;
    }

    public static boolean isMySQL() {
        return isMySQLAvailable;
    }

    public static boolean verifyNeonPostgreSQLConnection() {
        Map<String, Object> diag = performJDBCDiagnosis();
        return Boolean.TRUE.equals(diag.get("connected"));
    }

    public static Map<String, Object> performJDBCDiagnosis() {
        Map<String, Object> diag = new java.util.LinkedHashMap<>();

        // 1. JDBC URL (masked password)
        String rawUrl = POSTGRES_URL;
        String maskedUrl = rawUrl.replaceAll(":[^/@]+@", ":****@");
        diag.put("jdbcUrl", maskedUrl);

        // 2. Extracted Host, Port, Database, Username
        String host = "ep-old-boat-axlruvbe-pooler.c-4.us-east-2.aws.neon.tech";
        String port = "5432";
        String database = "neondb";
        try {
            String clean = rawUrl.replace("jdbc:postgresql://", "");
            if (clean.contains("?")) clean = clean.substring(0, clean.indexOf("?"));
            String[] parts = clean.split("/");
            if (parts.length > 0) {
                String hostPort = parts[0];
                if (hostPort.contains("@")) hostPort = hostPort.substring(hostPort.indexOf("@") + 1);
                if (hostPort.contains(":")) {
                    String[] hp = hostPort.split(":");
                    host = hp[0];
                    port = hp[1];
                } else {
                    host = hostPort;
                }
            }
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                database = parts[1];
            }
        } catch (Exception ignored) {}

        diag.put("host", host);
        diag.put("port", port);
        diag.put("database", database);
        diag.put("username", POSTGRES_USER);

        // 3. Verify PostgreSQL JDBC Driver is loaded
        boolean driverLoaded = false;
        String driverExceptionMessage = null;
        try {
            Class.forName("org.postgresql.Driver");
            driverLoaded = true;
        } catch (Throwable t) {
            driverLoaded = false;
            java.io.StringWriter sw = new java.io.StringWriter();
            t.printStackTrace(new java.io.PrintWriter(sw));
            driverExceptionMessage = sw.toString();
        }
        diag.put("driverLoaded", driverLoaded);
        diag.put("driverException", driverExceptionMessage);

        // 4. Verify Environment Variables
        Map<String, Boolean> envStatus = new java.util.LinkedHashMap<>();
        envStatus.put("POSTGRES_URL", System.getenv("POSTGRES_URL") != null);
        envStatus.put("POSTGRES_USER", System.getenv("POSTGRES_USER") != null);
        envStatus.put("POSTGRES_PASS", System.getenv("POSTGRES_PASS") != null);
        envStatus.put("NEON_DB_URL", System.getenv("NEON_DB_URL") != null);
        envStatus.put("NEON_DB_USER", System.getenv("NEON_DB_USER") != null);
        envStatus.put("NEON_DB_PASS", System.getenv("NEON_DB_PASS") != null);
        diag.put("environmentVariables", envStatus);

        // 5, 6, 7, 8. Connection Test & SELECT 1 Execution
        boolean connected = false;
        boolean select1Success = false;
        String connectionMessage = "";
        String sqlState = "N/A";
        int vendorErrorCode = -1;
        String stackTraceStr = "None";
        String rootCauseStr = "None";
        String originalExceptionMsg = "None";

        System.out.println("=========================================================");
        System.out.println(" NEON POSTGRESQL JDBC DIAGNOSIS REPORT");
        System.out.println("=========================================================");
        System.out.println("1. JDBC URL: " + maskedUrl);
        System.out.println("2. Connection Parameters:");
        System.out.println("   - Host: " + host);
        System.out.println("   - Port: " + port);
        System.out.println("   - Database: " + database);
        System.out.println("   - Username: " + POSTGRES_USER);
        System.out.println("3. PostgreSQL Driver Loaded: " + (driverLoaded ? "TRUE 🟢" : "FALSE 🔴 (" + driverExceptionMessage + ")"));
        System.out.println("4. Environment Variables Check:");
        for (Map.Entry<String, Boolean> entry : envStatus.entrySet()) {
            System.out.println("   - " + entry.getKey() + ": " + (entry.getValue() ? "EXISTS 🟢" : "NOT SET 🔴"));
        }
        System.out.println("5. Target URL: " + rawUrl);
        System.out.println("6. Attempting Connection & Executing SELECT 1...");

        if (driverLoaded) {
            try {
                DriverManager.setLoginTimeout(5);
            } catch (Exception ignored) {}
            try (Connection conn = DriverManager.getConnection(rawUrl, POSTGRES_USER, POSTGRES_PASS);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {

                if (rs.next()) {
                    connected = true;
                    select1Success = true;
                    connectionMessage = "Database Connected Successfully";
                    System.out.println("7. Result: Database Connected Successfully 🟢");
                    isPostgreSQLAvailable = true;
                }
            } catch (Throwable t) {
                connected = false;
                select1Success = false;
                isPostgreSQLAvailable = false;

                originalExceptionMsg = t.getMessage() != null ? t.getMessage() : t.toString();
                lastSQLException = originalExceptionMsg;

                if (t instanceof SQLException) {
                    SQLException sqlEx = (SQLException) t;
                    sqlState = sqlEx.getSQLState() != null ? sqlEx.getSQLState() : "UNKNOWN";
                    vendorErrorCode = sqlEx.getErrorCode();
                }

                Throwable rootCause = t;
                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }
                rootCauseStr = rootCause.toString();

                java.io.StringWriter sw = new java.io.StringWriter();
                t.printStackTrace(new java.io.PrintWriter(sw));
                stackTraceStr = sw.toString();

                connectionMessage = originalExceptionMsg;

                System.err.println("8. CONNECTION FAILED 🔴");
                System.err.println("   - SQLState: " + sqlState);
                System.err.println("   - Vendor Error Code: " + vendorErrorCode);
                System.err.println("   - Original Exception Message: " + originalExceptionMsg);
                System.err.println("   - Root Cause: " + rootCauseStr);
                System.err.println("   - Stack Trace:\n" + stackTraceStr);
            }
        } else {
            connectionMessage = "PostgreSQL Driver Not Loaded: " + driverExceptionMessage;
        }

        System.out.println("=========================================================");

        diag.put("connected", connected);
        diag.put("select1Success", select1Success);
        diag.put("connectionMessage", connectionMessage);
        diag.put("sqlState", sqlState);
        diag.put("vendorErrorCode", vendorErrorCode);
        diag.put("rootCause", rootCauseStr);
        diag.put("stackTrace", stackTraceStr);
        diag.put("originalExceptionMessage", originalExceptionMsg);

        return diag;
    }

    public static Connection getPostgreSQLConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASS);
            isPostgreSQLAvailable = true;
            return wrapConnection(conn, "Neon PostgreSQL (" + POSTGRES_URL + ")");
        } catch (Exception pgEx) {
            logSQLException(pgEx);
            throw new SQLException("Neon PostgreSQL Connection Error: " + pgEx.getMessage(), pgEx);
        }
    }

    public static Connection getMySQLConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            isMySQLAvailable = true;
            return wrapConnection(conn, "MySQL (" + MYSQL_URL + ")");
        } catch (Exception mysqlEx) {
            logSQLException(mysqlEx);
            throw new SQLException("MySQL Connection Error (localhost:3306/niramaya_hospital): " + mysqlEx.getMessage(), mysqlEx);
        }
    }

    public static Connection getValidatedConnection() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn != null && !conn.isClosed() && conn.isValid(5)) {
                return conn;
            }
        } catch (Exception e) {
            logSQLException(e);
            System.err.println("[SQL EXCEPTION] Connection validation failed, attempting reconnection... " + e.getMessage());
        }
        // Automatic retry connection once
        return getConnection();
    }

    public static Connection getConnection() throws SQLException {
        long now = System.currentTimeMillis();
        // 1. Try Neon PostgreSQL if configured / available or if cooldown elapsed
        boolean hasPgEnv = System.getenv("POSTGRES_URL") != null || System.getenv("NEON_DB_URL") != null;
        if (isPostgreSQLAvailable || (hasPgEnv && (now - lastPostgresAttemptTime > POSTGRES_RETRY_INTERVAL_MS))) {
            try {
                Class.forName("org.postgresql.Driver");
                lastPostgresAttemptTime = now;
                Connection conn = DriverManager.getConnection(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASS);
                isPostgreSQLAvailable = true;
                return wrapConnection(conn, "Neon PostgreSQL (" + POSTGRES_URL + ")");
            } catch (Exception pgEx) {
                isPostgreSQLAvailable = false;
                lastPostgresAttemptTime = now;
                logSQLException(pgEx);
            }
        }

        // 2. Try MySQL fallback
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            isMySQLAvailable = true;
            return wrapConnection(conn, "MySQL (" + MYSQL_URL + ")");
        } catch (Exception mysqlEx) {
            isMySQLAvailable = false;
            // 3. Fall back gracefully to SQLite embedded database
            try {
                Class.forName("org.sqlite.JDBC");
                Connection conn = DriverManager.getConnection(SQLITE_URL);
                return wrapConnection(conn, "SQLite Embedded (" + SQLITE_URL + ")");
            } catch (Exception sqliteEx) {
                logSQLException(sqliteEx);
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    return wrapConnection(conn, "SQLite In-Memory");
                } catch (Exception inMemEx) {
                    logSQLException(inMemEx);
                    throw new SQLException("Could not establish JDBC connection to Neon PostgreSQL / Fallback DB");
                }
            }
        }
    }

    public static java.util.Map<String, Object> getDebugDatabaseStatus() {
        java.util.Map<String, Object> debug = performJDBCDiagnosis();

        long uptimeSeconds = (System.currentTimeMillis() - SERVER_START_TIME) / 1000;
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long secs = uptimeSeconds % 60;
        String uptimeStr = String.format("%d hours, %d mins, %d secs", hours, minutes, secs);

        boolean isConnValid = false;
        int tableCount = 0;
        String pgVer = "PostgreSQL";
        String curUser = String.valueOf(debug.get("username"));
        String curDb = String.valueOf(debug.get("database"));

        if (Boolean.TRUE.equals(debug.get("connected"))) {
            try (Connection conn = getValidatedConnection()) {
                isConnValid = conn.isValid(5);
                DatabaseMetaData meta = conn.getMetaData();
                curUser = meta.getUserName();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT version()")) {
                    if (rs.next()) pgVer = rs.getString(1);
                } catch (Exception ignored) {}

                try (ResultSet rs = meta.getTables(null, "public", "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        String tName = rs.getString("TABLE_NAME");
                        if (tName != null && !tName.startsWith("pg_") && !tName.startsWith("sql_")) {
                            tableCount++;
                        }
                    }
                }
            } catch (Exception e) {
                logSQLException(e);
            }
        }

        debug.put("currentDatabase", curDb);
        debug.put("currentUser", curUser);
        debug.put("postgresVersion", pgVer);
        debug.put("numberOfTables", tableCount);
        debug.put("connectionValid", isConnValid);
        debug.put("lastSQLException", lastSQLException);
        debug.put("lastQueryExecuted", lastQueryExecuted);
        debug.put("serverUptime", uptimeStr);

        System.out.println("[DEBUG ENDPOINT] /api/debug/database -> connected=" + debug.get("connected") + ", valid=" + isConnValid + ", tables=" + tableCount);
        return debug;
    }

    public static void initTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. DOCTORS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS doctors (" +
                    "doctor_id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "doctor_name VARCHAR(100), " +
                    "phone VARCHAR(20), " +
                    "phone_number VARCHAR(20), " +
                    "age INT, " +
                    "gender VARCHAR(10), " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100), " +
                    "qualification VARCHAR(100), " +
                    "category VARCHAR(100), " +
                    "department VARCHAR(50), " +
                    "specialization VARCHAR(100), " +
                    "consultation_fees DECIMAL(10,2), " +
                    "consultation_fee DECIMAL(10,2), " +
                    "working_days VARCHAR(100), " +
                    "available_days VARCHAR(100), " +
                    "working_hours VARCHAR(100), " +
                    "available_time VARCHAR(100), " +
                    "profile_photo TEXT, " +
                    "available_status VARCHAR(20) DEFAULT 'Online', " +
                    "status VARCHAR(20) DEFAULT 'Online', " +
                    "appointment_available BOOLEAN DEFAULT true, " +
                    "accept_appointments VARCHAR(10) DEFAULT 'Yes', " +
                    "created_at VARCHAR(50), " +
                    "created_date VARCHAR(50)" +
                    ");");

            // Ensure missing columns exist in existing doctors table
            String[] doctorAlters = new String[]{
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS name VARCHAR(100)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS phone VARCHAR(20)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS age INT",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS gender VARCHAR(10)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS password VARCHAR(100)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS category VARCHAR(100)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS consultation_fees DECIMAL(10,2)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS working_days VARCHAR(100)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS working_hours VARCHAR(100)",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS available_status VARCHAR(20) DEFAULT 'Online'",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS appointment_available BOOLEAN DEFAULT true",
                "ALTER TABLE doctors ADD COLUMN IF NOT EXISTS created_at VARCHAR(50)"
            };
            for (String alterSql : doctorAlters) {
                try { stmt.execute(alterSql); } catch (Exception ignored) {}
            }

            // 1B. PHARMACY STAFF TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS pharmacy_staff (" +
                    "staff_id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "age INT, " +
                    "gender VARCHAR(10), " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "qualification VARCHAR(100), " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 1C. LAB TECHNICIANS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS lab_technicians (" +
                    "technician_id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "age INT, " +
                    "gender VARCHAR(10), " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "qualification VARCHAR(100), " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 2. PATIENTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "patient_id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "age INT, " +
                    "gender VARCHAR(10), " +
                    "blood_group VARCHAR(10)" +
                    ");");

            // 3. APPOINTMENTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS appointments (" +
                    "appointment_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50) NOT NULL, " +
                    "doctor_name VARCHAR(100), " +
                    "department VARCHAR(50), " +
                    "appointment_date VARCHAR(30), " +
                    "appointment_time VARCHAR(30), " +
                    "status VARCHAR(30) DEFAULT 'Confirmed', " +
                    "payment_status VARCHAR(30) DEFAULT 'Paid'" +
                    ");");

            // 4. PRESCRIPTIONS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS prescriptions (" +
                    "prescription_id VARCHAR(50) PRIMARY KEY, " +
                    "appointment_id VARCHAR(50), " +
                    "doctor_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "diagnosis TEXT, " +
                    "medicines TEXT, " +
                    "doctor_notes TEXT, " +
                    "follow_up VARCHAR(50), " +
                    "created_date VARCHAR(50)" +
                    ");");

            // 5. USERS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "email VARCHAR(100) PRIMARY KEY, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "name VARCHAR(100), " +
                    "phone VARCHAR(20)" +
                    ");");

            // Note: Doctor records are strictly database-driven via registration.

            // Seed default patients if empty
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO patients (patient_id, name, email, phone, age, gender, blood_group) VALUES " +
                            "('PT100842', 'Rekha Prasad', 'patient@niramaya.health', '+91 98765 43210', 28, 'Female', 'O+') ON CONFLICT (patient_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO patients (patient_id, name, email, phone, age, gender, blood_group) VALUES " +
                            "('PT100842', 'Rekha Prasad', 'patient@niramaya.health', '+91 98765 43210', 28, 'Female', 'O+');");
                }
            } catch (Exception ignored) {}

            // Seed default users if empty
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO users (email, password, role, name, phone) VALUES " +
                            "('patient@niramaya.health', 'demo1234', 'patient', 'Rekha Prasad', '+91 98765 43210'), " +
                            "('doctor@niramaya.health', 'demo1234', 'doctor', 'Dr. Ananya Rao', '+91 98765 43211'), " +
                            "('labtech@tech.in', 'demo1234', 'technician', 'Senior Pathology Specialist', '+91 98765 43213'), " +
                            "('pharmacy@niramaya.health', 'demo1234', 'pharmacy', 'Chief Pharmacist', '+91 98765 43214'), " +
                            "('admin@niramaya.health', 'demo1234', 'admin', 'Hospital Admin', '+91 98765 43212') ON CONFLICT (email) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO users (email, password, role, name, phone) VALUES " +
                            "('patient@niramaya.health', 'demo1234', 'patient', 'Rekha Prasad', '+91 98765 43210'), " +
                            "('doctor@niramaya.health', 'demo1234', 'doctor', 'Dr. Ananya Rao', '+91 98765 43211'), " +
                            "('labtech@tech.in', 'demo1234', 'technician', 'Senior Pathology Specialist', '+91 98765 43213'), " +
                            "('pharmacy@niramaya.health', 'demo1234', 'pharmacy', 'Chief Pharmacist', '+91 98765 43214'), " +
                            "('admin@niramaya.health', 'demo1234', 'admin', 'Hospital Admin', '+91 98765 43212');");
                }
            } catch (Exception ignored) {}

            // Seed default appointments if empty
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO appointments (appointment_id, patient_id, doctor_id, doctor_name, department, appointment_date, appointment_time, status, payment_status) VALUES " +
                            "('TK-1001', 'PT100842', 'DOC10084', 'Dr. Ananya Rao', 'Cardiology', '2026-08-01', '10:00 AM', 'Confirmed', 'Paid') ON CONFLICT (appointment_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO appointments (appointment_id, patient_id, doctor_id, doctor_name, department, appointment_date, appointment_time, status, payment_status) VALUES " +
                            "('TK-1001', 'PT100842', 'DOC10084', 'Dr. Ananya Rao', 'Cardiology', '2026-08-01', '10:00 AM', 'Confirmed', 'Paid');");
                }
            } catch (Exception ignored) {}

            // Seed default prescriptions if empty
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO prescriptions (prescription_id, appointment_id, doctor_id, patient_id, diagnosis, medicines, doctor_notes, follow_up, created_date) VALUES " +
                            "('RX-908124', 'TK-1001', 'DOC10084', 'PT100842', 'Acute Upper Respiratory Infection', 'Paracetamol 650mg, Amoxicillin 500mg, Vitamin D3 60K', 'Take rest and drink plenty of warm water.', '5 Days', '2026-07-29') ON CONFLICT (prescription_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO prescriptions (prescription_id, appointment_id, doctor_id, patient_id, diagnosis, medicines, doctor_notes, follow_up, created_date) VALUES " +
                            "('RX-908124', 'TK-1001', 'DOC10084', 'PT100842', 'Acute Upper Respiratory Infection', 'Paracetamol 650mg, Amoxicillin 500mg, Vitamin D3 60K', 'Take rest and drink plenty of warm water.', '5 Days', '2026-07-29');");
                }
            } catch (Exception ignored) {}

            // 6. LAB BOOKINGS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS lab_bookings (" +
                    "booking_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50), " +
                    "prescription_id VARCHAR(50), " +
                    "test_name TEXT NOT NULL, " +
                    "booking_date VARCHAR(30), " +
                    "booking_time VARCHAR(30), " +
                    "status VARCHAR(30) DEFAULT 'Pending', " +
                    "payment_status VARCHAR(30) DEFAULT 'Paid', " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 7. LAB PAYMENTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS lab_payments (" +
                    "payment_id VARCHAR(50) PRIMARY KEY, " +
                    "booking_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "amount DECIMAL(10,2) NOT NULL, " +
                    "payment_method VARCHAR(50), " +
                    "transaction_id VARCHAR(100), " +
                    "payment_status VARCHAR(30) DEFAULT 'Success', " +
                    "payment_date VARCHAR(50)" +
                    ");");

            // 8. LAB REPORTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS lab_reports (" +
                    "report_id VARCHAR(50) PRIMARY KEY, " +
                    "booking_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50), " +
                    "test_name VARCHAR(100) NOT NULL, " +
                    "result TEXT, " +
                    "observation TEXT, " +
                    "remarks TEXT, " +
                    "report_file TEXT, " +
                    "uploaded_by VARCHAR(100), " +
                    "report_date VARCHAR(50), " +
                    "status VARCHAR(30) DEFAULT 'Ready'" +
                    ");");

            // 9. MEDICINES TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS medicines (" +
                    "medicine_id VARCHAR(50) PRIMARY KEY, " +
                    "medicine_name VARCHAR(100) NOT NULL, " +
                    "generic_name VARCHAR(100), " +
                    "category VARCHAR(50) DEFAULT 'Tablet', " +
                    "strength VARCHAR(50), " +
                    "dosage_form VARCHAR(50) DEFAULT 'Tablet', " +
                    "manufacturer VARCHAR(100), " +
                    "batch_number VARCHAR(50), " +
                    "manufacturing_date VARCHAR(50), " +
                    "expiry_date VARCHAR(50), " +
                    "purchase_price DECIMAL(10,2) DEFAULT 0.0, " +
                    "selling_price DECIMAL(10,2) DEFAULT 0.0, " +
                    "unit_price DECIMAL(10,2) DEFAULT 0.0, " +
                    "gst_percentage DECIMAL(5,2) DEFAULT 12.0, " +
                    "stock_quantity INT DEFAULT 100, " +
                    "minimum_stock INT DEFAULT 15, " +
                    "rack_number VARCHAR(50) DEFAULT 'R-101', " +
                    "supplier_name VARCHAR(100) DEFAULT 'Niramaya Medical Supplies', " +
                    "supplier_contact VARCHAR(50), " +
                    "storage_instructions TEXT, " +
                    "prescription_required VARCHAR(10) DEFAULT 'No', " +
                    "description TEXT, " +
                    "status VARCHAR(50) DEFAULT 'In Stock', " +
                    "created_at VARCHAR(50), " +
                    "updated_at VARCHAR(50)" +
                    ");");

            // Migration alter statements for existing databases
            String[] medicineCols = {
                    "ALTER TABLE medicines ADD COLUMN generic_name VARCHAR(100);",
                    "ALTER TABLE medicines ADD COLUMN category VARCHAR(50) DEFAULT 'Tablet';",
                    "ALTER TABLE medicines ADD COLUMN dosage_form VARCHAR(50) DEFAULT 'Tablet';",
                    "ALTER TABLE medicines ADD COLUMN batch_number VARCHAR(50);",
                    "ALTER TABLE medicines ADD COLUMN manufacturing_date VARCHAR(50);",
                    "ALTER TABLE medicines ADD COLUMN purchase_price DECIMAL(10,2) DEFAULT 0.0;",
                    "ALTER TABLE medicines ADD COLUMN selling_price DECIMAL(10,2) DEFAULT 0.0;",
                    "ALTER TABLE medicines ADD COLUMN gst_percentage DECIMAL(5,2) DEFAULT 12.0;",
                    "ALTER TABLE medicines ADD COLUMN minimum_stock INT DEFAULT 15;",
                    "ALTER TABLE medicines ADD COLUMN rack_number VARCHAR(50) DEFAULT 'R-101';",
                    "ALTER TABLE medicines ADD COLUMN supplier_name VARCHAR(100) DEFAULT 'Niramaya Medical Supplies';",
                    "ALTER TABLE medicines ADD COLUMN supplier_contact VARCHAR(50);",
                    "ALTER TABLE medicines ADD COLUMN storage_instructions TEXT;",
                    "ALTER TABLE medicines ADD COLUMN prescription_required VARCHAR(10) DEFAULT 'No';",
                    "ALTER TABLE medicines ADD COLUMN description TEXT;",
                    "ALTER TABLE medicines ADD COLUMN status VARCHAR(50) DEFAULT 'In Stock';",
                    "ALTER TABLE medicines ADD COLUMN created_at VARCHAR(50);",
                    "ALTER TABLE medicines ADD COLUMN updated_at VARCHAR(50);"
            };
            for (String colSql : medicineCols) {
                try { stmt.execute(colSql); } catch (Exception ignored) {}
            }

            // Seed default medicines if empty
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO medicines (medicine_id, medicine_name, generic_name, category, strength, dosage_form, manufacturer, batch_number, manufacturing_date, expiry_date, purchase_price, selling_price, unit_price, gst_percentage, stock_quantity, minimum_stock, rack_number, supplier_name, supplier_contact, storage_instructions, prescription_required, description, status, created_at, updated_at) VALUES " +
                            "('MED101', 'Paracetamol 650mg', 'Paracetamol', 'Tablet', '650mg', 'Tablet', 'Apex Pharma', 'BT-9081', '2025-01-10', '2027-12-31', 30.0, 40.0, 4.0, 12.0, 250, 15, 'R-101', 'Niramaya Supplies', '+91 98765 00001', 'Store in cool dry place', 'No', 'Fever and pain relief', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED102', 'Amoxicillin 500mg', 'Amoxicillin', 'Capsule', '500mg', 'Capsule', 'Sun Pharma', 'BT-9082', '2025-02-15', '2027-08-31', 120.0, 180.0, 18.0, 12.0, 150, 15, 'R-102', 'Niramaya Supplies', '+91 98765 00001', 'Store below 25C', 'Yes', 'Antibiotic capsule', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED103', 'Vitamin D3 60K', 'Cholecalciferol', 'Capsule', '60K IU', 'Capsule', 'Cipla', 'BT-9083', '2025-03-01', '2028-03-31', 250.0, 350.0, 35.0, 12.0, 100, 10, 'R-103', 'Niramaya Supplies', '+91 98765 00001', 'Store in dry place', 'No', 'Vitamin D Supplement', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED104', 'Metformin 500mg', 'Metformin', 'Tablet', '500mg', 'Tablet', 'Torrent Pharma', 'BT-9084', '2025-01-20', '2027-11-30', 80.0, 120.0, 12.0, 12.0, 200, 20, 'R-104', 'Niramaya Supplies', '+91 98765 00001', 'Store below 30C', 'Yes', 'Anti-diabetic tablet', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED105', 'Pantoprazole 40mg', 'Pantoprazole', 'Tablet', '40mg', 'Tablet', 'Alkem Labs', 'BT-9085', '2025-02-01', '2027-09-30', 60.0, 95.0, 9.5, 12.0, 180, 15, 'R-105', 'Niramaya Supplies', '+91 98765 00001', 'Store in cool dry place', 'No', 'Antacid / GERD medication', 'In Stock', '2026-07-29', '2026-07-29') ON CONFLICT (medicine_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO medicines (medicine_id, medicine_name, generic_name, category, strength, dosage_form, manufacturer, batch_number, manufacturing_date, expiry_date, purchase_price, selling_price, unit_price, gst_percentage, stock_quantity, minimum_stock, rack_number, supplier_name, supplier_contact, storage_instructions, prescription_required, description, status, created_at, updated_at) VALUES " +
                            "('MED101', 'Paracetamol 650mg', 'Paracetamol', 'Tablet', '650mg', 'Tablet', 'Apex Pharma', 'BT-9081', '2025-01-10', '2027-12-31', 30.0, 40.0, 4.0, 12.0, 250, 15, 'R-101', 'Niramaya Supplies', '+91 98765 00001', 'Store in cool dry place', 'No', 'Fever and pain relief', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED102', 'Amoxicillin 500mg', 'Amoxicillin', 'Capsule', '500mg', 'Capsule', 'Sun Pharma', 'BT-9082', '2025-02-15', '2027-08-31', 120.0, 180.0, 18.0, 12.0, 150, 15, 'R-102', 'Niramaya Supplies', '+91 98765 00001', 'Store below 25C', 'Yes', 'Antibiotic capsule', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED103', 'Vitamin D3 60K', 'Cholecalciferol', 'Capsule', '60K IU', 'Capsule', 'Cipla', 'BT-9083', '2025-03-01', '2028-03-31', 250.0, 350.0, 35.0, 12.0, 100, 10, 'R-103', 'Niramaya Supplies', '+91 98765 00001', 'Store in dry place', 'No', 'Vitamin D Supplement', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED104', 'Metformin 500mg', 'Metformin', 'Tablet', '500mg', 'Tablet', 'Torrent Pharma', 'BT-9084', '2025-01-20', '2027-11-30', 80.0, 120.0, 12.0, 12.0, 200, 20, 'R-104', 'Niramaya Supplies', '+91 98765 00001', 'Store below 30C', 'Yes', 'Anti-diabetic tablet', 'In Stock', '2026-07-29', '2026-07-29'), " +
                            "('MED105', 'Pantoprazole 40mg', 'Pantoprazole', 'Tablet', '40mg', 'Tablet', 'Alkem Labs', 'BT-9085', '2025-02-01', '2027-09-30', 60.0, 95.0, 9.5, 12.0, 180, 15, 'R-105', 'Niramaya Supplies', '+91 98765 00001', 'Store in cool dry place', 'No', 'Antacid / GERD medication', 'In Stock', '2026-07-29', '2026-07-29');");
                }
            } catch (Exception ignored) {}

            // Seed default lab bookings & reports if empty
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO lab_bookings (booking_id, patient_id, doctor_id, prescription_id, test_name, booking_date, booking_time, status, payment_status, created_at) VALUES " +
                            "('LAB-1001', 'PT100842', 'DOC10084', 'RX-908124', 'Complete Blood Count (CBC)', '2026-07-29', '09:30 AM', 'Completed', 'Paid', '2026-07-29') ON CONFLICT (booking_id) DO NOTHING;");
                    stmt.execute("INSERT INTO lab_reports (report_id, booking_id, patient_id, doctor_id, test_name, result, observation, remarks, report_file, uploaded_by, report_date, status) VALUES " +
                            "('RPT-1001', 'LAB-1001', 'PT100842', 'DOC10084', 'Complete Blood Count (CBC)', 'Normal', 'Hb: 13.5 g/dL, WBC: 6,500 /mcL, Platelets: 250,000 /mcL', 'All parameters within healthy reference range', '', 'Senior Pathology Specialist', '2026-07-29', 'Ready') ON CONFLICT (report_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO lab_bookings (booking_id, patient_id, doctor_id, prescription_id, test_name, booking_date, booking_time, status, payment_status, created_at) VALUES " +
                            "('LAB-1001', 'PT100842', 'DOC10084', 'RX-908124', 'Complete Blood Count (CBC)', '2026-07-29', '09:30 AM', 'Completed', 'Paid', '2026-07-29');");
                    stmt.execute("INSERT OR IGNORE INTO lab_reports (report_id, booking_id, patient_id, doctor_id, test_name, result, observation, remarks, report_file, uploaded_by, report_date, status) VALUES " +
                            "('RPT-1001', 'LAB-1001', 'PT100842', 'DOC10084', 'Complete Blood Count (CBC)', 'Normal', 'Hb: 13.5 g/dL, WBC: 6,500 /mcL, Platelets: 250,000 /mcL', 'All parameters within healthy reference range', '', 'Senior Pathology Specialist', '2026-07-29', 'Ready');");
                }
            } catch (Exception ignored) {}

            // 10. PHARMACY ORDERS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS pharmacy_orders (" +
                    "order_id VARCHAR(50) PRIMARY KEY, " +
                    "pharmacy_token VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50), " +
                    "prescription_id VARCHAR(50), " +
                    "appointment_id VARCHAR(50), " +
                    "total_amount DECIMAL(10,2) DEFAULT 0.0, " +
                    "payment_status VARCHAR(30) DEFAULT 'Unpaid', " +
                    "order_status VARCHAR(30) DEFAULT 'Prescription Received', " +
                    "payment_method VARCHAR(50), " +
                    "transaction_id VARCHAR(100), " +
                    "order_date VARCHAR(50)" +
                    ");");

            // 11. PHARMACY ORDER ITEMS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS pharmacy_order_items (" +
                    "item_id VARCHAR(50) PRIMARY KEY, " +
                    "order_id VARCHAR(50) NOT NULL, " +
                    "medicine_id VARCHAR(50), " +
                    "medicine_name VARCHAR(100) NOT NULL, " +
                    "strength VARCHAR(50), " +
                    "dosage VARCHAR(50), " +
                    "morning INT DEFAULT 0, " +
                    "afternoon INT DEFAULT 0, " +
                    "night INT DEFAULT 0, " +
                    "duration VARCHAR(50), " +
                    "quantity INT DEFAULT 1, " +
                    "unit_price DECIMAL(10,2) DEFAULT 0.0, " +
                    "subtotal DECIMAL(10,2) DEFAULT 0.0, " +
                    "medicine_source VARCHAR(30) DEFAULT 'Inventory'" +
                    ");");

            // 12. DEPARTMENTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS departments (" +
                    "dept_id VARCHAR(50) PRIMARY KEY, " +
                    "dept_name VARCHAR(100) NOT NULL, " +
                    "head_doctor VARCHAR(100), " +
                    "total_doctors INT DEFAULT 0, " +
                    "total_patients INT DEFAULT 0" +
                    ");");

            // 13. STAFF TABLE (Unified Employee Code EMP-XXXXXX System)
            stmt.execute("CREATE TABLE IF NOT EXISTS staff (" +
                    "employee_code VARCHAR(50) PRIMARY KEY, " +
                    "role VARCHAR(50) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "mobile VARCHAR(20), " +
                    "department VARCHAR(100), " +
                    "designation VARCHAR(100), " +
                    "qualification VARCHAR(100), " +
                    "experience VARCHAR(50), " +
                    "status VARCHAR(20) DEFAULT 'Active', " +
                    "created_at VARCHAR(50), " +
                    "updated_at VARCHAR(50), " +
                    "medical_reg_no VARCHAR(100), " +
                    "specialization VARCHAR(100), " +
                    "consultation_fee DECIMAL(10,2) DEFAULT 0.0, " +
                    "license_no VARCHAR(100), " +
                    "office_extension VARCHAR(50), " +
                    "blood_group VARCHAR(10), " +
                    "joining_date VARCHAR(50), " +
                    "emergency_contact VARCHAR(50), " +
                    "validity VARCHAR(50), " +
                    "password VARCHAR(100)" +
                    ");");

            // 14. HOSPITAL SETTINGS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS hospital_settings (" +
                    "setting_key VARCHAR(100) PRIMARY KEY, " +
                    "setting_value TEXT" +
                    ");");

            // 15. SYSTEM NOTIFICATIONS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS system_notifications (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "title VARCHAR(200) NOT NULL, " +
                    "message TEXT, " +
                    "type VARCHAR(50), " +
                    "timestamp VARCHAR(50), " +
                    "is_read INT DEFAULT 0" +
                    ");");

            // 16. ONLINE CONSULTATION TABLE (TELEMEDICINE)
            stmt.execute("CREATE TABLE IF NOT EXISTS online_consultation (" +
                    "consultation_id VARCHAR(50) PRIMARY KEY, " +
                    "appointment_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50) NOT NULL, " +
                    "doctor_name VARCHAR(100), " +
                    "department VARCHAR(100), " +
                    "meeting_id VARCHAR(100) UNIQUE NOT NULL, " +
                    "meeting_room VARCHAR(100) NOT NULL, " +
                    "meeting_link TEXT NOT NULL, " +
                    "appointment_token VARCHAR(50) NOT NULL, " +
                    "meeting_password VARCHAR(50), " +
                    "consultation_type VARCHAR(50) DEFAULT 'Online Consultation', " +
                    "meeting_status VARCHAR(50) DEFAULT 'Scheduled', " +
                    "meeting_date VARCHAR(30), " +
                    "meeting_time VARCHAR(30), " +
                    "scheduled_start VARCHAR(50), " +
                    "scheduled_end VARCHAR(50), " +
                    "actual_start VARCHAR(50), " +
                    "actual_end VARCHAR(50), " +
                    "start_time VARCHAR(50), " +
                    "end_time VARCHAR(50), " +
                    "patient_join_time VARCHAR(50), " +
                    "doctor_join_time VARCHAR(50), " +
                    "patient_leave_time VARCHAR(50), " +
                    "doctor_leave_time VARCHAR(50), " +
                    "duration_minutes INT DEFAULT 0, " +
                    "total_minutes INT DEFAULT 0, " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 17. CONSULTATION NOTES TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS consultation_notes (" +
                    "note_id VARCHAR(50) PRIMARY KEY, " +
                    "consultation_id VARCHAR(50), " +
                    "appointment_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50) NOT NULL, " +
                    "consultation_summary TEXT, " +
                    "diagnosis TEXT, " +
                    "advice TEXT, " +
                    "follow_up_date VARCHAR(50), " +
                    "medical_certificate_required VARCHAR(10) DEFAULT 'No', " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 18. MEETING CHAT TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS meeting_chat (" +
                    "chat_id VARCHAR(50) PRIMARY KEY, " +
                    "consultation_id VARCHAR(50), " +
                    "meeting_id VARCHAR(100) NOT NULL, " +
                    "sender_id VARCHAR(50) NOT NULL, " +
                    "sender_name VARCHAR(100) NOT NULL, " +
                    "sender_role VARCHAR(20) NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "timestamp VARCHAR(50) NOT NULL" +
                    ");");

            // 19. MEETING LOGS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS meeting_logs (" +
                    "log_id VARCHAR(50) PRIMARY KEY, " +
                    "consultation_id VARCHAR(50), " +
                    "meeting_id VARCHAR(100) NOT NULL, " +
                    "user_id VARCHAR(50) NOT NULL, " +
                    "user_role VARCHAR(20) NOT NULL, " +
                    "event_type VARCHAR(50) NOT NULL, " +
                    "timestamp VARCHAR(50) NOT NULL" +
                    ");");

            // 20. ACTIVITY LOGS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS activity_logs (" +
                    "log_id VARCHAR(50) PRIMARY KEY, " +
                    "user_id VARCHAR(50), " +
                    "user_name VARCHAR(100), " +
                    "role VARCHAR(50), " +
                    "module VARCHAR(50), " +
                    "action VARCHAR(100), " +
                    "status VARCHAR(50), " +
                    "ip_address VARCHAR(50), " +
                    "created_at VARCHAR(50)" +
                    ");");

            // Seed default departments if empty
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO departments (dept_id, dept_name, head_doctor, total_doctors, total_patients) VALUES " +
                            "('DEP101', 'Cardiology', 'Dr. Ananya Rao', 6, 120), " +
                            "('DEP102', 'Emergency / Casualty', 'Dr. Rajesh Kumar', 8, 250), " +
                            "('DEP103', 'Pediatrics', 'Dr. Meera Iyer', 4, 85), " +
                            "('DEP104', 'Orthopedics', 'Dr. Sameer Kulkarni', 5, 95), " +
                            "('DEP105', 'Obstetrics and Gynecology', 'Dr. Sunita Deshmukh', 4, 110) ON CONFLICT (dept_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO departments (dept_id, dept_name, head_doctor, total_doctors, total_patients) VALUES " +
                            "('DEP101', 'Cardiology', 'Dr. Ananya Rao', 6, 120), " +
                            "('DEP102', 'Emergency / Casualty', 'Dr. Rajesh Kumar', 8, 250), " +
                            "('DEP103', 'Pediatrics', 'Dr. Meera Iyer', 4, 85), " +
                            "('DEP104', 'Orthopedics', 'Dr. Sameer Kulkarni', 5, 95), " +
                            "('DEP105', 'Obstetrics and Gynecology', 'Dr. Sunita Deshmukh', 4, 110);");
                }
            } catch(Exception ignored){}

            // Seed default settings
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO hospital_settings (setting_key, setting_value) VALUES " +
                            "('hospital_name', 'Niramaya Hospitals'), " +
                            "('hospital_logo', 'assets/logo.png'), " +
                            "('address', 'No. 25, Anna Salai, Chennai - 600002'), " +
                            "('phone', '+91 98765 43210'), " +
                            "('email', 'contact@niramaya.health'), " +
                            "('gst_number', '33AAAAA0000A1Z5'), " +
                            "('working_hours', '24 Hours / 7 Days'), " +
                            "('emergency_contact', '108 / 1800-425-0000') ON CONFLICT (setting_key) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO hospital_settings (setting_key, setting_value) VALUES " +
                            "('hospital_name', 'Niramaya Hospitals'), " +
                            "('hospital_logo', 'assets/logo.png'), " +
                            "('address', 'No. 25, Anna Salai, Chennai - 600002'), " +
                            "('phone', '+91 98765 43210'), " +
                            "('email', 'contact@niramaya.health'), " +
                            "('gst_number', '33AAAAA0000A1Z5'), " +
                            "('working_hours', '24 Hours / 7 Days'), " +
                            "('emergency_contact', '108 / 1800-425-0000');");
                }
            } catch(Exception ignored){}

            // Migrations for existing tables & timestamps
            String[] migrations = {
                "ALTER TABLE pharmacy_order_items ADD COLUMN medicine_source VARCHAR(30) DEFAULT 'Inventory';",
                "ALTER TABLE appointments ADD COLUMN consultation_type VARCHAR(50) DEFAULT 'Online Consultation';",
                "ALTER TABLE appointments ADD COLUMN meeting_id VARCHAR(100);",
                "ALTER TABLE appointments ADD COLUMN meeting_link TEXT;",
                "ALTER TABLE appointments ADD COLUMN appointment_token VARCHAR(50);",
                "ALTER TABLE appointments ADD COLUMN meeting_status VARCHAR(50) DEFAULT 'Scheduled';",
                
                // User & Role Timestamps
                "ALTER TABLE users ADD COLUMN created_at VARCHAR(50);",
                "ALTER TABLE users ADD COLUMN login_time VARCHAR(50);",
                "ALTER TABLE users ADD COLUMN logout_time VARCHAR(50);",
                "ALTER TABLE users ADD COLUMN updated_at VARCHAR(50);",

                // Patient Module Timestamps
                "ALTER TABLE patients ADD COLUMN registration_time VARCHAR(50);",
                "ALTER TABLE patients ADD COLUMN last_login VARCHAR(50);",
                "ALTER TABLE patients ADD COLUMN profile_updated_time VARCHAR(50);",
                "ALTER TABLE patients ADD COLUMN created_at VARCHAR(50);",
                "ALTER TABLE patients ADD COLUMN updated_at VARCHAR(50);",

                // Doctor Module Timestamps
                "ALTER TABLE doctors ADD COLUMN registration_time VARCHAR(50);",
                "ALTER TABLE doctors ADD COLUMN approval_time VARCHAR(50);",
                "ALTER TABLE doctors ADD COLUMN login_time VARCHAR(50);",
                "ALTER TABLE doctors ADD COLUMN logout_time VARCHAR(50);",
                "ALTER TABLE doctors ADD COLUMN online_status_change_time VARCHAR(50);",
                "ALTER TABLE doctors ADD COLUMN updated_at VARCHAR(50);",

                // Appointment Module Timestamps
                "ALTER TABLE appointments ADD COLUMN created_at VARCHAR(50);",
                "ALTER TABLE appointments ADD COLUMN updated_at VARCHAR(50);",
                "ALTER TABLE appointments ADD COLUMN confirmed_at VARCHAR(50);",
                "ALTER TABLE appointments ADD COLUMN cancelled_at VARCHAR(50);",
                "ALTER TABLE appointments ADD COLUMN completed_at VARCHAR(50);",

                // Prescription Module Timestamps
                "ALTER TABLE prescriptions ADD COLUMN created_at VARCHAR(50);",
                "ALTER TABLE prescriptions ADD COLUMN updated_at VARCHAR(50);",
                "ALTER TABLE prescriptions ADD COLUMN downloaded_at VARCHAR(50);",

                // Lab Module Timestamps
                "ALTER TABLE lab_bookings ADD COLUMN updated_at VARCHAR(50);",
                "ALTER TABLE lab_bookings ADD COLUMN completed_at VARCHAR(50);",
                "ALTER TABLE lab_payments ADD COLUMN payment_completed_at VARCHAR(50);",
                "ALTER TABLE lab_reports ADD COLUMN report_uploaded_at VARCHAR(50);",
                "ALTER TABLE lab_reports ADD COLUMN report_updated_at VARCHAR(50);",
                "ALTER TABLE lab_reports ADD COLUMN report_viewed_at VARCHAR(50);",
                "ALTER TABLE lab_reports ADD COLUMN report_downloaded_at VARCHAR(50);",
                "ALTER TABLE lab_reports ADD COLUMN created_at VARCHAR(50);",
                "ALTER TABLE lab_reports ADD COLUMN updated_at VARCHAR(50);",

                // Medicine & Pharmacy Timestamps
                "ALTER TABLE medicines ADD COLUMN stock_updated_at VARCHAR(50);",
                "ALTER TABLE medicines ADD COLUMN approved_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN created_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN medicine_sold_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN invoice_generated_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN payment_received_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN medicine_packed_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN medicine_ready_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN medicine_collected_at VARCHAR(50);",
                "ALTER TABLE pharmacy_orders ADD COLUMN invoice_downloaded_at VARCHAR(50);",

                // Online Consultation Timestamps & Meeting Lifecycle
                "ALTER TABLE online_consultation ADD COLUMN scheduled_start VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN scheduled_end VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN actual_start VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN actual_end VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN patient_join_time VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN doctor_join_time VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN patient_leave_time VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN doctor_leave_time VARCHAR(50);",
                "ALTER TABLE online_consultation ADD COLUMN duration_minutes INT DEFAULT 0;",

                // Staff & Settings Timestamps
                "ALTER TABLE staff ADD COLUMN employee_code VARCHAR(50);",
                "ALTER TABLE staff ADD COLUMN full_name VARCHAR(100);",
                "ALTER TABLE staff ADD COLUMN mobile VARCHAR(20);",
                "ALTER TABLE staff ADD COLUMN designation VARCHAR(100);",
                "ALTER TABLE staff ADD COLUMN qualification VARCHAR(100);",
                "ALTER TABLE staff ADD COLUMN experience VARCHAR(50);",
                "ALTER TABLE staff ADD COLUMN medical_reg_no VARCHAR(100);",
                "ALTER TABLE staff ADD COLUMN specialization VARCHAR(100);",
                "ALTER TABLE staff ADD COLUMN consultation_fee DECIMAL(10,2) DEFAULT 0.0;",
                "ALTER TABLE staff ADD COLUMN license_no VARCHAR(100);",
                "ALTER TABLE staff ADD COLUMN office_extension VARCHAR(50);",
                "ALTER TABLE staff ADD COLUMN blood_group VARCHAR(10);",
                "ALTER TABLE staff ADD COLUMN joining_date VARCHAR(50);",
                "ALTER TABLE staff ADD COLUMN emergency_contact VARCHAR(50);",
                "ALTER TABLE staff ADD COLUMN validity VARCHAR(50);",
                "ALTER TABLE staff ADD COLUMN password VARCHAR(100);",
                "ALTER TABLE staff ADD COLUMN created_at VARCHAR(50);",
                "ALTER TABLE staff ADD COLUMN approved_at VARCHAR(50);",
                "ALTER TABLE hospital_settings ADD COLUMN updated_at VARCHAR(50);",
                "ALTER TABLE notifications ADD COLUMN patient_id VARCHAR(50);"
            };
            for (String mig : migrations) {
                try { stmt.execute(mig); } catch (Exception ignored) {}
            }

            // 21. PHARMACY INVENTORY TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS pharmacy_inventory (" +
                    "inventory_id VARCHAR(50) PRIMARY KEY, " +
                    "medicine_id VARCHAR(50), " +
                    "medicine_name VARCHAR(100) NOT NULL, " +
                    "batch_number VARCHAR(50), " +
                    "stock_quantity INT DEFAULT 100, " +
                    "unit_price DECIMAL(10,2) DEFAULT 0.0, " +
                    "expiry_date VARCHAR(50), " +
                    "status VARCHAR(50) DEFAULT 'In Stock', " +
                    "created_at VARCHAR(50), " +
                    "updated_at VARCHAR(50)" +
                    ");");

            // 22. LABORATORY REPORTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS laboratory_reports (" +
                    "report_id VARCHAR(50) PRIMARY KEY, " +
                    "booking_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50), " +
                    "test_name VARCHAR(100) NOT NULL, " +
                    "result TEXT, " +
                    "status VARCHAR(30) DEFAULT 'Ready', " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 23. BILLING TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS billing (" +
                    "bill_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "bill_type VARCHAR(50) DEFAULT 'General Hospital Bill', " +
                    "total_amount DECIMAL(10,2) DEFAULT 0.0, " +
                    "paid_amount DECIMAL(10,2) DEFAULT 0.0, " +
                    "payment_status VARCHAR(30) DEFAULT 'Paid', " +
                    "payment_method VARCHAR(50) DEFAULT 'Cash/Online', " +
                    "created_at VARCHAR(50), " +
                    "updated_at VARCHAR(50)" +
                    ");");

            // 24. NOTIFICATIONS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "title VARCHAR(200) NOT NULL, " +
                    "message TEXT, " +
                    "type VARCHAR(50), " +
                    "timestamp VARCHAR(50), " +
                    "is_read INT DEFAULT 0, " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 25. ONLINE CONSULTATIONS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS online_consultations (" +
                    "consultation_id VARCHAR(50) PRIMARY KEY, " +
                    "appointment_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "doctor_id VARCHAR(50) NOT NULL, " +
                    "doctor_name VARCHAR(100), " +
                    "department VARCHAR(100), " +
                    "meeting_id VARCHAR(100) NOT NULL, " +
                    "meeting_link TEXT NOT NULL, " +
                    "meeting_status VARCHAR(50) DEFAULT 'Scheduled', " +
                    "meeting_date VARCHAR(30), " +
                    "meeting_time VARCHAR(30), " +
                    "created_at VARCHAR(50)" +
                    ");");

            // 27. NURSE MODULE TABLES
            stmt.execute("CREATE TABLE IF NOT EXISTS nurses (" +
                    "nurse_id VARCHAR(50) PRIMARY KEY, " +
                    "employee_code VARCHAR(50) UNIQUE, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100), " +
                    "gender VARCHAR(20), " +
                    "dob VARCHAR(50), " +
                    "phone VARCHAR(20), " +
                    "phone_number VARCHAR(20), " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "department VARCHAR(100), " +
                    "qualification VARCHAR(100), " +
                    "experience_years INT DEFAULT 0, " +
                    "shift VARCHAR(50) DEFAULT 'Morning', " +
                    "joining_date VARCHAR(50), " +
                    "address TEXT, " +
                    "username VARCHAR(100), " +
                    "password VARCHAR(100) NOT NULL, " +
                    "profile_photo TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Active', " +
                    "created_at VARCHAR(50), " +
                    "updated_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS nurse_assignments (" +
                    "assignment_id VARCHAR(50) PRIMARY KEY, " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "patient_id VARCHAR(50), " +
                    "patient_name VARCHAR(100), " +
                    "doctor_name VARCHAR(100), " +
                    "ward VARCHAR(50), " +
                    "room_number VARCHAR(20), " +
                    "bed_number VARCHAR(20), " +
                    "admission_date VARCHAR(50), " +
                    "status VARCHAR(50) DEFAULT 'Active', " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS patient_vitals (" +
                    "vital_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "temperature VARCHAR(20), " +
                    "blood_pressure VARCHAR(20), " +
                    "pulse_rate VARCHAR(20), " +
                    "respiratory_rate VARCHAR(20), " +
                    "oxygen_saturation VARCHAR(20), " +
                    "blood_sugar VARCHAR(20), " +
                    "weight VARCHAR(20), " +
                    "height VARCHAR(20), " +
                    "recorded_date VARCHAR(50), " +
                    "recorded_time VARCHAR(50), " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS nursing_notes (" +
                    "note_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "observation TEXT NOT NULL, " +
                    "note_date VARCHAR(50), " +
                    "note_time VARCHAR(50), " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS medication_administration (" +
                    "admin_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "prescription_id VARCHAR(50), " +
                    "medicine_name VARCHAR(100) NOT NULL, " +
                    "dosage VARCHAR(50), " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "dosage_time VARCHAR(50), " +
                    "missed_reason TEXT, " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS patient_monitoring (" +
                    "monitoring_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "pain_level VARCHAR(20), " +
                    "food_intake VARCHAR(50), " +
                    "water_intake VARCHAR(50), " +
                    "sleep_quality VARCHAR(50), " +
                    "urine_output VARCHAR(50), " +
                    "bowel_movement VARCHAR(50), " +
                    "general_condition VARCHAR(100), " +
                    "observations TEXT, " +
                    "recorded_date VARCHAR(50), " +
                    "recorded_time VARCHAR(50), " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS nurse_shift (" +
                    "shift_id VARCHAR(50) PRIMARY KEY, " +
                    "nurse_id VARCHAR(50) NOT NULL, " +
                    "nurse_name VARCHAR(100), " +
                    "shift_type VARCHAR(50), " +
                    "start_time VARCHAR(50), " +
                    "end_time VARCHAR(50), " +
                    "ward VARCHAR(50), " +
                    "handover_notes TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Active', " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS injection_records (" +
                    "injection_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "injection_name VARCHAR(100) NOT NULL, " +
                    "dose VARCHAR(50), " +
                    "route VARCHAR(50), " +
                    "record_date VARCHAR(50), " +
                    "record_time VARCHAR(50), " +
                    "remarks TEXT, " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_requests (" +
                    "request_id VARCHAR(50) PRIMARY KEY, " +
                    "nurse_id VARCHAR(50) NOT NULL, " +
                    "nurse_name VARCHAR(100), " +
                    "item_name VARCHAR(100) NOT NULL, " +
                    "quantity INT DEFAULT 1, " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "request_date VARCHAR(50), " +
                    "remarks TEXT, " +
                    "approved_by VARCHAR(100), " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS emergency_alerts (" +
                    "alert_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "patient_name VARCHAR(100), " +
                    "room_number VARCHAR(20), " +
                    "ward VARCHAR(50), " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "alert_type VARCHAR(100) NOT NULL, " +
                    "alert_time VARCHAR(50), " +
                    "status VARCHAR(20) DEFAULT 'Active', " +
                    "resolved_by VARCHAR(100), " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS lab_samples (" +
                    "sample_id VARCHAR(50) PRIMARY KEY, " +
                    "patient_id VARCHAR(50) NOT NULL, " +
                    "patient_name VARCHAR(100), " +
                    "nurse_id VARCHAR(50), " +
                    "nurse_name VARCHAR(100), " +
                    "sample_type VARCHAR(50) NOT NULL, " +
                    "status VARCHAR(50) DEFAULT 'Pending', " +
                    "collected_time VARCHAR(50), " +
                    "created_at VARCHAR(50)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS daily_nurse_tasks (" +
                    "task_id VARCHAR(50) PRIMARY KEY, " +
                    "nurse_id VARCHAR(50) NOT NULL, " +
                    "patient_id VARCHAR(50), " +
                    "task_name VARCHAR(200) NOT NULL, " +
                    "category VARCHAR(100), " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "task_time VARCHAR(50), " +
                    "created_at VARCHAR(50)" +
                    ");");

            // --- POPULATE COMPREHENSIVE SEED DATA FOR ALL MODULES IF EMPTY ---
            // 1. Doctors Seed
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO doctors (doctor_id, name, doctor_name, phone, phone_number, age, gender, email, password, qualification, category, department, specialization, consultation_fees, consultation_fee, working_days, available_days, working_hours, available_time, available_status, status, appointment_available, accept_appointments) VALUES " +
                            "('DOC10084', 'Dr. Ananya Rao', 'Dr. Ananya Rao', '+91 98765 43211', '+91 98765 43211', 38, 'Female', 'doctor@niramaya.health', 'demo1234', 'MD Cardiology', 'Cardiology', 'Cardiology', 'Senior Cardiologist', 800.0, 800.0, 'Mon - Sat', 'Mon - Sat', '09:00 AM - 02:00 PM', '09:00 AM - 02:00 PM', 'Online', 'Online', true, 'Yes'), " +
                            "('DOC10085', 'Dr. Rajesh Kumar', 'Dr. Rajesh Kumar', '+91 98765 43215', '+91 98765 43215', 45, 'Male', 'rajesh@niramaya.health', 'demo1234', 'MD Emergency Medicine', 'Emergency', 'Emergency / Casualty', 'Emergency Medicine Specialist', 1000.0, 1000.0, 'Mon - Sun', 'Mon - Sun', '02:00 PM - 09:00 PM', '02:00 PM - 09:00 PM', 'Online', 'Online', true, 'Yes'), " +
                            "('DOC10086', 'Dr. Meera Iyer', 'Dr. Meera Iyer', '+91 98765 43216', '+91 98765 43216', 34, 'Female', 'meera@niramaya.health', 'demo1234', 'MD Pediatrics', 'Pediatrics', 'Pediatrics', 'Pediatric Specialist', 700.0, 700.0, 'Mon - Fri', 'Mon - Fri', '10:00 AM - 04:00 PM', '10:00 AM - 04:00 PM', 'Online', 'Online', true, 'Yes') ON CONFLICT (doctor_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR REPLACE INTO doctors (doctor_id, name, doctor_name, phone, phone_number, age, gender, email, password, qualification, category, department, specialization, consultation_fees, consultation_fee, working_days, available_days, working_hours, available_time, available_status, status, appointment_available, accept_appointments) VALUES ('DOC10084', 'Dr. Ananya Rao', 'Dr. Ananya Rao', '+91 98765 43211', '+91 98765 43211', 38, 'Female', 'doctor@niramaya.health', 'demo1234', 'MD Cardiology', 'Cardiology', 'Cardiology', 'Senior Cardiologist', 800.0, 800.0, 'Mon - Sat', 'Mon - Sat', '09:00 AM - 02:00 PM', '09:00 AM - 02:00 PM', 'Online', 'Online', 1, 'Yes');");
                    stmt.execute("INSERT OR REPLACE INTO doctors (doctor_id, name, doctor_name, phone, phone_number, age, gender, email, password, qualification, category, department, specialization, consultation_fees, consultation_fee, working_days, available_days, working_hours, available_time, available_status, status, appointment_available, accept_appointments) VALUES ('DOC10085', 'Dr. Rajesh Kumar', 'Dr. Rajesh Kumar', '+91 98765 43215', '+91 98765 43215', 45, 'Male', 'rajesh@niramaya.health', 'demo1234', 'MD Emergency Medicine', 'Emergency', 'Emergency / Casualty', 'Emergency Medicine Specialist', 1000.0, 1000.0, 'Mon - Sun', 'Mon - Sun', '02:00 PM - 09:00 PM', '02:00 PM - 09:00 PM', 'Online', 'Online', 1, 'Yes');");
                    stmt.execute("INSERT OR REPLACE INTO doctors (doctor_id, name, doctor_name, phone, phone_number, age, gender, email, password, qualification, category, department, specialization, consultation_fees, consultation_fee, working_days, available_days, working_hours, available_time, available_status, status, appointment_available, accept_appointments) VALUES ('DOC10086', 'Dr. Meera Iyer', 'Dr. Meera Iyer', '+91 98765 43216', '+91 98765 43216', 34, 'Female', 'meera@niramaya.health', 'demo1234', 'MD Pediatrics', 'Pediatrics', 'Pediatrics', 'Pediatric Specialist', 700.0, 700.0, 'Mon - Fri', 'Mon - Fri', '10:00 AM - 04:00 PM', '10:00 AM - 04:00 PM', 'Online', 'Online', 1, 'Yes');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED ERROR] doctors:"); e.printStackTrace(); }

            // 2. Staff Seed
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO staff (employee_code, role, full_name, email, mobile, department, designation, qualification, experience, status, joining_date, blood_group) VALUES " +
                            "('EMP-000001', 'Doctor', 'Dr. Ananya Rao', 'doctor@niramaya.health', '+91 98765 43211', 'Cardiology', 'Senior Cardiologist', 'MD Cardiology', '12 Years', 'Active', '2022-03-15', 'O+'), " +
                            "('EMP-000002', 'Pharmacist', 'Chief Pharmacist', 'pharmacy@niramaya.health', '+91 98765 43214', 'Pharmacy & Inventory', 'Lead Pharmacist', 'B.Pharm', '7 Years', 'Active', '2023-01-10', 'A+'), " +
                            "('EMP-000003', 'Laboratory Technician', 'Senior Pathology Specialist', 'labtech@tech.in', '+91 98765 43213', 'Pathology & Diagnostics', 'Lab Officer', 'M.Sc Pathology', '9 Years', 'Active', '2022-08-01', 'B+'), " +
                            "('EMP-000004', 'Administrator', 'Hospital Admin', 'admin@niramaya.health', '+91 98765 43212', 'Hospital Operations', 'Super Admin', 'MBA Healthcare', '15 Years', 'Active', '2020-01-01', 'AB+') ON CONFLICT (employee_code) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO staff (employee_code, role, full_name, email, mobile, department, designation, qualification, experience, status, joining_date, blood_group) VALUES " +
                            "('EMP-000001', 'Doctor', 'Dr. Ananya Rao', 'doctor@niramaya.health', '+91 98765 43211', 'Cardiology', 'Senior Cardiologist', 'MD Cardiology', '12 Years', 'Active', '2022-03-15', 'O+'), " +
                            "('EMP-000002', 'Pharmacist', 'Chief Pharmacist', 'pharmacy@niramaya.health', '+91 98765 43214', 'Pharmacy & Inventory', 'Lead Pharmacist', 'B.Pharm', '7 Years', 'Active', '2023-01-10', 'A+'), " +
                            "('EMP-000003', 'Laboratory Technician', 'Senior Pathology Specialist', 'labtech@tech.in', '+91 98765 43213', 'Pathology & Diagnostics', 'Lab Officer', 'M.Sc Pathology', '9 Years', 'Active', '2022-08-01', 'B+'), " +
                            "('EMP-000004', 'Administrator', 'Hospital Admin', 'admin@niramaya.health', '+91 98765 43212', 'Hospital Operations', 'Super Admin', 'MBA Healthcare', '15 Years', 'Active', '2020-01-01', 'AB+');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED] staff: " + e.getMessage()); }

            // 3. Pharmacy Orders & Inventory Seed
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO pharmacy_orders (order_id, pharmacy_token, patient_id, doctor_id, prescription_id, total_amount, payment_status, order_status, payment_method, order_date) VALUES " +
                            "('ORD-7001', 'PH-101', 'PT100842', 'DOC10084', 'RX-908124', 470.0, 'Paid', 'Completed', 'Online UPI', '2026-07-29') ON CONFLICT (order_id) DO NOTHING;");
                    stmt.execute("INSERT INTO pharmacy_order_items (item_id, order_id, medicine_id, medicine_name, strength, dosage, morning, afternoon, night, duration, quantity, unit_price, subtotal, medicine_source) VALUES " +
                            "('ITM-7001-1', 'ORD-7001', 'MED101', 'Paracetamol 650mg', '650mg', 'After Food', 1, 0, 1, '5 Days', 10, 4.0, 40.0, 'Inventory'), " +
                            "('ITM-7001-2', 'ORD-7001', 'MED102', 'Amoxicillin 500mg', '500mg', 'After Food', 1, 0, 1, '5 Days', 10, 18.0, 180.0, 'Inventory'), " +
                            "('ITM-7001-3', 'ORD-7001', 'MED103', 'Vitamin D3 60K', '60K IU', 'After Food', 0, 1, 0, '4 Weeks', 1, 250.0, 250.0, 'Inventory') ON CONFLICT (item_id) DO NOTHING;");
                    stmt.execute("INSERT INTO pharmacy_inventory (inventory_id, medicine_id, medicine_name, batch_number, stock_quantity, unit_price, expiry_date, status, created_at) VALUES " +
                            "('INV-101', 'MED101', 'Paracetamol 650mg', 'BT-9081', 250, 4.0, '2027-12-31', 'In Stock', '2026-07-29'), " +
                            "('INV-102', 'MED102', 'Amoxicillin 500mg', 'BT-9082', 150, 18.0, '2027-08-31', 'In Stock', '2026-07-29'), " +
                            "('INV-103', 'MED103', 'Vitamin D3 60K', 'BT-9083', 100, 35.0, '2028-03-31', 'In Stock', '2026-07-29') ON CONFLICT (inventory_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO pharmacy_orders (order_id, pharmacy_token, patient_id, doctor_id, prescription_id, total_amount, payment_status, order_status, payment_method, order_date) VALUES " +
                            "('ORD-7001', 'PH-101', 'PT100842', 'DOC10084', 'RX-908124', 470.0, 'Paid', 'Completed', 'Online UPI', '2026-07-29');");
                    stmt.execute("INSERT OR IGNORE INTO pharmacy_order_items (item_id, order_id, medicine_id, medicine_name, strength, dosage, morning, afternoon, night, duration, quantity, unit_price, subtotal, medicine_source) VALUES " +
                            "('ITM-7001-1', 'ORD-7001', 'MED101', 'Paracetamol 650mg', '650mg', 'After Food', 1, 0, 1, '5 Days', 10, 4.0, 40.0, 'Inventory'), " +
                            "('ITM-7001-2', 'ORD-7001', 'MED102', 'Amoxicillin 500mg', '500mg', 'After Food', 1, 0, 1, '5 Days', 10, 18.0, 180.0, 'Inventory'), " +
                            "('ITM-7001-3', 'ORD-7001', 'MED103', 'Vitamin D3 60K', '60K IU', 'After Food', 0, 1, 0, '4 Weeks', 1, 250.0, 250.0, 'Inventory');");
                    stmt.execute("INSERT OR IGNORE INTO pharmacy_inventory (inventory_id, medicine_id, medicine_name, batch_number, stock_quantity, unit_price, expiry_date, status, created_at) VALUES " +
                            "('INV-101', 'MED101', 'Paracetamol 650mg', 'BT-9081', 250, 4.0, '2027-12-31', 'In Stock', '2026-07-29'), " +
                            "('INV-102', 'MED102', 'Amoxicillin 500mg', 'BT-9082', 150, 18.0, '2027-08-31', 'In Stock', '2026-07-29'), " +
                            "('INV-103', 'MED103', 'Vitamin D3 60K', 'BT-9083', 100, 35.0, '2028-03-31', 'In Stock', '2026-07-29');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED] pharmacy: " + e.getMessage()); }

            // 4. Billing Seed
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO billing (bill_id, patient_id, bill_type, total_amount, paid_amount, payment_status, payment_method, created_at) VALUES " +
                            "('INV-9001', 'PT100842', 'OPD Consultation Fee', 800.0, 800.0, 'Paid', 'Online UPI', '2026-07-29'), " +
                            "('INV-9002', 'PT100842', 'Lab Test CBC Fee', 500.0, 500.0, 'Paid', 'Credit Card', '2026-07-29'), " +
                            "('INV-9003', 'PT100842', 'Pharmacy Medication Order', 470.0, 470.0, 'Paid', 'Cash', '2026-07-29') ON CONFLICT (bill_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO billing (bill_id, patient_id, bill_type, total_amount, paid_amount, payment_status, payment_method, created_at) VALUES " +
                            "('INV-9001', 'PT100842', 'OPD Consultation Fee', 800.0, 800.0, 'Paid', 'Online UPI', '2026-07-29'), " +
                            "('INV-9002', 'PT100842', 'Lab Test CBC Fee', 500.0, 500.0, 'Paid', 'Credit Card', '2026-07-29'), " +
                            "('INV-9003', 'PT100842', 'Pharmacy Medication Order', 470.0, 470.0, 'Paid', 'Cash', '2026-07-29');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED] billing: " + e.getMessage()); }

            // 5. Notifications Seed
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO notifications (id, title, message, type, timestamp, is_read, created_at) VALUES " +
                            "('NTF-101', 'Appointment Confirmed', 'Patient Rekha Prasad confirmed appointment with Dr. Ananya Rao.', 'APPOINTMENT', '2026-07-29 10:00:00', 0, '2026-07-29 10:00:00'), " +
                            "('NTF-102', 'Lab Report Ready', 'Complete Blood Count (CBC) report uploaded for Patient Rekha Prasad.', 'LAB', '2026-07-29 11:30:00', 0, '2026-07-29 11:30:00') ON CONFLICT (id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO notifications (id, title, message, type, timestamp, is_read, created_at) VALUES " +
                            "('NTF-101', 'Appointment Confirmed', 'Patient Rekha Prasad confirmed appointment with Dr. Ananya Rao.', 'APPOINTMENT', '2026-07-29 10:00:00', 0, '2026-07-29 10:00:00'), " +
                            "('NTF-102', 'Lab Report Ready', 'Complete Blood Count (CBC) report uploaded for Patient Rekha Prasad.', 'LAB', '2026-07-29 11:30:00', 0, '2026-07-29 11:30:00');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED] notifications: " + e.getMessage()); }

            // 6. Online Consultations Seed
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO online_consultations (consultation_id, appointment_id, patient_id, doctor_id, doctor_name, department, meeting_id, meeting_link, meeting_status, meeting_date, meeting_time, created_at) VALUES " +
                            "('CONS-1001', 'TK-1001', 'PT100842', 'DOC10084', 'Dr. Ananya Rao', 'Cardiology', 'MTG-80912', 'https://meet.jit.si/niramaya-telemed-TK-1001', 'Completed', '2026-07-29', '10:00 AM', '2026-07-29') ON CONFLICT (consultation_id) DO NOTHING;");
                    stmt.execute("INSERT INTO online_consultation (consultation_id, appointment_id, patient_id, doctor_id, doctor_name, department, meeting_id, meeting_room, meeting_link, appointment_token, meeting_status, meeting_date, meeting_time, created_at) VALUES " +
                            "('CONS-1001', 'TK-1001', 'PT100842', 'DOC10084', 'Dr. Ananya Rao', 'Cardiology', 'MTG-80912', 'niramaya-telemed-TK-1001', 'https://meet.jit.si/niramaya-telemed-TK-1001', 'TK-1001', 'Completed', '2026-07-29', '10:00 AM', '2026-07-29') ON CONFLICT (consultation_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO online_consultations (consultation_id, appointment_id, patient_id, doctor_id, doctor_name, department, meeting_id, meeting_link, meeting_status, meeting_date, meeting_time, created_at) VALUES " +
                            "('CONS-1001', 'TK-1001', 'PT100842', 'DOC10084', 'Dr. Ananya Rao', 'Cardiology', 'MTG-80912', 'https://meet.jit.si/niramaya-telemed-TK-1001', 'Completed', '2026-07-29', '10:00 AM', '2026-07-29');");
                    stmt.execute("INSERT OR IGNORE INTO online_consultation (consultation_id, appointment_id, patient_id, doctor_id, doctor_name, department, meeting_id, meeting_room, meeting_link, appointment_token, meeting_status, meeting_date, meeting_time, created_at) VALUES " +
                            "('CONS-1001', 'TK-1001', 'PT100842', 'DOC10084', 'Dr. Ananya Rao', 'Cardiology', 'MTG-80912', 'niramaya-telemed-TK-1001', 'https://meet.jit.si/niramaya-telemed-TK-1001', 'TK-1001', 'Completed', '2026-07-29', '10:00 AM', '2026-07-29');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED] online_consultations: " + e.getMessage()); }

            // 7. Activity Logs Seed
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO activity_logs (log_id, user_id, user_name, role, module, action, status, ip_address, created_at) VALUES " +
                            "('LOG-1001', 'patient@niramaya.health', 'Rekha Prasad', 'Patient', 'AUTH', 'User Login Successful', 'Success', '127.0.0.1', '2026-08-03 09:30:00'), " +
                            "('LOG-1002', 'admin@niramaya.health', 'Hospital Admin', 'Administrator', 'DATABASE_MANAGER', 'Admin Console Accessed', 'Success', '127.0.0.1', '2026-08-03 09:31:00') ON CONFLICT (log_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO activity_logs (log_id, user_id, user_name, role, module, action, status, ip_address, created_at) VALUES " +
                            "('LOG-1001', 'patient@niramaya.health', 'Rekha Prasad', 'Patient', 'AUTH', 'User Login Successful', 'Success', '127.0.0.1', '2026-08-03 09:30:00'), " +
                            "('LOG-1002', 'admin@niramaya.health', 'Hospital Admin', 'Administrator', 'DATABASE_MANAGER', 'Admin Console Accessed', 'Success', '127.0.0.1', '2026-08-03 09:31:00');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED] activity_logs: " + e.getMessage()); }

            // 8. Nurse Module Seed Data
            try {
                if (isPostgreSQL()) {
                    stmt.execute("INSERT INTO users (email, password, role, name, phone) VALUES " +
                            "('nurse@niramaya.health', 'demo1234', 'nurse', 'Nurse Priya Sharma', '+91 98765 43217') ON CONFLICT (email) DO NOTHING;");
                    stmt.execute("INSERT INTO staff (employee_code, role, full_name, email, mobile, department, designation, qualification, experience, status, joining_date, blood_group) VALUES " +
                            "('NUR10084', 'Nurse', 'Nurse Priya Sharma', 'nurse@niramaya.health', '+91 98765 43217', 'ICU & Emergency', 'Senior Staff Nurse', 'B.Sc Nursing', '5 Years', 'Active', '2023-04-10', 'B+') ON CONFLICT (employee_code) DO NOTHING;");
                    stmt.execute("INSERT INTO nurses (nurse_id, employee_code, name, full_name, gender, dob, phone, phone_number, email, department, qualification, experience_years, shift, joining_date, address, username, password, status, created_at) VALUES " +
                            "('NUR10084', 'NUR10084', 'Nurse Priya Sharma', 'Nurse Priya Sharma', 'Female', '1995-06-15', '+91 98765 43217', '+91 98765 43217', 'nurse@niramaya.health', 'ICU & Emergency Ward', 'B.Sc Nursing', 5, 'Morning', '2023-04-10', '124 Healthcare Enclave, City', 'nurse@niramaya.health', 'demo1234', 'Active', '2026-08-03') ON CONFLICT (nurse_id) DO NOTHING;");
                    stmt.execute("INSERT INTO nurse_assignments (assignment_id, nurse_id, nurse_name, patient_id, patient_name, doctor_name, ward, room_number, bed_number, admission_date, status) VALUES " +
                            "('ASN-101', 'NUR10084', 'Nurse Priya Sharma', 'PT100842', 'Rekha Prasad', 'Dr. Ananya Rao', 'ICU Ward 3', 'ICU-302', 'Bed-04', '2026-08-01', 'Active') ON CONFLICT (assignment_id) DO NOTHING;");
                } else {
                    stmt.execute("INSERT OR IGNORE INTO users (email, password, role, name, phone) VALUES " +
                            "('nurse@niramaya.health', 'demo1234', 'nurse', 'Nurse Priya Sharma', '+91 98765 43217');");
                    stmt.execute("INSERT OR IGNORE INTO staff (employee_code, role, full_name, email, mobile, department, designation, qualification, experience, status, joining_date, blood_group) VALUES " +
                            "('NUR10084', 'Nurse', 'Nurse Priya Sharma', 'nurse@niramaya.health', '+91 98765 43217', 'ICU & Emergency', 'Senior Staff Nurse', 'B.Sc Nursing', '5 Years', 'Active', '2023-04-10', 'B+');");
                    stmt.execute("INSERT OR IGNORE INTO nurses (nurse_id, employee_code, name, full_name, gender, dob, phone, phone_number, email, department, qualification, experience_years, shift, joining_date, address, username, password, status, created_at) VALUES " +
                            "('NUR10084', 'NUR10084', 'Nurse Priya Sharma', 'Nurse Priya Sharma', 'Female', '1995-06-15', '+91 98765 43217', '+91 98765 43217', 'nurse@niramaya.health', 'ICU & Emergency Ward', 'B.Sc Nursing', 5, 'Morning', '2023-04-10', '124 Healthcare Enclave, City', 'nurse@niramaya.health', 'demo1234', 'Active', '2026-08-03');");
                    stmt.execute("INSERT OR IGNORE INTO nurse_assignments (assignment_id, nurse_id, nurse_name, patient_id, patient_name, doctor_name, ward, room_number, bed_number, admission_date, status) VALUES " +
                            "('ASN-101', 'NUR10084', 'Nurse Priya Sharma', 'PT100842', 'Rekha Prasad', 'Dr. Ananya Rao', 'ICU Ward 3', 'ICU-302', 'Bed-04', '2026-08-01', 'Active');");
                }
            } catch (Exception e) { System.err.println("[SQL SEED] nurses: " + e.getMessage()); }

            System.out.println("✓ JDBC Database Tables initialized successfully (PostgreSQL / MySQL / SQLite).");
        } catch (Exception e) {
            System.err.println("[SQL Exception] JDBC Table Init Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void verifyTablesExist() {
        String[] requiredTables = {
            "patients", "doctors", "staff", "appointments", "prescriptions",
            "pharmacy_orders", "pharmacy_inventory", "laboratory_reports",
            "lab_payments", "medicines", "billing", "notifications",
            "online_consultations", "audit_logs", "nurses", "nurse_assignments",
            "patient_vitals", "nursing_notes", "medication_administration",
            "patient_monitoring", "nurse_shift", "injection_records",
            "inventory_requests", "emergency_alerts"
        };
        System.out.println("=========================================================");
        System.out.println(" Verifying PostgreSQL / Database Tables Status...");
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String tableName : requiredTables) {
                boolean exists = false;
                try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (rs.next()) exists = true;
                } catch (Exception e) {
                    System.err.println("[SQL Exception] Error inspecting table metadata: " + tableName + " | " + e.getMessage());
                }
                if (!exists) {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs2 = stmt.executeQuery("SELECT 1 FROM " + tableName + " LIMIT 1")) {
                        exists = true;
                    } catch (Exception e) {
                        System.err.println("[SQL Exception] Verification Query Failed for table '" + tableName + "': " + e.getMessage());
                    }
                }
                if (exists) {
                    System.out.println("  [DB Verification] Table: " + String.format("%-22s", tableName) + " -> Status: ACTIVE 🟢");
                } else {
                    System.out.println("  [DB Verification] Table: " + String.format("%-22s", tableName) + " -> Status: MISSING 🔴");
                }
            }
        } catch (SQLException e) {
            System.err.println("[SQL Exception] Error verifying tables: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=========================================================");
    }
}
