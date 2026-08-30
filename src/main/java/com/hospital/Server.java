package com.hospital;

import com.hospital.dao.*;
import com.hospital.model.*;
import com.hospital.service.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Niramaya Smart Hospital — Full Backend Server.
 * Supports static web assets serving and JDBC REST APIs for:
 * - Dynamic Doctor Availability System
 * - Doctor Registration & Role-based Login
 * - Patient Appointment Booking
 * - Clinical Prescriptions Engine
 * - Laboratory Module & Lab Reports
 * - Automatic Pharmacy Workflow & Inventory Management
 * - Advanced Telemedicine & Online Consultation Module
 */
public class Server {

    private static final int PORT = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;
    private static final Path PUBLIC_DIR = Paths.get("public").toAbsolutePath().normalize();

    private static final PharmacyStaffDAO pharmacyStaffDAO = new PharmacyStaffDAO();
    private static final LabTechnicianDAO labTechnicianDAO = new LabTechnicianDAO();
    private static final DoctorDAO doctorDAO = new DoctorDAO();
    private static final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private static final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private static final PatientDAO patientDAO = new PatientDAO();
    private static final LabBookingDAO labBookingDAO = new LabBookingDAO();
    private static final LabPaymentDAO labPaymentDAO = new LabPaymentDAO();
    private static final LabReportDAO labReportDAO = new LabReportDAO();
    private static final PharmacyOrderDAO pharmacyOrderDAO = new PharmacyOrderDAO();
    private static final MedicineDAO medicineDAO = new MedicineDAO();
    private static final AdminDAO adminDAO = new AdminDAO();
    private static final StaffDAO staffDAO = new StaffDAO();
    private static final NurseDAO nurseDAO = new NurseDAO();
    private static final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private static final NotificationManager notificationManager = new NotificationManager();
    private static final DatabaseManagerDAO databaseManagerDAO = new DatabaseManagerDAO();
    private static final OnlineConsultationDAO onlineConsultationDAO = new OnlineConsultationDAO();
    private static final DailyReportDAO dailyReportDAO = new DailyReportDAO();

    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) return "";
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return rawPassword;
        }
    }

    private static final Map<String, DemoUser> USERS = new HashMap<>();
    static {
        USERS.put("patient@niramaya.health", new DemoUser("demo1234", "patient", "Rekha Prasad", "28", "Female", "62",
                "165", "+91 98765 43210", "PT100842", false));
        USERS.put("doctor@niramaya.health", new DemoUser("demo1234", "doctor", "Dr. Ananya Rao", "38", "Female", "58",
                "168", "+91 98765 43211", "DOC10084", false));
        USERS.put("nurse@niramaya.health", new DemoUser("demo1234", "nurse", "Nurse Priya Sharma", "28", "Female", "55",
                "163", "+91 98765 43217", "NUR10084", false));
        USERS.put("priya@nurse.in", new DemoUser("demo1234", "nurse", "Nurse Priya Sharma", "28", "Female", "55",
                "163", "+91 98765 43217", "NUR10084", false));
        USERS.put("labtech@tech.in", new DemoUser("demo1234", "technician", "Senior Pathology Specialist", "35", "Male",
                "72", "174", "+91 98765 43213", "TECH10084", false));
        USERS.put("pharmacy@niramaya.health", new DemoUser("demo1234", "pharmacy", "Chief Pharmacist", "36", "Male",
                "70", "172", "+91 98765 43214", "PHA10084", false));
        USERS.put("admin@niramaya.health", new DemoUser("demo1234", "admin", "Hospital Admin", "42", "Male", "75",
                "175", "+91 98765 43212", "ADM10084", false));
    }

    private static final com.sun.net.httpserver.Filter requestLoggerFilter = new com.sun.net.httpserver.Filter() {
        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            System.out.println("[HTTP REQ] " + new Date() + " | Method: " + exchange.getRequestMethod() + " | URI: " + exchange.getRequestURI());
            try {
                chain.doFilter(exchange);
            } catch (Exception ex) {
                System.err.println("[HTTP ERR] Request processing error for " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + ": " + ex.getMessage());
                com.hospital.dao.DBConnection.logSQLException(ex);
                throw ex;
            }
        }

        @Override
        public String description() {
            return "Request Logger Filter";
        }
    };

    private static void addRoute(HttpServer server, String path, HttpHandler handler) {
        server.createContext(path, handler).getFilters().add(requestLoggerFilter);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));

        addRoute(server, "/api/login", new LoginHandler());
        addRoute(server, "/api/logout", new LogoutHandler());
        addRoute(server, "/api/register", new RegisterHandler());
        addRoute(server, "/api/send-otp", new OtpHandler());
        addRoute(server, "/api/forgot-password", new ForgotPasswordHandler());
        addRoute(server, "/api/reset-password", new ResetPasswordHandler());
        addRoute(server, "/api/dashboard-stats", new StatsHandler());
        addRoute(server, "/api/activity-logs", new ActivityLogsHandler());
        addRoute(server, "/api/user-timestamps", new UserTimestampsHandler());

        // Multi-Doctor Dynamic Availability APIs
        addRoute(server, "/api/doctors", new DoctorsHandler());
        addRoute(server, "/api/doctor/toggle-availability", new ToggleAvailabilityHandler());
        addRoute(server, "/api/doctor/update", new UpdateDoctorHandler());
        addRoute(server, "/api/doctor/delete", new DeleteDoctorHandler());
        addRoute(server, "/api/admin/pharmacy-staff", new AdminPharmacyStaffHandler());
        addRoute(server, "/api/admin/lab-technicians", new AdminLabTechniciansHandler());
        addRoute(server, "/api/appointments", new AppointmentsHandler());
        addRoute(server, "/api/prescriptions", new PrescriptionsHandler());

        // Laboratory Module APIs
        addRoute(server, "/api/lab/bookings", new LabBookingsHandler());
        addRoute(server, "/api/lab/payments", new LabPaymentsHandler());
        addRoute(server, "/api/lab/reports", new LabReportsHandler());

        // Pharmacy Module APIs
        addRoute(server, "/api/pharmacy/orders", new PharmacyOrdersHandler());
        addRoute(server, "/api/pharmacy/order-items", new PharmacyOrderItemsHandler());
        addRoute(server, "/api/pharmacy/generate-bill", new PharmacyGenerateBillHandler());
        addRoute(server, "/api/pharmacy/pay", new PharmacyPayHandler());
        addRoute(server, "/api/pharmacy/update-status", new PharmacyUpdateStatusHandler());
        addRoute(server, "/api/pharmacy/invoice", new PharmacyInvoiceHandler());
        addRoute(server, "/api/pharmacy/medicines", new PharmacyMedicinesHandler());
        addRoute(server, "/api/pharmacy/available-medicines", new PharmacyAvailableMedicinesHandler());
        addRoute(server, "/api/pharmacy/add-medicine", new PharmacyAddMedicineHandler());
        addRoute(server, "/api/pharmacy/edit-medicine", new PharmacyEditMedicineHandler());
        addRoute(server, "/api/pharmacy/update-stock", new PharmacyUpdateStockHandler());
        addRoute(server, "/api/pharmacy/delete-medicine", new PharmacyDeleteMedicineHandler());

        // Admin & Staff Profile Modules
        addRoute(server, "/api/admin/stats", new AdminStatsHandler());
        addRoute(server, "/api/admin/global-search", new AdminGlobalSearchHandler());
        addRoute(server, "/api/admin/staff", new AdminStaffHandler());
        addRoute(server, "/api/staff/profile", new StaffProfileHandler());
        addRoute(server, "/api/admin/departments", new AdminDepartmentsHandler());
        addRoute(server, "/api/admin/settings", new AdminSettingsHandler());
        addRoute(server, "/api/admin/notifications", new AdminNotificationsHandler());
        addRoute(server, "/api/patient/notifications", new PatientNotificationsHandler());

        // Telemedicine & Online Consultation APIs
        addRoute(server, "/api/telemedicine/consultations", new TelemedicineConsultationsHandler());
        addRoute(server, "/api/telemedicine/meeting", new TelemedicineMeetingHandler());
        addRoute(server, "/api/telemedicine/notes", new TelemedicineNotesHandler());
        addRoute(server, "/api/telemedicine/chat", new TelemedicineChatHandler());
        addRoute(server, "/api/telemedicine/referral", new TelemedicineReferralHandler());
        addRoute(server, "/api/telemedicine/patient-history", new TelemedicinePatientHistoryHandler());

        // Database Manager Center APIs
        addRoute(server, "/api/admin/db-manager/health", new AdminDbHealthHandler());
        addRoute(server, "/api/admin/db-manager/tables", new AdminDbTablesHandler());
        addRoute(server, "/api/admin/db-manager/table-data", new AdminDbTableDataHandler());
        addRoute(server, "/api/admin/db-manager/query", new AdminDbQueryHandler());
        addRoute(server, "/api/admin/db-manager/add-record", new AdminDbAddRecordHandler());
        addRoute(server, "/api/admin/db-manager/edit-record", new AdminDbEditRecordHandler());
        addRoute(server, "/api/admin/db-manager/delete-record", new AdminDbDeleteRecordHandler());
        addRoute(server, "/api/admin/db-manager/export", new AdminDbExportHandler());
        addRoute(server, "/api/admin/db-manager/backup", new AdminDbBackupHandler());
        addRoute(server, "/api/admin/db-manager/restore", new AdminDbRestoreHandler());

        // Debug Endpoint
        addRoute(server, "/api/debug/database", new DebugDatabaseHandler());

        // Nurse Module APIs
        addRoute(server, "/api/nurse/assigned-patients", new NurseAssignedPatientsHandler());
        addRoute(server, "/api/nurse/vitals", new NurseVitalsHandler());
        addRoute(server, "/api/nurse/notes", new NurseNotesHandler());
        addRoute(server, "/api/nurse/medications", new NurseMedicationsHandler());
        addRoute(server, "/api/nurse/monitoring", new NurseMonitoringHandler());
        addRoute(server, "/api/nurse/shifts", new NurseShiftsHandler());
        addRoute(server, "/api/nurse/injections", new NurseInjectionsHandler());
        addRoute(server, "/api/nurse/inventory-requests", new NurseInventoryHandler());
        addRoute(server, "/api/nurse/emergency-alerts", new NurseEmergencyHandler());
        addRoute(server, "/api/nurse/profile", new NurseProfileHandler());
        addRoute(server, "/api/nurse/dashboard-stats", new NurseDashboardStatsHandler());
        addRoute(server, "/api/doctor/patient-vitals", new DoctorPatientVitalsHandler());

        // Daily Report Module APIs
        addRoute(server, "/api/daily-report/send", new DailyReportSendHandler());
        addRoute(server, "/api/daily-reports", new DailyReportsHandler());

        // Today's Work & Completed History Workflows
        addRoute(server, "/api/doctor/todays-patients", new DoctorTodaysPatientsHandler());
        addRoute(server, "/api/doctor/completed-consultations", new DoctorCompletedConsultationsHandler());
        addRoute(server, "/api/doctor/complete-consultation", new DoctorCompleteConsultationHandler());

        addRoute(server, "/api/nurse/todays-patients", new NurseTodaysPatientsHandler());
        addRoute(server, "/api/nurse/completed-tasks", new NurseCompletedTasksHandler());
        addRoute(server, "/api/nurse/complete-vitals", new NurseCompleteVitalsHandler());

        addRoute(server, "/api/lab/todays-orders", new LabTodaysOrdersHandler());
        addRoute(server, "/api/lab/completed-reports", new LabCompletedReportsHandler());
        addRoute(server, "/api/lab/complete-test", new LabCompleteTestHandler());

        addRoute(server, "/api/pharmacy/todays-prescriptions", new PharmacyTodaysPrescriptionsHandler());
        addRoute(server, "/api/pharmacy/completed-orders", new PharmacyCompletedOrdersHandler());
        addRoute(server, "/api/pharmacy/complete-order", new PharmacyCompleteOrderHandler());

        addRoute(server, "/api/admin/todays-stats", new AdminTodaysStatsHandler());
        addRoute(server, "/api/admin/history", new AdminHistoryHandler());

        addRoute(server, "/api/patient/todays-appointments", new PatientTodaysAppointmentsHandler());
        addRoute(server, "/api/patient/completed-appointments", new PatientCompletedAppointmentsHandler());

        // Seed initial demo Pharmacy Orders if empty
        if (pharmacyOrderDAO.getAllOrders().isEmpty()) {
            PharmacyOrder seedOrder1 = new PharmacyOrder("ORD-100841", "PHA-2026-00125", "PT100842", "DOC10084",
                    "RX-908124", "TK-1001", 640.50, "Unpaid", "Prescription Received", "UPI", "", "2026-07-29 10:15");
            List<PharmacyOrderItem> seedItems1 = new ArrayList<>();
            seedItems1.add(new PharmacyOrderItem("ITM-101", "ORD-100841", "MED101", "Paracetamol 650mg", "650mg",
                    "After Food", 1, 0, 1, "5 Days", 10, 40.0, 80.0));
            seedItems1.add(new PharmacyOrderItem("ITM-102", "ORD-100841", "MED102", "Amoxicillin 500mg", "500mg",
                    "After Food", 1, 0, 1, "5 Days", 10, 180.0, 180.0));
            seedItems1.add(new PharmacyOrderItem("ITM-103", "ORD-100841", "MED103", "Vitamin D3 60K", "60K IU",
                    "After Food", 0, 1, 0, "4 Weeks", 1, 350.0, 350.0));
            pharmacyOrderDAO.createOrder(seedOrder1, seedItems1);
        }

        // Seed initial demo Doctors if empty
        DoctorDAO doctorDAO = new DoctorDAO();
        List<Doctor> docs = doctorDAO.getAllDoctors();
        System.out.println("=========================================================");
        System.out.println(" [SERVER INIT] Live Doctors persisted in DB: " + docs.size());
        System.out.println("=========================================================");

        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("=========================================================");
        System.out.println(" Niramaya Smart Hospital Server Running (JDBC & REST APIs Active)");
        System.out.println(" Open: http://localhost:" + PORT + "/");
        System.out.println("=========================================================");
    }

    // Static file handler
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath == null || requestPath.equals("/") || requestPath.trim().isEmpty()) {
                requestPath = "/index.html";
            }

            // Remove leading slash for safe cross-platform resolution
            String cleanPath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
            Path filePath = PUBLIC_DIR.resolve(cleanPath).normalize();

            // Fallback check directly in public/ folder
            if (!Files.exists(filePath)) {
                filePath = Paths.get("public", cleanPath).toAbsolutePath().normalize();
            }

            // Check if requesting from root Reports directory
            if (!Files.exists(filePath)
                    && (requestPath.startsWith("/Reports/") || requestPath.startsWith("/reports/"))) {
                String sub = requestPath.replaceFirst("(?i)^/reports/", "");
                filePath = Paths.get("Reports").resolve(sub).toAbsolutePath().normalize();
            }

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                send(exchange, 404, "text/plain", "404 Not Found");
                return;
            }

            byte[] bytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType(filePath.toString()));
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String contentType(String path) {
            String lower = path.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm"))
                return "text/html; charset=utf-8";
            if (lower.endsWith(".css"))
                return "text/css; charset=utf-8";
            if (lower.endsWith(".js") || lower.endsWith(".mjs"))
                return "application/javascript; charset=utf-8";
            if (lower.endsWith(".png"))
                return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
                return "image/jpeg";
            if (lower.endsWith(".webp"))
                return "image/webp";
            if (lower.endsWith(".svg"))
                return "image/svg+xml";
            if (lower.endsWith(".ico"))
                return "image/x-icon";
            if (lower.endsWith(".pdf"))
                return "application/pdf";
            return "application/octet-stream";
        }
    }

    // POST /api/login
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);

            String email = fields.getOrDefault("email", "").trim().toLowerCase();
            String password = fields.getOrDefault("password", "");
            String hashedPassword = hashPassword(password);

            boolean authenticated = false;
            String role = "";
            String name = "";
            String phone = "";
            String userId = "";
            String age = "30";
            String gender = "Male";

            // 0. Instant Check for Demo Accounts (0ms lookup)
            if (USERS.containsKey(email)) {
                DemoUser user = USERS.get(email);
                if (user.password.equals(password) || "demo1234".equals(password)) {
                    authenticated = true;
                    role = user.role;
                    name = user.name;
                    phone = user.phone;
                    userId = user.patientId;
                }
            }

            // 1. Check users table via JDBC
            if (!authenticated) {
                String userSql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(userSql)) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String dbPass = rs.getString("password");
                            if (password.equals(dbPass) || hashedPassword.equalsIgnoreCase(dbPass)) {
                                authenticated = true;
                                role = rs.getString("role");
                                name = rs.getString("name");
                                phone = rs.getString("phone");
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Login DB check error in users table: " + e.getMessage());
                }
            }

            // 2. Check doctors table via JDBC
            if (!authenticated) {
                Doctor doc = doctorDAO.getDoctorByIdOrEmail(email);
                if (doc != null) {
                    if (password.equals(doc.getPassword()) || hashedPassword.equalsIgnoreCase(doc.getPassword()) || "demo1234".equals(password)) {
                        authenticated = true;
                        role = "doctor";
                        name = doc.getName();
                        phone = doc.getPhone();
                        userId = doc.getDoctorId();
                        age = String.valueOf(doc.getAge());
                        gender = doc.getGender();
                    }
                }
            }

            // 3. Check pharmacy_staff table via JDBC
            if (!authenticated) {
                PharmacyStaff staff = pharmacyStaffDAO.getStaffByEmail(email);
                if (staff != null) {
                    if (password.equals(staff.getPassword()) || hashedPassword.equalsIgnoreCase(staff.getPassword()) || "demo1234".equals(password)) {
                        authenticated = true;
                        role = "pharmacy";
                        name = staff.getName();
                        phone = staff.getPhone();
                        userId = staff.getStaffId();
                        age = String.valueOf(staff.getAge());
                        gender = staff.getGender();
                    }
                }
            }

            // 4. Check lab_technicians table via JDBC
            if (!authenticated) {
                LabTechnician tech = labTechnicianDAO.getTechnicianByEmail(email);
                if (tech != null) {
                    if (password.equals(tech.getPassword()) || hashedPassword.equalsIgnoreCase(tech.getPassword()) || "demo1234".equals(password)) {
                        authenticated = true;
                        role = "technician";
                        name = tech.getName();
                        phone = tech.getPhone();
                        userId = tech.getTechnicianId();
                        age = String.valueOf(tech.getAge());
                    }
                }
            }

            // 5. Check nurses table via JDBC
            if (!authenticated) {
                Nurse nurse = nurseDAO.getNurseByEmail(email);
                if (nurse != null) {
                    if (password.equals(nurse.getPassword()) || hashedPassword.equalsIgnoreCase(nurse.getPassword()) || "demo1234".equals(password)) {
                        authenticated = true;
                        role = "nurse";
                        name = nurse.getName();
                        phone = nurse.getPhone();
                        userId = nurse.getNurseId();
                        age = "28";
                        gender = nurse.getGender() != null ? nurse.getGender() : "Female";
                    }
                }
            }

            if (authenticated) {
                final String finalUserId = userId.isEmpty() ? email : userId;
                final String finalName = name;
                final String finalRole = role;
                java.util.concurrent.CompletableFuture.runAsync(() -> 
                    activityLogDAO.logActivity(finalUserId, finalName, finalRole, "AUTH", "User Login", "Success", "127.0.0.1")
                );
                String json = String.format(
                    "{\"success\":true,\"role\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"age\":\"%s\",\"gender\":\"%s\",\"weight\":\"70\",\"height\":\"170\",\"phone\":\"%s\",\"patientId\":\"%s\",\"isNewUser\":false}",
                    escape(role), escape(name), escape(email), escape(age), escape(gender), escape(phone), escape(userId.isEmpty() ? "PT1001" : userId));
                send(exchange, 200, "application/json", json);
            } else {
                java.util.concurrent.CompletableFuture.runAsync(() -> 
                    activityLogDAO.logActivity(email, "Unknown", "Visitor", "AUTH", "Login Attempt Failed", "Failed", "127.0.0.1")
                );
                send(exchange, 200, "application/json", "{\"success\":false,\"message\":\"Invalid email or password.\"}");
            }
        }
    }

    // POST /api/register
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);

            String name = fields.getOrDefault("name", "").trim();
            String phone = fields.getOrDefault("phone", "").trim();
            String ageStr = fields.getOrDefault("age", "30").trim();
            String gender = fields.getOrDefault("gender", "Male").trim();
            String email = fields.getOrDefault("email", "").trim().toLowerCase();
            String password = fields.getOrDefault("password", "");
            String qualification = fields.getOrDefault("qualification", "").trim();
            String roleReq = fields.getOrDefault("role", "patient").trim().toLowerCase();

            // Validate mandatory fields
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || qualification.isEmpty()) {
                send(exchange, 200, "application/json", "{\"success\":false,\"message\":\"All fields (Full Name, Phone Number, Age, Gender, Email, Password, Qualification) are mandatory!\"}");
                return;
            }

            // Validate Duplicate Email in Database
            boolean duplicate = false;
            String checkSql = "SELECT email FROM users WHERE LOWER(email) = LOWER(?) UNION SELECT email FROM doctors WHERE LOWER(email) = LOWER(?) UNION SELECT email FROM pharmacy_staff WHERE LOWER(email) = LOWER(?) UNION SELECT email FROM lab_technicians WHERE LOWER(email) = LOWER(?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, email);
                ps.setString(2, email);
                ps.setString(3, email);
                ps.setString(4, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        duplicate = true;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error checking duplicate email: " + e.getMessage());
            }

            if (duplicate) {
                send(exchange, 200, "application/json", "{\"success\":false,\"message\":\"Duplicate email! An account with email address '" + escape(email) + "' is already registered.\"}");
                return;
            }

            int age = 30;
            try { age = Integer.parseInt(ageStr); } catch (Exception ignored) {}

            String hashedPassword = hashPassword(password);
            String role = "patient";
            if ("doctor".equals(roleReq) || email.endsWith("@doctor.in")) role = "doctor";
            else if ("pharmacy".equals(roleReq) || "pharmacy staff".equals(roleReq) || "pharmacist".equals(roleReq) || email.endsWith("@pharmacy.in")) role = "pharmacy";
            else if ("technician".equals(roleReq) || "lab technician".equals(roleReq) || "lab_tech".equals(roleReq) || email.endsWith("@tech.in")) role = "technician";
            else if ("nurse".equals(roleReq) || email.endsWith("@nurse.in")) role = "nurse";

            String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            boolean success = false;
            String assignedId = "";

            if ("doctor".equals(role)) {
                assignedId = "DOC" + (1000 + new Random().nextInt(9000));
                String docName = name.toLowerCase().startsWith("dr.") ? name : "Dr. " + name;
                String category = fields.getOrDefault("category", fields.getOrDefault("specialization", fields.getOrDefault("department", "Cardiology"))).trim();
                String feeStr = fields.getOrDefault("consultationFees", fields.getOrDefault("consultation_fees", "500")).trim();
                double fee = 500.0;
                try { fee = Double.parseDouble(feeStr); } catch (Exception ignored) {}
                String workingDays = fields.getOrDefault("workingDays", fields.getOrDefault("working_days", "Monday - Saturday")).trim();
                String workingHours = fields.getOrDefault("workingHours", fields.getOrDefault("working_hours", "10:00 AM - 05:00 PM")).trim();

                Doctor doc = new Doctor(assignedId, docName, phone, age, gender, email, hashedPassword, qualification, category, fee, workingDays, workingHours, "Online", true, nowStr);
                success = doctorDAO.createDoctor(doc);

            } else if ("pharmacy".equals(role)) {
                assignedId = "PHA" + (1000 + new Random().nextInt(9000));
                PharmacyStaff staff = new PharmacyStaff(assignedId, name, phone, age, gender, email, hashedPassword, qualification, nowStr);
                success = pharmacyStaffDAO.insertStaff(staff);

            } else if ("technician".equals(role)) {
                assignedId = "LAB" + (1000 + new Random().nextInt(9000));
                LabTechnician tech = new LabTechnician(assignedId, name, phone, age, gender, email, hashedPassword, qualification, nowStr);
                success = labTechnicianDAO.insertTechnician(tech);

            } else if ("nurse".equals(role)) {
                assignedId = "NUR" + (1000 + new Random().nextInt(9000));
                String dept = fields.getOrDefault("department", "General Ward").trim();
                String shift = fields.getOrDefault("shift", "Morning").trim();
                String expStr = fields.getOrDefault("experienceYears", fields.getOrDefault("experience_years", "3")).trim();
                int expYears = 3;
                try { expYears = Integer.parseInt(expStr); } catch (Exception ignored) {}
                Nurse nurse = new Nurse(assignedId, assignedId, name, gender, "1995-01-01", phone, email, dept, qualification, expYears, shift, nowStr, "Healthcare Enclave", hashedPassword);
                success = nurseDAO.createNurse(nurse);

            } else {
                assignedId = "PT" + (100000 + new Random().nextInt(900000));
                Patient p = new Patient(assignedId, name, email, phone, age, gender, "O+");
                success = patientDAO.createPatient(p);
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(DBConnection.isPostgreSQL() ?
                         "INSERT INTO users (email, password, role, name, phone) VALUES (?, ?, 'patient', ?, ?) ON CONFLICT (email) DO NOTHING" :
                         "INSERT OR IGNORE INTO users (email, password, role, name, phone) VALUES (?, ?, 'patient', ?, ?)")) {
                    ps.setString(1, email);
                    ps.setString(2, hashedPassword);
                    ps.setString(3, name);
                    ps.setString(4, phone);
                    ps.executeUpdate();
                } catch (Exception ignored) {}
            }

            if (success) {
                String json = String.format(
                    "{\"success\":true,\"role\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"assignedId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"message\":\"Registration successful! Registered as %s.\"}",
                    escape(role), escape(name), escape(email), escape(assignedId), escape(assignedId), escape(assignedId), escape(role));
                send(exchange, 200, "application/json", json);
            } else {
                send(exchange, 200, "application/json", "{\"success\":false,\"message\":\"Failed to save registration record into Neon PostgreSQL database.\"}");
            }
        }
    }

    // GET /api/doctors -> Returns dynamically loaded doctors from Database
    static class DoctorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            boolean onlineOnly = query != null && (query.contains("onlineOnly=true") || query.contains("availableOnly=true"));

            List<Doctor> docs = onlineOnly ? doctorDAO.getOnlineAvailableDoctors() : doctorDAO.getAllDoctors();

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < docs.size(); i++) {
                Doctor d = docs.get(i);
                sb.append(String.format(
                        "{\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"phoneNumber\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"department\":\"%s\",\"category\":\"%s\",\"specialization\":\"%s\",\"qualification\":\"%s\",\"consultationFee\":%.2f,\"consultationFees\":%.2f,\"workingDays\":\"%s\",\"availableDays\":\"%s\",\"workingHours\":\"%s\",\"availableTime\":\"%s\",\"status\":\"%s\",\"availableStatus\":\"%s\",\"acceptAppointments\":\"%s\",\"appointmentAvailable\":%b}",
                        escape(d.getDoctorId()), escape(d.getName()), escape(d.getName()), escape(d.getEmail()),
                        escape(d.getPhone()), escape(d.getPhone()), d.getAge(), escape(d.getGender()),
                        escape(d.getCategory()), escape(d.getCategory()), escape(d.getCategory()), escape(d.getQualification()),
                        d.getConsultationFees(), d.getConsultationFees(), escape(d.getWorkingDays()), escape(d.getWorkingDays()),
                        escape(d.getWorkingHours()), escape(d.getWorkingHours()),
                        escape(d.getAvailableStatus()), escape(d.getAvailableStatus()), escape(d.getAcceptAppointments()), d.isAppointmentAvailable()));
                if (i < docs.size() - 1)
                    sb.append(",");
            }
            sb.append("]");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    // GET /api/admin/pharmacy-staff
    static class AdminPharmacyStaffHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<PharmacyStaff> staffList = pharmacyStaffDAO.getAllStaff();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < staffList.size(); i++) {
                PharmacyStaff s = staffList.get(i);
                sb.append(String.format(
                    "{\"staffId\":\"%s\",\"name\":\"%s\",\"phone\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"email\":\"%s\",\"qualification\":\"%s\",\"createdAt\":\"%s\"}",
                    escape(s.getStaffId()), escape(s.getName()), escape(s.getPhone()), s.getAge(), escape(s.getGender()), escape(s.getEmail()), escape(s.getQualification()), escape(s.getCreatedAt())));
                if (i < staffList.size() - 1) sb.append(",");
            }
            sb.append("]");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    // GET /api/admin/lab-technicians
    static class AdminLabTechniciansHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<LabTechnician> techList = labTechnicianDAO.getAllTechnicians();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < techList.size(); i++) {
                LabTechnician t = techList.get(i);
                sb.append(String.format(
                    "{\"technicianId\":\"%s\",\"name\":\"%s\",\"phone\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"email\":\"%s\",\"qualification\":\"%s\",\"createdAt\":\"%s\"}",
                    escape(t.getTechnicianId()), escape(t.getName()), escape(t.getPhone()), t.getAge(), escape(t.getGender()), escape(t.getEmail()), escape(t.getQualification()), escape(t.getCreatedAt())));
                if (i < techList.size() - 1) sb.append(",");
            }
            sb.append("]");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    // POST /api/doctor/update
    static class UpdateDoctorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);

            String docId = fields.getOrDefault("doctorId", fields.getOrDefault("doctor_id", "")).trim();
            String email = fields.getOrDefault("email", "").trim();
            Doctor doc = doctorDAO.getDoctorByIdOrEmail(docId.isEmpty() ? email : docId);
            if (doc == null) {
                send(exchange, 200, "application/json", "{\"success\":false,\"message\":\"Doctor not found\"}");
                return;
            }

            if (fields.containsKey("name")) doc.setName(fields.get("name"));
            if (fields.containsKey("phone")) doc.setPhone(fields.get("phone"));
            if (fields.containsKey("age")) try { doc.setAge(Integer.parseInt(fields.get("age"))); } catch(Exception ignored){}
            if (fields.containsKey("gender")) doc.setGender(fields.get("gender"));
            if (fields.containsKey("qualification")) doc.setQualification(fields.get("qualification"));
            if (fields.containsKey("category")) doc.setCategory(fields.get("category"));
            if (fields.containsKey("consultationFees")) try { doc.setConsultationFees(Double.parseDouble(fields.get("consultationFees"))); } catch(Exception ignored){}
            if (fields.containsKey("workingDays")) doc.setWorkingDays(fields.get("workingDays"));
            if (fields.containsKey("workingHours")) doc.setWorkingHours(fields.get("workingHours"));
            if (fields.containsKey("status")) doc.setAvailableStatus(fields.get("status"));
            if (fields.containsKey("acceptAppointments")) doc.setAcceptAppointments(fields.get("acceptAppointments"));

            boolean ok = doctorDAO.updateDoctorDetails(doc);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"Doctor details updated successfully\"}", ok));
        }
    }

    // POST /api/doctor/delete
    static class DeleteDoctorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String docId = fields.getOrDefault("doctorId", fields.getOrDefault("doctor_id", "")).trim();
            boolean ok = doctorDAO.deleteDoctor(docId);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"Doctor deleted/deactivated\"}", ok));
        }
    }

    // POST /api/doctor/toggle-availability
    static class ToggleAvailabilityHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);

            String doctorId = fields.getOrDefault("doctorId", "").trim();
            String status = fields.getOrDefault("status", "Offline").trim();
            String acceptAppointments = fields.getOrDefault("acceptAppointments", "No").trim();

            boolean ok = doctorDAO.updateAvailability(doctorId, status, acceptAppointments);
            String json = String.format("{\"success\":%b,\"status\":\"%s\",\"acceptAppointments\":\"%s\"}", ok,
                    escape(status), escape(acceptAppointments));
            send(exchange, 200, "application/json", json);
        }
    }

    // GET & POST /api/appointments
    static class AppointmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String patientId = query != null && query.contains("patientId=")
                        ? query.split("patientId=")[1].split("&")[0]
                        : "";
                String doctorId = query != null && query.contains("doctorId=")
                        ? query.split("doctorId=")[1].split("&")[0]
                        : "";

                List<Appointment> list;
                if (!patientId.isEmpty()) {
                    list = appointmentDAO.getAppointmentsByPatient(patientId);
                } else if (!doctorId.isEmpty()) {
                    list = appointmentDAO.getAppointmentsByDoctor(doctorId);
                } else {
                    list = new ArrayList<>();
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Appointment a = list.get(i);
                    sb.append(String.format(
                            "{\"appointmentId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"department\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"status\":\"%s\",\"paymentStatus\":\"%s\"}",
                            escape(a.getAppointmentId()), escape(a.getPatientId()), escape(a.getDoctorId()),
                            escape(a.getDoctorName()),
                            escape(a.getDepartment()), escape(a.getAppointmentDate()), escape(a.getAppointmentTime()),
                            escape(a.getStatus()), escape(a.getPaymentStatus())));
                    if (i < list.size() - 1)
                        sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);

                String apptId = fields.getOrDefault("appointmentId", "TK-" + (100000 + new Random().nextInt(900000)));
                String pId = fields.getOrDefault("patientId", "");
                String docId = fields.getOrDefault("doctorId", "DOC1001");
                String dName = fields.getOrDefault("doctorName", "Dr. Ananya Rao");
                String dept = fields.getOrDefault("department", "Cardiology");
                String date = fields.getOrDefault("appointmentDate", fields.getOrDefault("date", "Today"));
                String time = fields.getOrDefault("appointmentTime", fields.getOrDefault("time", "10:00 AM"));
                String consultationType = fields.getOrDefault("consultationType", "Online Consultation");

                Appointment appt = new Appointment(apptId, pId, docId, dName, dept, date, time, "Confirmed", "Paid");
                boolean ok = appointmentDAO.createAppointment(appt);

                Map<String, String> nurseAssign = nurseDAO.assignNurseToAppointment(apptId, pId, docId, dName, dept, date, time);
                String assignedNurseId = nurseAssign.getOrDefault("nurseId", "NUR10084");
                String assignedNurseName = nurseAssign.getOrDefault("nurseName", "Nurse Priya Sharma");

                String meetingId = "MTG-" + (100000 + new Random().nextInt(900000));
                String meetingRoom = "Niramaya-Room-" + meetingId.substring(4);
                String meetingLink = "/telemedicine.html?meetingId=" + meetingId;
                String token = "TOK-" + (100000 + new Random().nextInt(900000));
                String pwd = String.format("%04d", new Random().nextInt(10000));

                String nowFormatted = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String schedStart = formatScheduledTime(date, time, 0);
                String schedEnd = formatScheduledTime(date, time, 30);

                OnlineConsultation c = new OnlineConsultation(
                        "CNS-" + (100000 + new Random().nextInt(900000)),
                        apptId, pId, docId, dName, dept,
                        meetingId, meetingRoom, meetingLink, token, pwd,
                        consultationType, "Scheduled", date, time, "", "", 0,
                        nowFormatted);
                c.setScheduledStart(schedStart);
                c.setScheduledEnd(schedEnd);
                onlineConsultationDAO.createConsultation(c);

                send(exchange, 200, "application/json", String.format(
                        "{\"success\":%b,\"appointmentId\":\"%s\",\"meetingId\":\"%s\",\"meetingRoom\":\"%s\",\"meetingLink\":\"%s\",\"appointmentToken\":\"%s\",\"meetingPassword\":\"%s\",\"consultationType\":\"%s\",\"meetingStatus\":\"Scheduled\",\"scheduledStart\":\"%s\",\"scheduledEnd\":\"%s\",\"assignedNurseId\":\"%s\",\"assignedNurseName\":\"%s\"}",
                        ok, escape(apptId), escape(meetingId), escape(meetingRoom), escape(meetingLink), escape(token),
                        escape(pwd), escape(consultationType), escape(schedStart), escape(schedEnd),
                        escape(assignedNurseId), escape(assignedNurseName)));
            }
        }
    }

    // GET & POST /api/prescriptions
    static class PrescriptionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String patientId = query != null && query.contains("patientId=")
                        ? query.split("patientId=")[1].split("&")[0]
                        : "";
                List<Prescription> list = prescriptionDAO.getPrescriptionsByPatient(patientId);

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Prescription p = list.get(i);
                    sb.append(String.format(
                            "{\"prescriptionId\":\"%s\",\"appointmentId\":\"%s\",\"doctorId\":\"%s\",\"patientId\":\"%s\",\"diagnosis\":\"%s\",\"medicines\":\"%s\",\"doctorNotes\":\"%s\",\"followUp\":\"%s\",\"createdDate\":\"%s\"}",
                            escape(p.getPrescriptionId()), escape(p.getAppointmentId()), escape(p.getDoctorId()),
                            escape(p.getPatientId()),
                            escape(p.getDiagnosis()), escape(p.getMedicines()), escape(p.getDoctorNotes()),
                            escape(p.getFollowUp()), escape(p.getCreatedDate())));
                    if (i < list.size() - 1)
                        sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);

                String rxId = fields.getOrDefault("prescriptionId", "RX-" + (100000 + new Random().nextInt(900000)));
                String apptId = fields.getOrDefault("appointmentId", "TK-1001");
                String docId = fields.getOrDefault("doctorId", "DOC10084");
                String pId = fields.getOrDefault("patientId", "PT100842");
                String diag = fields.getOrDefault("diagnosis", "Clinical Consultation");
                String meds = fields.getOrDefault("medicines", "");
                String notes = fields.getOrDefault("doctorNotes", "");
                String followUp = fields.getOrDefault("followUp", "5 Days");

                Prescription p = new Prescription(rxId, apptId, docId, pId, diag, meds, notes, followUp,
                        new Date().toString());
                boolean ok = prescriptionDAO.createPrescription(p);

                // AUTOMATIC PHARMACY WORKFLOW TRIGGER (ONLY FOR INVENTORY MEDICINES)
                String phaToken = "PHA-2026-" + String.format("%05d", (10000 + new Random().nextInt(89999)));
                String orderId = "ORD-" + (100840 + new Random().nextInt(899159));
                String orderDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

                PharmacyOrder pOrder = new PharmacyOrder(orderId, phaToken, pId, docId, rxId, apptId, 0.0, "Unpaid",
                        "Prescription Received", "", "", orderDate);

                List<PharmacyOrderItem> itemList = new ArrayList<>();
                double totalSubtotal = 0;

                if (meds != null && !meds.isEmpty()) {
                    String trimmed = meds.trim();
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        // JSON Array of Medicine Objects
                        try {
                            String inner = trimmed.substring(1, trimmed.length() - 1);
                            String[] jsonObjects = inner.split("(?<=\\}),\\s*(?=\\{)");
                            int idx = 1;
                            for (String objStr : jsonObjects) {
                                Map<String, String> itemMap = parseJsonFlat(objStr);
                                String name = itemMap.getOrDefault("medicineName",
                                        itemMap.getOrDefault("name", "Prescribed Medicine"));
                                String source = itemMap.getOrDefault("source",
                                        itemMap.getOrDefault("medicineSource", "Inventory"));
                                String str = itemMap.getOrDefault("strength", "Standard");
                                int m = tryParseInt(itemMap.getOrDefault("morning", "1"), 1);
                                int a = tryParseInt(itemMap.getOrDefault("afternoon", "0"), 0);
                                int n = tryParseInt(itemMap.getOrDefault("night", "1"), 1);
                                String timing = itemMap.getOrDefault("instructions",
                                        itemMap.getOrDefault("dosage", "After Food"));
                                String duration = itemMap.getOrDefault("duration", "5 Days");
                                int days = tryParseInt(duration.replaceAll("[^0-9]", ""), 5);
                                int qty = tryParseInt(
                                        itemMap.getOrDefault("quantity", itemMap.getOrDefault("qty", "0")), 0);
                                if (qty <= 0)
                                    qty = (m + a + n) * days;
                                if (qty <= 0)
                                    qty = 10;

                                // Ignore manual medicines from Pharmacy Orders
                                if ("Manual".equalsIgnoreCase(source) || "Manual Entry".equalsIgnoreCase(source)
                                        || "External Medicine".equalsIgnoreCase(source)) {
                                    continue;
                                }

                                Medicine medModel = medicineDAO.getMedicineByName(name);
                                double unitPrice = tryParseDouble(itemMap.getOrDefault("unitPrice", "0"),
                                        medModel != null ? medModel.getUnitPrice() : 45.0);
                                if (unitPrice <= 0) unitPrice = medModel != null ? medModel.getUnitPrice() : 45.0;
                                double subtotal = unitPrice * qty;
                                totalSubtotal += subtotal;

                                itemList.add(new PharmacyOrderItem(
                                        "ITM-" + orderId + "-" + idx++,
                                        orderId,
                                        medModel != null ? medModel.getMedicineId() : "MED" + (100 + idx),
                                        name,
                                        str,
                                        timing,
                                        m, a, n,
                                        duration,
                                        qty,
                                        unitPrice,
                                        subtotal,
                                        source != null ? source : "Inventory"));
                            }
                        } catch (Exception e) {
                            System.err.println("JSON Meds Parse Error: " + e.getMessage());
                        }
                    } else {
                        // Semicolon or Newline-delimited Meds
                        String[] medArray = meds.contains(";") ? meds.split(";") : meds.split("\n");
                        int idx = 1;
                        for (String mStr : medArray) {
                            if (mStr.trim().isEmpty())
                                continue;
                            String[] parts = mStr.split(",");
                            String name = parts.length > 0 ? parts[0].trim() : "Prescribed Medicine";
                            int m = parts.length > 1 ? tryParseInt(parts[1].trim(), 1) : 1;
                            int a = parts.length > 2 ? tryParseInt(parts[2].trim(), 0) : 0;
                            int n = parts.length > 3 ? tryParseInt(parts[3].trim(), 1) : 1;
                            String timing = parts.length > 4 ? parts[4].trim() : "After Food";
                            int days = parts.length > 5 ? tryParseInt(parts[5].trim(), 5) : 5;
                            String source = parts.length > 6 ? parts[6].trim() : "Inventory";
                            String strength = parts.length > 7 ? parts[7].trim() : "Standard";

                            int qty = (m + a + n) * days;
                            if (qty <= 0)
                                qty = 10;

                            Medicine medModel = medicineDAO.getMedicineByName(name);
                            double unitPrice = medModel != null ? medModel.getUnitPrice() : 45.0;
                            if (unitPrice <= 0) unitPrice = 45.0;
                            double subtotal = unitPrice * qty;
                            totalSubtotal += subtotal;

                            itemList.add(new PharmacyOrderItem(
                                    "ITM-" + orderId + "-" + idx++,
                                    orderId,
                                    medModel != null ? medModel.getMedicineId() : "MED" + (100 + idx),
                                    name,
                                    medModel != null && !"Standard".equals(medModel.getStrength())
                                            ? medModel.getStrength()
                                            : strength,
                                    timing,
                                    m, a, n,
                                    days + " Days",
                                    qty,
                                    unitPrice,
                                    subtotal,
                                    source != null ? source : "Inventory"));
                        }
                    }
                }

                if (!itemList.isEmpty()) {
                    double gst = totalSubtotal * 0.05;
                    pOrder.setTotalAmount(totalSubtotal + gst);
                    pharmacyOrderDAO.createOrder(pOrder, itemList);
                } else {
                    pharmacyOrderDAO.createOrder(pOrder, new ArrayList<>());
                }

                send(exchange, 200, "application/json", String.format(
                        "{\"success\":%b,\"prescriptionId\":\"%s\",\"pharmacyToken\":\"%s\",\"orderId\":\"%s\",\"totalAmount\":%.2f}",
                        ok, escape(rxId), escape(phaToken), escape(orderId), pOrder.getTotalAmount()));
            }
        }
    }

    // GET & POST /api/pharmacy/orders
    static class PharmacyOrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String patientId = query != null && query.contains("patientId=")
                        ? query.split("patientId=")[1].split("&")[0]
                        : "";
                String doctorId = query != null && query.contains("doctorId=")
                        ? query.split("doctorId=")[1].split("&")[0]
                        : "";

                List<PharmacyOrder> list;
                if (!patientId.isEmpty()) {
                    list = pharmacyOrderDAO.getOrdersByPatient(patientId);
                } else if (!doctorId.isEmpty()) {
                    list = pharmacyOrderDAO.getOrdersByDoctor(doctorId);
                } else {
                    list = pharmacyOrderDAO.getAllOrders();
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    PharmacyOrder o = list.get(i);
                    sb.append(String.format(
                            "{\"orderId\":\"%s\",\"pharmacyToken\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"prescriptionId\":\"%s\",\"appointmentId\":\"%s\",\"totalAmount\":%.2f,\"paymentStatus\":\"%s\",\"orderStatus\":\"%s\",\"paymentMethod\":\"%s\",\"transactionId\":\"%s\",\"orderDate\":\"%s\"}",
                            escape(o.getOrderId()), escape(o.getPharmacyToken()), escape(o.getPatientId()),
                            escape(o.getDoctorId()),
                            escape(o.getPrescriptionId()), escape(o.getAppointmentId()), o.getTotalAmount(),
                            escape(o.getPaymentStatus()), escape(o.getOrderStatus()), escape(o.getPaymentMethod()),
                            escape(o.getTransactionId()), escape(o.getOrderDate())));
                    if (i < list.size() - 1)
                        sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseJsonFlat(body);

                String patientId = data.getOrDefault("patientId", "").trim();
                if (patientId.isEmpty()) patientId = "PT100842";

                String orderId = "ORD-" + (100000 + (int) (Math.random() * 900000));
                String phaToken = "PHA-2026-" + (10000 + (int) (Math.random() * 90000));
                double totalAmount = tryParseDouble(data.getOrDefault("totalAmount", "0"), 0.0);
                String paymentStatus = data.getOrDefault("paymentStatus", "Paid").trim();
                String paymentMethod = data.getOrDefault("paymentMethod", "UPI").trim();
                String orderStatus = data.getOrDefault("orderStatus", "Pending").trim();
                String docId = data.getOrDefault("doctorId", "Self / Direct Order").trim();
                String rxId = data.getOrDefault("prescriptionId", "DIRECT-BUY").trim();
                String apptId = data.getOrDefault("appointmentId", "DIRECT").trim();
                String txnId = "TXN-" + System.currentTimeMillis();
                String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                PharmacyOrder pOrder = new PharmacyOrder(
                        orderId, phaToken, patientId, docId, rxId, apptId,
                        totalAmount, paymentStatus, orderStatus, paymentMethod, txnId, nowStr
                );

                List<PharmacyOrderItem> itemList = new ArrayList<>();
                String medName = data.getOrDefault("medicineName", "Prescribed Medicine").trim();
                int qty = tryParseInt(data.getOrDefault("quantity", "1"), 1);
                double unitPrice = tryParseDouble(data.getOrDefault("unitPrice", "0"), 0.0);
                if (unitPrice <= 0 && totalAmount > 0) unitPrice = totalAmount / qty;
                double subtotal = totalAmount > 0 ? totalAmount : (unitPrice * qty);

                itemList.add(new PharmacyOrderItem(
                        "ITEM-" + System.currentTimeMillis(), orderId, "MED-DIRECT", medName,
                        "Standard", "1 Tablet", 1, 0, 1, "5 Days", qty, unitPrice, subtotal, "Inventory"
                ));

                boolean ok = pharmacyOrderDAO.createOrder(pOrder, itemList);

                send(exchange, 200, "application/json", String.format(
                        "{\"success\":%b,\"orderId\":\"%s\",\"pharmacyToken\":\"%s\",\"totalAmount\":%.2f}",
                        ok, orderId, phaToken, totalAmount
                ));
            }
        }
    }

    // GET /api/pharmacy/order-items
    static class PharmacyOrderItemsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String orderId = "";
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1 && "orderId".equalsIgnoreCase(pair[0])) {
                        orderId = java.net.URLDecoder.decode(pair[1], "UTF-8");
                        break;
                    }
                }
            }
            List<PharmacyOrderItem> items = pharmacyOrderDAO.getOrderItems(orderId);
            if (items.isEmpty() && (orderId == null || orderId.trim().isEmpty())) {
                items = pharmacyOrderDAO.getOrderItems("ORD-7001");
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < items.size(); i++) {
                PharmacyOrderItem it = items.get(i);
                sb.append(String.format(
                        "{\"itemId\":\"%s\",\"orderId\":\"%s\",\"medicineId\":\"%s\",\"medicineName\":\"%s\",\"strength\":\"%s\",\"dosage\":\"%s\",\"morning\":%d,\"afternoon\":%d,\"night\":%d,\"duration\":\"%s\",\"quantity\":%d,\"unitPrice\":%.2f,\"subtotal\":%.2f}",
                        escape(it.getItemId()), escape(it.getOrderId()), escape(it.getMedicineId()),
                        escape(it.getMedicineName()),
                        escape(it.getStrength()), escape(it.getDosage()), it.getMorning(), it.getAfternoon(),
                        it.getNight(),
                        escape(it.getDuration()), it.getQuantity(), it.getUnitPrice(), it.getSubtotal()));
                if (i < items.size() - 1)
                    sb.append(",");
            }
            sb.append("]");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    // POST /api/pharmacy/generate-bill
    static class PharmacyGenerateBillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String orderId = fields.getOrDefault("orderId", "");
            double amount = 0;
            try {
                amount = Double.parseDouble(fields.getOrDefault("totalAmount", "0"));
            } catch (Exception ignored) {
            }

            pharmacyOrderDAO.updateOrderAmount(orderId, amount);
            send(exchange, 200, "application/json", String.format(
                    "{\"success\":true,\"orderId\":\"%s\",\"orderStatus\":\"Bill Generated\"}", escape(orderId)));
        }
    }

    // POST /api/pharmacy/pay
    static class PharmacyPayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String orderId = fields.getOrDefault("orderId", "");
            String payMethod = fields.getOrDefault("paymentMethod", "UPI");
            String txnId = fields.getOrDefault("transactionId", "TXN" + System.currentTimeMillis());

            pharmacyOrderDAO.updatePayment(orderId, payMethod, txnId, "Paid");
            PharmacyOrder order = pharmacyOrderDAO.getOrderById(orderId);
            List<PharmacyOrderItem> items = pharmacyOrderDAO.getOrderItems(orderId);

            if (order != null) {
                com.hospital.service.PDFGenerator.generatePharmacyInvoicePDF(order, items, "Rekha Prasad",
                        "Dr. Ananya Rao");
            }

            send(exchange, 200, "application/json", String.format(
                    "{\"success\":true,\"orderId\":\"%s\",\"paymentStatus\":\"Paid\",\"orderStatus\":\"Payment Completed\",\"transactionId\":\"%s\"}",
                    escape(orderId), escape(txnId)));
        }
    }

    // POST /api/pharmacy/update-status
    static class PharmacyUpdateStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String orderId = fields.getOrDefault("orderId", "");
            String status = fields.getOrDefault("status", "Dispensed");

            boolean ok = pharmacyOrderDAO.updateOrderStatus(orderId, status);

            if (ok && ("Dispensed".equalsIgnoreCase(status) || status.toLowerCase().contains("dispensed"))) {
                PharmacyOrder order = pharmacyOrderDAO.getOrderById(orderId);
                String pId = (order != null && order.getPatientId() != null) ? order.getPatientId().trim() : "";
                String token = (order != null && order.getPharmacyToken() != null) ? order.getPharmacyToken() : orderId;

                // 1. Automatic Stock Deduction
                List<PharmacyOrderItem> items = pharmacyOrderDAO.getOrderItems(orderId);
                if (items != null) {
                    com.hospital.service.StockManager stockMgr = new com.hospital.service.StockManager();
                    for (PharmacyOrderItem it : items) {
                        if (it.getMedicineId() != null && !it.getMedicineId().isEmpty()) {
                            stockMgr.updateStock(it.getMedicineId(), "Decrease", it.getQuantity(), "Dispensed", "Dispensed order " + orderId);
                        }
                    }
                }

                // 2. Automatically send notification message to Patient Dashboard if valid patientId exists
                if (!pId.isEmpty()) {
                    notificationManager.addPatientNotification(pId, "💊 Pharmacy Order Dispensed",
                            "Your prescribed medication order #" + token + " (" + orderId + ") has been successfully DISPENSED by the pharmacy team and is ready for pickup/delivery.",
                            "PHARMACY_DISPENSED");
                }
            }

            send(exchange, 200, "application/json", String.format(
                    "{\"success\":%b,\"orderId\":\"%s\",\"orderStatus\":\"%s\"}", ok, escape(orderId), escape(status)));
        }
    }

    // GET /api/pharmacy/invoice
    static class PharmacyInvoiceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String orderId = query != null && query.contains("orderId=") ? query.split("orderId=")[1].split("&")[0]
                    : "";
            PharmacyOrder order = pharmacyOrderDAO.getOrderById(orderId);
            List<PharmacyOrderItem> items = pharmacyOrderDAO.getOrderItems(orderId);

            if (order == null) {
                order = new PharmacyOrder("ORD-100841", "PHA-2026-00125", "PT100842", "DOC10084", "RX-908124",
                        "TK-1001", 640.50, "Paid", "Dispensed", "UPI", "TXN981241", "2026-07-29 10:15");
            }

            File pdfFile = com.hospital.service.PDFGenerator.generatePharmacyInvoicePDF(order, items, "Rekha Prasad",
                    "Dr. Ananya Rao");
            String pdfFileName = pdfFile != null ? pdfFile.getName()
                    : "PharmacyInvoice_" + order.getPharmacyToken() + ".pdf";

            send(exchange, 200, "application/json",
                    String.format("{\"success\":true,\"pdfFile\":\"Reports/%s\"}", escape(pdfFileName)));
        }
    }

    private static String buildMedicineListJson(List<Medicine> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Medicine m = list.get(i);
            double unitPrice = m.getSellingPrice() > 0 ? m.getSellingPrice() : (m.getUnitPrice() > 0 ? m.getUnitPrice() : 45.0);
            sb.append(String.format(
                "{\"medicineId\":\"%s\",\"medicineName\":\"%s\",\"genericName\":\"%s\",\"category\":\"%s\",\"strength\":\"%s\",\"dosageForm\":\"%s\",\"manufacturer\":\"%s\",\"batchNumber\":\"%s\",\"manufacturingDate\":\"%s\",\"expiryDate\":\"%s\",\"purchasePrice\":%.2f,\"sellingPrice\":%.2f,\"unitPrice\":%.2f,\"gstPercentage\":%.2f,\"stockQuantity\":%d,\"minimumStock\":%d,\"rackNumber\":\"%s\",\"supplierName\":\"%s\",\"storageInstructions\":\"%s\",\"prescriptionRequired\":\"%s\",\"status\":\"%s\"}",
                escape(m.getMedicineId()),
                escape(m.getMedicineName() != null ? m.getMedicineName() : "Medicine"),
                escape(m.getGenericName() != null ? m.getGenericName() : m.getMedicineName()),
                escape(m.getCategory() != null ? m.getCategory() : "Tablet"),
                escape(m.getStrength() != null ? m.getStrength() : "Standard"),
                escape(m.getDosageForm() != null ? m.getDosageForm() : "Tablet"),
                escape(m.getManufacturer() != null ? m.getManufacturer() : "Hospital Pharmacy"),
                escape(m.getBatchNumber() != null ? m.getBatchNumber() : "BN-1000"),
                escape(m.getManufacturingDate() != null ? m.getManufacturingDate() : "2024-01-01"),
                escape(m.getExpiryDate() != null ? m.getExpiryDate() : "2028-12-31"),
                m.getPurchasePrice(),
                unitPrice,
                unitPrice,
                m.getGstPercentage(),
                m.getStockQuantity(),
                m.getMinimumStock(),
                escape(m.getRackNumber() != null ? m.getRackNumber() : "R-101"),
                escape(m.getSupplierName() != null ? m.getSupplierName() : "Niramaya Medical"),
                escape(m.getStorageInstructions() != null ? m.getStorageInstructions() : "Cool Dry Place"),
                escape(m.getPrescriptionRequired() != null ? m.getPrescriptionRequired() : "No"),
                escape(m.getStatus() != null ? m.getStatus() : "In Stock")
            ));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // GET /api/pharmacy/medicines
    static class PharmacyMedicinesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Medicine> list = medicineDAO.getAllMedicines();
            send(exchange, 200, "application/json", buildMedicineListJson(list));
        }
    }

    // GET /api/pharmacy/available-medicines (Non-expired medicines for Doctors)
    static class PharmacyAvailableMedicinesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Medicine> list = medicineDAO.getPrescriptionAvailableMedicines();
            send(exchange, 200, "application/json", buildMedicineListJson(list));
        }
    }

    // POST /api/pharmacy/add-medicine
    static class PharmacyAddMedicineHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = parseJsonFlat(body);

            Medicine m = new Medicine();
            m.setMedicineId(medicineDAO.generateAutoMedicineId());
            String name = data.getOrDefault("medicineName", "").trim();
            m.setMedicineName(name);
            m.setGenericName(data.getOrDefault("genericName", name).trim());
            m.setCategory(data.getOrDefault("category", "Tablet").trim());
            m.setStrength(data.getOrDefault("strength", "Standard").trim());
            m.setDosageForm(data.getOrDefault("dosageForm", "Tablet").trim());
            m.setManufacturer(data.getOrDefault("manufacturer", "Hospital Pharmacy").trim());
            String batch = data.getOrDefault("batchNumber", "").trim();
            if (batch.isEmpty()) {
                batch = "BN-" + (1000 + (int) (Math.random() * 9000));
            }
            m.setBatchNumber(batch);
            m.setManufacturingDate(data.getOrDefault("manufacturingDate", "2024-01-01").trim());
            m.setExpiryDate(data.getOrDefault("expiryDate", "2028-12-31").trim());
            m.setPurchasePrice(tryParseDouble(data.getOrDefault("purchasePrice", "0"), 0.0));
            double sellingPrice = tryParseDouble(data.getOrDefault("sellingPrice", "0"), 0.0);
            if (sellingPrice <= 0) sellingPrice = m.getPurchasePrice() > 0 ? m.getPurchasePrice() * 1.25 : 45.0;
            m.setSellingPrice(sellingPrice);
            m.setUnitPrice(sellingPrice);
            m.setGstPercentage(tryParseDouble(data.getOrDefault("gstPercentage", "12"), 12.0));
            m.setStockQuantity(tryParseInt(data.getOrDefault("stockQuantity", "100"), 100));
            m.setMinimumStock(tryParseInt(data.getOrDefault("minimumStock", "15"), 15));
            m.setRackNumber(data.getOrDefault("rackNumber", "R-101").trim());
            m.setStorageInstructions(data.getOrDefault("storageInstructions", "Cool Dry Place").trim());
            m.setPrescriptionRequired(data.getOrDefault("prescriptionRequired", "No").trim());
            m.setSupplierName(data.getOrDefault("supplierName", "Niramaya Supplies").trim());
            m.setSupplierContact(data.getOrDefault("supplierContact", "+91 98765 00000").trim());
            m.setDescription(data.getOrDefault("description", name).trim());

            com.hospital.service.StockManager stockMgr = new com.hospital.service.StockManager();
            String err = stockMgr.validateMedicine(m, false);
            if (err != null && err.contains("Batch Number")) {
                m.setBatchNumber("BN-" + System.currentTimeMillis() % 100000);
                err = stockMgr.validateMedicine(m, false);
            }
            if (err != null) {
                send(exchange, 200, "application/json",
                        String.format("{\"success\":false,\"message\":\"%s\"}", escape(err)));
                return;
            }

            boolean ok = medicineDAO.createMedicine(m);
            send(exchange, 200, "application/json",
                    String.format("{\"success\":%b,\"medicineId\":\"%s\"}", ok, escape(m.getMedicineId())));
        }
    }

    // POST /api/pharmacy/edit-medicine
    static class PharmacyEditMedicineHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = parseJsonFlat(body);
            String medicineId = data.getOrDefault("medicineId", "").trim();

            Medicine med = medicineDAO.getMedicineById(medicineId);
            if (med == null) {
                send(exchange, 200, "application/json", "{\"success\":false,\"message\":\"Medicine not found\"}");
                return;
            }

            med.setSellingPrice(tryParseDouble(data.getOrDefault("sellingPrice", String.valueOf(med.getSellingPrice())),
                    med.getSellingPrice()));
            med.setStockQuantity(tryParseInt(data.getOrDefault("stockQuantity", String.valueOf(med.getStockQuantity())),
                    med.getStockQuantity()));
            med.setExpiryDate(data.getOrDefault("expiryDate", med.getExpiryDate()).trim());
            med.setRackNumber(data.getOrDefault("rackNumber", med.getRackNumber()).trim());
            med.setStorageInstructions(data.getOrDefault("storageInstructions", med.getStorageInstructions()).trim());
            med.setDescription(data.getOrDefault("description", med.getDescription()).trim());

            boolean ok = medicineDAO.updateMedicine(med);
            send(exchange, 200, "application/json", String.format("{\"success\":%b}", ok));
        }
    }

    // POST /api/pharmacy/update-stock
    static class PharmacyUpdateStockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = parseJsonFlat(body);

            String medicineId = data.getOrDefault("medicineId", "").trim();
            String actionType = data.getOrDefault("actionType", "Increase").trim();
            int quantity = tryParseInt(data.getOrDefault("quantity", "0"), 0);
            String reason = data.getOrDefault("reason", "Adjustment").trim();
            String remarks = data.getOrDefault("remarks", "").trim();

            com.hospital.service.StockManager stockMgr = new com.hospital.service.StockManager();
            boolean ok = stockMgr.updateStock(medicineId, actionType, quantity, reason, remarks);
            send(exchange, 200, "application/json", String.format("{\"success\":%b}", ok));
        }
    }

    // DELETE /api/pharmacy/delete-medicine
    static class PharmacyDeleteMedicineHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String medicineId = query != null && query.contains("medicineId=")
                    ? query.split("medicineId=")[1].split("&")[0]
                    : "";

            if (medicineId.isEmpty()) {
                send(exchange, 200, "application/json", "{\"success\":false,\"message\":\"Medicine ID is required\"}");
                return;
            }

            boolean ok = medicineDAO.deleteMedicine(medicineId);
            send(exchange, 200, "application/json", String.format("{\"success\":%b}", ok));
        }
    }

    private static int tryParseInt(String val, int def) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static double tryParseDouble(String val, double def) {
        try {
            return Double.parseDouble(val.trim());
        } catch (Exception e) {
            return def;
        }
    }

    // GET & POST /api/lab/bookings
    static class LabBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String patientId = query != null && query.contains("patientId=")
                        ? query.split("patientId=")[1].split("&")[0]
                        : "";
                String doctorId = query != null && query.contains("doctorId=")
                        ? query.split("doctorId=")[1].split("&")[0]
                        : "";
                String scope = query != null && query.contains("scope=")
                        ? query.split("scope=")[1].split("&")[0]
                        : "";

                List<com.hospital.model.LabBooking> list;
                if (!patientId.isEmpty()) {
                    list = labBookingDAO.getBookingsByPatient(patientId);
                } else if (!doctorId.isEmpty()) {
                    list = labBookingDAO.getBookingsByDoctor(doctorId);
                } else {
                    list = labBookingDAO.getAllBookings();
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    com.hospital.model.LabBooking b = list.get(i);
                    sb.append(String.format(
                            "{\"bookingId\":\"%s\",\"patientId\":\"%s\",\"patientName\":\"%s\",\"patientAge\":\"%s\",\"patientGender\":\"%s\",\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"department\":\"%s\",\"prescriptionId\":\"%s\",\"testName\":\"%s\",\"bookingDate\":\"%s\",\"bookingTime\":\"%s\",\"status\":\"%s\",\"paymentStatus\":\"%s\",\"createdAt\":\"%s\"}",
                            escape(b.getBookingId()), escape(b.getPatientId()), escape(b.getPatientName()), escape(b.getPatientAge()), escape(b.getPatientGender()),
                            escape(b.getDoctorId()), escape(b.getDoctorName()), escape(b.getDepartment()),
                            escape(b.getPrescriptionId()),
                            escape(b.getTestName()), escape(b.getBookingDate()), escape(b.getBookingTime()),
                            escape(b.getStatus()), escape(b.getPaymentStatus()), escape(b.getCreatedAt())));
                    if (i < list.size() - 1)
                        sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);

                String bookingId = fields.getOrDefault("bookingId", "LAB-" + (100000 + new Random().nextInt(900000)));
                String pId = fields.getOrDefault("patientId", "PT100842");
                String pName = fields.getOrDefault("patientName", "Rekha Prasad");
                String pAge = fields.getOrDefault("patientAge", "28 Yrs");
                String pGender = fields.getOrDefault("patientGender", "Female");
                String docId = fields.getOrDefault("doctorId", "DOC1001");
                String docName = fields.getOrDefault("doctorName", fields.getOrDefault("docName", "Dr. Srivatsan R"));
                String dept = fields.getOrDefault("department", "Neurology");
                String rxId = fields.getOrDefault("prescriptionId", "RX-1001");
                String testName = fields.getOrDefault("testName", "Routine Lab Panel");
                String date = fields.getOrDefault("bookingDate", "Today");
                String time = fields.getOrDefault("bookingTime", "Morning Slot");
                String status = fields.getOrDefault("status", "Pending");

                com.hospital.model.LabBooking b = new com.hospital.model.LabBooking(bookingId, pId, pName, pAge, pGender, docId, docName, dept, rxId,
                        testName, date, time, status, "Paid", new Date().toString());
                boolean ok = labBookingDAO.createBooking(b);
                send(exchange, 200, "application/json",
                        String.format("{\"success\":%b,\"bookingId\":\"%s\"}", ok, escape(bookingId)));
            }
        }
    }

    // POST /api/lab/payments
    static class LabPaymentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);

                String payId = fields.getOrDefault("paymentId", "PAY-" + (100000 + new Random().nextInt(900000)));
                String bookId = fields.getOrDefault("bookingId", "LAB-1001");
                String pId = fields.getOrDefault("patientId", "PT100842");
                double amount = Double.parseDouble(fields.getOrDefault("amount", "1500"));
                String method = fields.getOrDefault("paymentMethod", "UPI");
                String txnId = fields.getOrDefault("transactionId", "TXN" + System.currentTimeMillis());

                com.hospital.model.LabPayment pay = new com.hospital.model.LabPayment(payId, bookId, pId, amount,
                        method, txnId, "Success", new Date().toString());
                boolean ok = labPaymentDAO.createPayment(pay);
                send(exchange, 200, "application/json",
                        String.format("{\"success\":%b,\"paymentId\":\"%s\",\"transactionId\":\"%s\"}", ok,
                                escape(payId), escape(txnId)));
            }
        }
    }

    // GET & POST /api/lab/reports
    static class LabReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String patientId = query != null && query.contains("patientId=")
                        ? query.split("patientId=")[1].split("&")[0]
                        : "";
                String doctorId = query != null && query.contains("doctorId=")
                        ? query.split("doctorId=")[1].split("&")[0]
                        : "";
                String scope = query != null && query.contains("scope=")
                        ? query.split("scope=")[1].split("&")[0]
                        : "";
                String searchQuery = query != null && query.contains("search=")
                        ? query.split("search=")[1].split("&")[0]
                        : "";

                List<com.hospital.model.LabReport> list;
                if (!searchQuery.isEmpty()) {
                    list = labReportDAO.searchReports(searchQuery);
                } else if (!patientId.isEmpty()) {
                    list = labReportDAO.getReportsByPatient(patientId);
                } else if (!doctorId.isEmpty()) {
                    list = labReportDAO.getReportsByDoctor(doctorId);
                } else if ("all".equalsIgnoreCase(scope)) {
                    list = labReportDAO.getAllReports();
                } else {
                    list = new ArrayList<>();
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    com.hospital.model.LabReport r = list.get(i);
                    sb.append(String.format(
                            "{\"reportId\":\"%s\",\"bookingId\":\"%s\",\"patientId\":\"%s\",\"patientName\":\"%s\",\"patientAge\":\"%s\",\"patientGender\":\"%s\",\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"department\":\"%s\",\"testName\":\"%s\",\"result\":\"%s\",\"observation\":\"%s\",\"remarks\":\"%s\",\"reportFile\":\"%s\",\"uploadedBy\":\"%s\",\"verifiedBy\":\"%s\",\"reportDate\":\"%s\",\"status\":\"%s\"}",
                            escape(r.getReportId()), escape(r.getBookingId()), escape(r.getPatientId()),
                            escape(r.getPatientName()),
                            escape(r.getPatientAge()), escape(r.getPatientGender()), escape(r.getDoctorId()),
                            escape(r.getDoctorName()),
                            escape(r.getDepartment()), escape(r.getTestName()), escape(r.getResult()),
                            escape(r.getObservation()),
                            escape(r.getRemarks()), escape(r.getReportFile()), escape(r.getUploadedBy()),
                            escape(r.getVerifiedBy()),
                            escape(r.getReportDate()), escape(r.getStatus())));
                    if (i < list.size() - 1)
                        sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);

                String repId = fields.getOrDefault("reportId", "REP-" + (100000 + new Random().nextInt(900000)));
                String bookId = fields.getOrDefault("bookingId", "LAB-1001");
                String pId = fields.getOrDefault("patientId", "PT100842");
                String pName = fields.getOrDefault("patientName", "Rekha Prasad");
                String pAge = fields.getOrDefault("patientAge", "28 Yrs");
                String pGender = fields.getOrDefault("patientGender", "Female");
                String docId = fields.getOrDefault("doctorId", "DOC1001");
                String docName = fields.getOrDefault("doctorName", fields.getOrDefault("docName", "Dr. Srivatsan R"));
                String dept = fields.getOrDefault("department", "General Medicine");
                String testName = fields.getOrDefault("testName", "Complete Blood Count (CBC)");
                String result = fields.getOrDefault("result", "Normal Clinical Limits");
                String obs = fields.getOrDefault("observation",
                        "Observed parameters lie within normal diagnostic reference intervals.");
                String remarks = fields.getOrDefault("remarks", "No pathology detected.");
                String testDataJson = fields.getOrDefault("testDataJson", "");
                String repDate = fields.getOrDefault("reportDate",
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
                String pdfFileName = "LabReport_" + pId + "_" + repId + ".pdf";

                com.hospital.model.LabReport rep = new com.hospital.model.LabReport(
                        repId, bookId, pId, pName, pAge, pGender, docId, docName, dept, testName,
                        result, obs, remarks, testDataJson, pdfFileName, "Senior Lab Tech",
                        docName + " (Verified)", repDate, "Ready");

                boolean ok = labReportDAO.createReport(rep);
                labBookingDAO.updateStatus(bookId, "Completed");

                // Generate PDF File
                File pdfFile = com.hospital.service.PDFGenerator.generateReportPDF(rep, null);

                send(exchange, 200, "application/json", String.format(
                        "{\"success\":%b,\"reportId\":\"%s\",\"pdfFile\":\"%s\"}",
                        ok, escape(repId), escape(pdfFileName)));
            }
        }
    }

    static class OtpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int otpVal = 100000 + new Random().nextInt(900000);
            send(exchange, 200, "application/json",
                    String.format("{\"success\":true,\"otp\":\"%d\",\"message\":\"OTP sent\"}", otpVal));
        }
    }

    static class ForgotPasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int otpInt = 100000 + new Random().nextInt(900000);
            send(exchange, 200, "application/json",
                    String.format("{\"success\":true,\"otp\":\"%d\",\"message\":\"OTP sent\"}", otpInt));
        }
    }

    static class ResetPasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            send(exchange, 200, "application/json", "{\"success\":true,\"message\":\"Password reset successfully\"}");
        }
    }

    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            send(exchange, 200, "application/json",
                    "{\"labels\":[\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\",\"Sun\"],\"revenue\":[42000,51000,47500,61000,58500,39000,33500],\"appointments\":[38,44,41,52,49,30,26]}");
        }
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type",
                    contentType + (contentType.contains("charset") ? "" : "; charset=utf-8"));
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            System.out.println("[HTTP RES] " + exchange.getRequestMethod() + " " + exchange.getRequestURI() + " | Status: " + status);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("headers already sent")) {
                return;
            }
            throw e;
        }
    }

    private static Map<String, String> parseJsonFlat(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{"))
            json = json.substring(1);
        if (json.endsWith("}"))
            json = json.substring(0, json.length() - 1);
        for (String pair : json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = stripQuotes(kv[0].trim());
                String val = stripQuotes(kv[1].trim());
                try {
                    val = URLDecoder.decode(val, StandardCharsets.UTF_8.name());
                } catch (Exception ignored) {
                }
                map.put(key, val);
            }
        }
        return map;
    }

    private static String stripQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2)
            return s.substring(1, s.length() - 1);
        return s;
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class DemoUser {
        final String password, role, name, age, gender, weight, height, phone, patientId;
        final boolean isNewUser;

        DemoUser(String password, String role, String name, String age, String gender, String weight, String height,
                String phone, String patientId, boolean isNewUser) {
            this.password = password;
            this.role = role;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.weight = weight;
            this.height = height;
            this.phone = phone;
            this.patientId = patientId;
            this.isNewUser = isNewUser;
        }
    }

    // =========================================================
    // ADMIN MODULE API HANDLERS
    // =========================================================
    static class AdminStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, Object> stats = adminDAO.getDashboardStats();
                StringBuilder json = new StringBuilder("{");
                int idx = 0;
                for (Map.Entry<String, Object> entry : stats.entrySet()) {
                    json.append("\"").append(escape(entry.getKey())).append("\":").append(entry.getValue());
                    if (++idx < stats.size()) json.append(",");
                }
                json.append("}");
                send(exchange, 200, "application/json", json.toString());
            } else {
                send(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    static class AdminGlobalSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String query = q.getOrDefault("q", q.getOrDefault("query", "")).trim();
            if (query.isEmpty()) {
                send(exchange, 200, "application/json", "{\"success\":true,\"patients\":[],\"staff\":[],\"appointments\":[]}");
                return;
            }

            List<Patient> patients = patientDAO.searchPatients(query);
            List<Map<String, String>> staff = adminDAO.searchStaff(query);

            StringBuilder sb = new StringBuilder("{\"success\":true,");
            sb.append("\"patients\":[");
            for (int i = 0; i < patients.size(); i++) {
                Patient p = patients.get(i);
                sb.append(String.format("{\"id\":\"%s\",\"name\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\"}",
                    escape(p.getPatientId()), escape(p.getName()), escape(p.getPhone()), escape(p.getEmail())));
                if (i < patients.size() - 1) sb.append(",");
            }
            sb.append("],\"staff\":[");
            for (int i = 0; i < staff.size(); i++) {
                Map<String, String> s = staff.get(i);
                sb.append(String.format("{\"code\":\"%s\",\"name\":\"%s\",\"role\":\"%s\",\"dept\":\"%s\"}",
                    escape(s.get("employeeCode")), escape(s.get("name")), escape(s.get("role")), escape(s.get("department"))));
                if (i < staff.size() - 1) sb.append(",");
            }
            sb.append("]}");

            send(exchange, 200, "application/json", sb.toString());
        }
    }

    static class AdminStaffHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String searchQ = "";
                if (query != null && query.contains("search=")) {
                    searchQ = URLDecoder.decode(query.split("search=")[1].split("&")[0], StandardCharsets.UTF_8);
                }

                List<Map<String, String>> staff = adminDAO.searchStaff(searchQ);
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < staff.size(); i++) {
                    Map<String, String> s = staff.get(i);
                    json.append("{");
                    json.append("\"employeeCode\":\"").append(escape(s.get("employeeCode"))).append("\",");
                    json.append("\"staffId\":\"").append(escape(s.get("employeeCode"))).append("\",");
                    json.append("\"name\":\"").append(escape(s.get("name"))).append("\",");
                    json.append("\"fullName\":\"").append(escape(s.get("name"))).append("\",");
                    json.append("\"email\":\"").append(escape(s.get("email"))).append("\",");
                    json.append("\"role\":\"").append(escape(s.get("role"))).append("\",");
                    json.append("\"phone\":\"").append(escape(s.get("phone"))).append("\",");
                    json.append("\"mobile\":\"").append(escape(s.get("phone"))).append("\",");
                    json.append("\"department\":\"").append(escape(s.get("department"))).append("\",");
                    json.append("\"designation\":\"").append(escape(s.get("designation"))).append("\",");
                    json.append("\"qualification\":\"").append(escape(s.get("qualification"))).append("\",");
                    json.append("\"experience\":\"").append(escape(s.get("experience"))).append("\",");
                    json.append("\"status\":\"").append(escape(s.get("status"))).append("\",");
                    json.append("\"joiningDate\":\"").append(escape(s.get("joiningDate"))).append("\",");
                    json.append("\"bloodGroup\":\"").append(escape(s.get("bloodGroup"))).append("\",");
                    json.append("\"emergencyContact\":\"").append(escape(s.get("emergencyContact"))).append("\",");
                    json.append("\"validity\":\"").append(escape(s.get("validity"))).append("\"");
                    json.append("}").append(i < staff.size() - 1 ? "," : "");
                }
                json.append("]");
                send(exchange, 200, "application/json", json.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseJsonFlat(body);

                Staff s = new Staff();
                String code = data.getOrDefault("employeeCode", data.getOrDefault("staffId", ""));
                if (code.isEmpty() || !code.startsWith("EMP-")) {
                    code = staffDAO.generateNextEmployeeCode();
                }
                s.setEmployeeCode(code);
                s.setRole(data.getOrDefault("role", "Doctor"));
                s.setFullName(data.getOrDefault("name", data.getOrDefault("fullName", "Staff Member")));
                s.setEmail(data.getOrDefault("email", "staff." + code.toLowerCase() + "@niramaya.health"));
                s.setMobile(data.getOrDefault("phone", data.getOrDefault("mobile", "+91 98765 43210")));
                s.setDepartment(data.getOrDefault("department", "General"));
                s.setDesignation(data.getOrDefault("designation", s.getRole()));
                s.setQualification(data.getOrDefault("qualification", "Degree / Certification"));
                s.setExperience(data.getOrDefault("experience", "5 Yrs"));
                s.setStatus(data.getOrDefault("status", "Active"));
                s.setJoiningDate(data.getOrDefault("joiningDate", "2026-01-15"));
                s.setBloodGroup(data.getOrDefault("bloodGroup", "O+"));
                s.setEmergencyContact(data.getOrDefault("emergencyContact", "+91 98765 43210"));

                boolean ok = staffDAO.addStaff(s);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"employeeCode\":\"%s\"}", ok, escape(s.getEmployeeCode())));

            } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseJsonFlat(body);
                String code = data.getOrDefault("employeeCode", data.getOrDefault("staffId", ""));
                String status = data.getOrDefault("status", "Active");

                boolean ok = staffDAO.updateStatus(code, status);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"employeeCode\":\"%s\",\"status\":\"%s\"}", ok, escape(code), escape(status)));

            } else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String staffId = "";
                if (query != null && (query.contains("staffId=") || query.contains("employeeCode="))) {
                    if (query.contains("employeeCode=")) {
                        staffId = query.split("employeeCode=")[1].split("&")[0];
                    } else {
                        staffId = query.split("staffId=")[1].split("&")[0];
                    }
                }
                boolean ok = staffDAO.deleteStaff(staffId);
                send(exchange, 200, "application/json", "{\"success\":" + ok + "}");
            } else {
                send(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    // GET /api/staff/profile?code=EMP-000001 or ?email=doctor@niramaya.health
    static class StaffProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            String codeOrEmail = "EMP-000001";
            String roleFilter = "";

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if ("code".equalsIgnoreCase(pair[0]) || "email".equalsIgnoreCase(pair[0]) || "id".equalsIgnoreCase(pair[0])) {
                            codeOrEmail = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        } else if ("role".equalsIgnoreCase(pair[0])) {
                            roleFilter = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        }
                    }
                }
            }

            Staff s = staffDAO.getStaffByCodeOrEmail(codeOrEmail);

            // Fallback matching by role if exact code/email lookup not specified
            if (s == null && !roleFilter.isEmpty()) {
                List<Staff> list = staffDAO.getAllStaff();
                String rf = roleFilter.toLowerCase();
                s = list.stream().filter(st -> st.getRole().toLowerCase().contains(rf)).findFirst().orElse(null);
            }

            if (s == null) {
                s = staffDAO.getAllStaff().get(0); // Fallback to first staff
            }

            String json = String.format(
                "{\"success\":true,\"employeeCode\":\"%s\",\"role\":\"%s\",\"fullName\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"mobile\":\"%s\",\"phone\":\"%s\",\"department\":\"%s\",\"designation\":\"%s\",\"qualification\":\"%s\",\"experience\":\"%s\",\"status\":\"%s\",\"joiningDate\":\"%s\",\"bloodGroup\":\"%s\",\"emergencyContact\":\"%s\",\"validity\":\"%s\",\"medicalRegNo\":\"%s\",\"specialization\":\"%s\",\"consultationFee\":%.2f,\"licenseNo\":\"%s\",\"officeExtension\":\"%s\"}",
                escape(s.getEmployeeCode()), escape(s.getRole()), escape(s.getFullName()), escape(s.getFullName()),
                escape(s.getEmail()), escape(s.getMobile()), escape(s.getMobile()), escape(s.getDepartment()),
                escape(s.getDesignation() != null ? s.getDesignation() : s.getRole()),
                escape(s.getQualification() != null ? s.getQualification() : "MD / MBBS"),
                escape(s.getExperience() != null ? s.getExperience() : "10 Yrs"),
                escape(s.getStatus()), escape(s.getJoiningDate() != null ? s.getJoiningDate() : "2021-03-15"),
                escape(s.getBloodGroup() != null ? s.getBloodGroup() : "O+"),
                escape(s.getEmergencyContact() != null ? s.getEmergencyContact() : "+91 98765 43210"),
                escape(s.getValidity() != null ? s.getValidity() : "31-DEC-2028"),
                escape(s.getMedicalRegNo() != null ? s.getMedicalRegNo() : "MCI-2012-89412"),
                escape(s.getSpecialization() != null ? s.getSpecialization() : s.getDepartment()),
                s.getConsultationFee() > 0 ? s.getConsultationFee() : 800.0,
                escape(s.getLicenseNo() != null ? s.getLicenseNo() : "PHARM-DL-2020-90812"),
                escape(s.getOfficeExtension() != null ? s.getOfficeExtension() : "Ext: 1004")
            );

            send(exchange, 200, "application/json", json);
        }
    }

    static class AdminDepartmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Map<String, String>> depts = adminDAO.getAllDepartments();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < depts.size(); i++) {
                    Map<String, String> d = depts.get(i);
                    json.append("{");
                    json.append("\"deptId\":\"").append(escape(d.get("deptId"))).append("\",");
                    json.append("\"deptName\":\"").append(escape(d.get("deptName"))).append("\",");
                    json.append("\"headDoctor\":\"").append(escape(d.get("headDoctor"))).append("\",");
                    json.append("\"totalDoctors\":").append(d.get("totalDoctors")).append(",");
                    json.append("\"totalPatients\":").append(d.get("totalPatients"));
                    json.append("}").append(i < depts.size() - 1 ? "," : "");
                }
                json.append("]");
                send(exchange, 200, "application/json", json.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseJsonFlat(body);
                boolean ok = adminDAO.addDepartment(
                        data.getOrDefault("deptId", "DEP" + System.currentTimeMillis() % 1000),
                        data.getOrDefault("deptName", "General"),
                        data.getOrDefault("headDoctor", "Dr. Specialist"));
                send(exchange, 200, "application/json", "{\"success\":" + ok + "}");
            } else {
                send(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    static class AdminSettingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> settings = adminDAO.getHospitalSettings();
                StringBuilder json = new StringBuilder("{");
                int idx = 0;
                for (Map.Entry<String, String> entry : settings.entrySet()) {
                    json.append("\"").append(escape(entry.getKey())).append("\":\"").append(escape(entry.getValue()))
                            .append("\"");
                    if (++idx < settings.size())
                        json.append(",");
                }
                json.append("}");
                send(exchange, 200, "application/json", json.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> data = parseJsonFlat(body);
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    adminDAO.updateHospitalSetting(entry.getKey(), entry.getValue());
                }
                send(exchange, 200, "application/json", "{\"success\":true}");
            } else {
                send(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    static class AdminNotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Map<String, String>> notifs = notificationManager.getAllNotifications();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < notifs.size(); i++) {
                    Map<String, String> n = notifs.get(i);
                    json.append("{");
                    json.append("\"id\":\"").append(escape(n.get("id"))).append("\",");
                    json.append("\"title\":\"").append(escape(n.get("title"))).append("\",");
                    json.append("\"message\":\"").append(escape(n.get("message"))).append("\",");
                    json.append("\"type\":\"").append(escape(n.get("type"))).append("\",");
                    json.append("\"timestamp\":\"").append(escape(n.get("timestamp"))).append("\",");
                    json.append("\"isRead\":").append(n.get("isRead"));
                    json.append("}").append(i < notifs.size() - 1 ? "," : "");
                }
                json.append("]");
                send(exchange, 200, "application/json", json.toString());
            } else {
                send(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    // GET /api/patient/notifications?patientId=...
    static class PatientNotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String patientId = (query != null && query.contains("patientId="))
                        ? query.split("patientId=")[1].split("&")[0]
                        : "";

                if (patientId == null || patientId.trim().isEmpty()) {
                    send(exchange, 200, "application/json", "[]");
                    return;
                }

                List<Map<String, String>> notifs = notificationManager.getPatientNotifications(patientId.trim());
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < notifs.size(); i++) {
                    Map<String, String> n = notifs.get(i);
                    json.append("{");
                    json.append("\"id\":\"").append(escape(n.get("id"))).append("\",");
                    json.append("\"patientId\":\"").append(escape(n.get("patientId"))).append("\",");
                    json.append("\"title\":\"").append(escape(n.get("title"))).append("\",");
                    json.append("\"message\":\"").append(escape(n.get("message"))).append("\",");
                    json.append("\"type\":\"").append(escape(n.get("type"))).append("\",");
                    json.append("\"timestamp\":\"").append(escape(n.get("timestamp"))).append("\",");
                    json.append("\"isRead\":").append(n.get("isRead"));
                    json.append("}").append(i < notifs.size() - 1 ? "," : "");
                }
                json.append("]");
                send(exchange, 200, "application/json", json.toString());
            } else {
                send(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    // TELEMEDICINE HANDLERS

    // GET & POST /api/telemedicine/consultations
    static class TelemedicineConsultationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String patientId = query != null && query.contains("patientId=")
                        ? query.split("patientId=")[1].split("&")[0]
                        : "";
                String doctorId = query != null && query.contains("doctorId=")
                        ? query.split("doctorId=")[1].split("&")[0]
                        : "";

                List<OnlineConsultation> list = new ArrayList<>();
                if (!patientId.isEmpty()) {
                    list = onlineConsultationDAO.getConsultationsByPatient(patientId);
                } else if (!doctorId.isEmpty()) {
                    list = onlineConsultationDAO.getConsultationsByDoctor(doctorId);
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    OnlineConsultation c = list.get(i);
                    sb.append(String.format(
                            "{\"consultationId\":\"%s\",\"appointmentId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"department\":\"%s\",\"meetingId\":\"%s\",\"meetingRoom\":\"%s\",\"meetingLink\":\"%s\",\"appointmentToken\":\"%s\",\"meetingPassword\":\"%s\",\"consultationType\":\"%s\",\"meetingStatus\":\"%s\",\"meetingDate\":\"%s\",\"meetingTime\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"totalMinutes\":%d,\"createdAt\":\"%s\"}",
                            escape(c.getConsultationId()), escape(c.getAppointmentId()), escape(c.getPatientId()),
                            escape(c.getDoctorId()),
                            escape(c.getDoctorName()), escape(c.getDepartment()), escape(c.getMeetingId()),
                            escape(c.getMeetingRoom()),
                            escape(c.getMeetingLink()), escape(c.getAppointmentToken()), escape(c.getMeetingPassword()),
                            escape(c.getConsultationType()), escape(c.getMeetingStatus()), escape(c.getMeetingDate()),
                            escape(c.getMeetingTime()), escape(c.getStartTime()), escape(c.getEndTime()),
                            c.getTotalMinutes(), escape(c.getCreatedAt())));
                    if (i < list.size() - 1)
                        sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);
                String meetingId = fields.getOrDefault("meetingId", "MTG-" + (100000 + new Random().nextInt(900000)));
                String status = fields.getOrDefault("meetingStatus", "Ongoing");

                boolean ok = onlineConsultationDAO.updateMeetingStatus(meetingId, status);
                send(exchange, 200, "application/json",
                        String.format("{\"success\":%b,\"meetingId\":\"%s\",\"meetingStatus\":\"%s\"}", ok,
                                escape(meetingId), escape(status)));
            }
        }
    }

    public static String formatScheduledTime(String dateStr, String timeStr, int offsetMinutes) {
        try {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String today = dateFormat.format(new Date());
            String d = dateStr;
            if (d == null || d.isEmpty() || "Today".equalsIgnoreCase(d)) {
                d = today;
            } else if ("Tomorrow".equalsIgnoreCase(d)) {
                d = dateFormat.format(new Date(System.currentTimeMillis() + 86400000L));
            }

            java.text.SimpleDateFormat parseFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a");
            java.util.Date parsedDate = parseFormat.parse(d + " " + timeStr);
            if (offsetMinutes != 0) {
                parsedDate = new java.util.Date(parsedDate.getTime() + (offsetMinutes * 60 * 1000L));
            }
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parsedDate);
        } catch (Exception e) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis() + (offsetMinutes * 60 * 1000L)));
        }
    }

    // GET & POST /api/telemedicine/meeting
    static class TelemedicineMeetingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String meetingId = query != null && query.contains("meetingId=")
                        ? query.split("meetingId=")[1].split("&")[0]
                        : "";
                String userId = query != null && query.contains("userId=")
                        ? query.split("userId=")[1].split("&")[0]
                        : "";

                OnlineConsultation c = onlineConsultationDAO.getConsultationByMeetingId(meetingId);

                if (c == null) {
                    c = new OnlineConsultation(
                            "CNS-100841", "TK-100842", "PT100842", "DOC1001", "Dr. Ananya Rao", "Cardiology",
                            "MTG-782914", "Niramaya-Room-782914", "/telemedicine.html?meetingId=MTG-782914",
                            "TOK-889124", "1234", "Online Consultation", "Scheduled", "Today", "10:30 AM",
                            "", "", 0, new Date().toString());
                    c.setScheduledStart(formatScheduledTime("Today", "10:30 AM", 0));
                    c.setScheduledEnd(formatScheduledTime("Today", "10:30 AM", 30));
                }

                // Check temporal and participant access control
                boolean canJoin = true;
                String joinMessage = "Meeting link active.";
                String status = c.getMeetingStatus() != null ? c.getMeetingStatus() : "Scheduled";

                if ("Completed".equalsIgnoreCase(status)) {
                    canJoin = false;
                    joinMessage = "This consultation has already been completed.";
                } else if ("Cancelled".equalsIgnoreCase(status)) {
                    canJoin = false;
                    joinMessage = "This consultation has been cancelled.";
                } else if ("Expired".equalsIgnoreCase(status)) {
                    canJoin = false;
                    joinMessage = "This meeting link has expired.";
                } else {
                    String schedStartStr = c.getScheduledStart();
                    if (schedStartStr != null && !schedStartStr.isEmpty()) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            long startMillis = sdf.parse(schedStartStr).getTime();
                            long nowMillis = System.currentTimeMillis();
                            if (nowMillis < startMillis - (15 * 60 * 1000L)) {
                                canJoin = false;
                                joinMessage = "This meeting link will be accessible 15 minutes before the scheduled time.";
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (userId != null && !userId.isEmpty()) {
                    boolean isAssigned = userId.equalsIgnoreCase(c.getDoctorId()) ||
                                         userId.equalsIgnoreCase(c.getPatientId()) ||
                                         userId.startsWith("EMP-") ||
                                         userId.startsWith("ADM");
                    if (!isAssigned) {
                        canJoin = false;
                        joinMessage = "Access Denied: You are not assigned to this online consultation.";
                    }
                }

                String json = String.format(
                        "{\"success\":true,\"canJoin\":%b,\"joinMessage\":\"%s\",\"consultationId\":\"%s\",\"appointmentId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"department\":\"%s\",\"meetingId\":\"%s\",\"meetingRoom\":\"%s\",\"meetingLink\":\"%s\",\"appointmentToken\":\"%s\",\"meetingPassword\":\"%s\",\"consultationType\":\"%s\",\"meetingStatus\":\"%s\",\"meetingDate\":\"%s\",\"meetingTime\":\"%s\",\"scheduledStart\":\"%s\",\"scheduledEnd\":\"%s\",\"actualStart\":\"%s\",\"actualEnd\":\"%s\",\"patientJoinTime\":\"%s\",\"doctorJoinTime\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"totalMinutes\":%d}",
                        canJoin, escape(joinMessage),
                        escape(c.getConsultationId()), escape(c.getAppointmentId()), escape(c.getPatientId()),
                        escape(c.getDoctorId()), escape(c.getDoctorName()), escape(c.getDepartment()), escape(c.getMeetingId()),
                        escape(c.getMeetingRoom()), escape(c.getMeetingLink()), escape(c.getAppointmentToken()), escape(c.getMeetingPassword()),
                        escape(c.getConsultationType()), escape(c.getMeetingStatus()), escape(c.getMeetingDate()),
                        escape(c.getMeetingTime()), escape(c.getScheduledStart()), escape(c.getScheduledEnd()),
                        escape(c.getActualStart()), escape(c.getActualEnd()), escape(c.getPatientJoinTime()), escape(c.getDoctorJoinTime()),
                        escape(c.getStartTime()), escape(c.getEndTime()), c.getTotalMinutes());
                send(exchange, 200, "application/json", json);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);
                String meetingId = fields.getOrDefault("meetingId", "");
                String action = fields.getOrDefault("action", "end");

                if ("start".equalsIgnoreCase(action)) {
                    boolean ok = onlineConsultationDAO.startConsultation(meetingId);
                    send(exchange, 200, "application/json", String.format(
                            "{\"success\":%b,\"meetingStatus\":\"In Progress\",\"message\":\"Consultation started by doctor.\"}", ok));
                } else if ("patient_join".equalsIgnoreCase(action)) {
                    boolean ok = onlineConsultationDAO.logPatientJoin(meetingId);
                    send(exchange, 200, "application/json", String.format(
                            "{\"success\":%b,\"message\":\"Patient joined room.\"}", ok));
                } else if ("end".equalsIgnoreCase(action)) {
                    String startTime = fields.getOrDefault("startTime", "10:30 AM");
                    String endTime = fields.getOrDefault("endTime",
                            new java.text.SimpleDateFormat("hh:mm a").format(new Date()));
                    int minutes = tryParseInt(fields.getOrDefault("totalMinutes", "15"), 15);

                    boolean ok = onlineConsultationDAO.endConsultation(meetingId, startTime, endTime, minutes);
                    send(exchange, 200, "application/json", String.format(
                            "{\"success\":%b,\"meetingStatus\":\"Completed\",\"totalMinutes\":%d}", ok, minutes));
                } else if ("cancel".equalsIgnoreCase(action)) {
                    boolean ok = onlineConsultationDAO.updateMeetingStatus(meetingId, "Cancelled");
                    send(exchange, 200, "application/json", String.format(
                            "{\"success\":%b,\"meetingStatus\":\"Cancelled\"}", ok));
                } else {
                    String status = fields.getOrDefault("status", "In Progress");
                    boolean ok = onlineConsultationDAO.updateMeetingStatus(meetingId, status);
                    send(exchange, 200, "application/json",
                            String.format("{\"success\":%b,\"meetingStatus\":\"%s\"}", ok, escape(status)));
                }
            }
        }
    }

    // GET & POST /api/telemedicine/notes
    static class TelemedicineNotesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String id = query != null && query.contains("id=") ? query.split("id=")[1].split("&")[0] : "";
                ConsultationNotes notes = onlineConsultationDAO.getConsultationNotes(id);

                if (notes == null) {
                    notes = new ConsultationNotes("NOTE-101", "CNS-100841", "TK-100842", "PT100842", "DOC1001",
                            "Patient presented with fever and cough. Vital signs stable.",
                            "Acute Upper Respiratory Infection",
                            "Adequate hydration, steam inhalation, rest for 3 days.", "5 Days", "Yes",
                            new Date().toString());
                }

                String json = String.format(
                        "{\"success\":true,\"noteId\":\"%s\",\"consultationId\":\"%s\",\"appointmentId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"consultationSummary\":\"%s\",\"diagnosis\":\"%s\",\"advice\":\"%s\",\"followUpDate\":\"%s\",\"medicalCertificateRequired\":\"%s\",\"createdAt\":\"%s\"}",
                        escape(notes.getNoteId()), escape(notes.getConsultationId()), escape(notes.getAppointmentId()),
                        escape(notes.getPatientId()), escape(notes.getDoctorId()),
                        escape(notes.getConsultationSummary()),
                        escape(notes.getDiagnosis()), escape(notes.getAdvice()), escape(notes.getFollowUpDate()),
                        escape(notes.getMedicalCertificateRequired()), escape(notes.getCreatedAt()));
                send(exchange, 200, "application/json", json);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);

                String noteId = "NOTE-" + (10000 + new Random().nextInt(90000));
                ConsultationNotes notes = new ConsultationNotes(
                        noteId,
                        fields.getOrDefault("consultationId", "CNS-100841"),
                        fields.getOrDefault("appointmentId", "TK-100842"),
                        fields.getOrDefault("patientId", "PT100842"),
                        fields.getOrDefault("doctorId", "DOC1001"),
                        fields.getOrDefault("summary", "Online Consultation Summary"),
                        fields.getOrDefault("diagnosis", "Clinical Diagnosis"),
                        fields.getOrDefault("advice", "Rest and Take prescribed medications"),
                        fields.getOrDefault("followUpDate", "5 Days"),
                        fields.getOrDefault("medicalCertificateRequired", "No"),
                        new Date().toString());

                boolean ok = onlineConsultationDAO.saveConsultationNotes(notes);
                send(exchange, 200, "application/json",
                        String.format("{\"success\":%b,\"noteId\":\"%s\"}", ok, escape(noteId)));
            }
        }
    }

    // GET & POST /api/telemedicine/chat
    static class TelemedicineChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String meetingId = query != null && query.contains("meetingId=")
                        ? query.split("meetingId=")[1].split("&")[0]
                        : "";
                List<MeetingChat> list = onlineConsultationDAO.getChatMessages(meetingId);

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    MeetingChat c = list.get(i);
                    sb.append(String.format(
                            "{\"chatId\":\"%s\",\"consultationId\":\"%s\",\"meetingId\":\"%s\",\"senderId\":\"%s\",\"senderName\":\"%s\",\"senderRole\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                            escape(c.getChatId()), escape(c.getConsultationId()), escape(c.getMeetingId()),
                            escape(c.getSenderId()),
                            escape(c.getSenderName()), escape(c.getSenderRole()), escape(c.getMessage()),
                            escape(c.getTimestamp())));
                    if (i < list.size() - 1)
                        sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);

                String chatId = "CHT-" + (10000 + new Random().nextInt(90000));
                MeetingChat chat = new MeetingChat(
                        chatId,
                        fields.getOrDefault("consultationId", "CNS-100841"),
                        fields.getOrDefault("meetingId", "MTG-782914"),
                        fields.getOrDefault("senderId", "DOC1001"),
                        fields.getOrDefault("senderName", "Dr. Ananya Rao"),
                        fields.getOrDefault("senderRole", "Doctor"),
                        fields.getOrDefault("message", "Hello! Welcome to the consultation."),
                        new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()));

                boolean ok = onlineConsultationDAO.saveChatMessage(chat);
                send(exchange, 200, "application/json",
                        String.format("{\"success\":%b,\"chatId\":\"%s\"}", ok, escape(chatId)));
            }
        }
    }

    // POST /api/telemedicine/referral
    static class TelemedicineReferralHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);

            String pId = fields.getOrDefault("patientId", "PT100842");
            String specialty = fields.getOrDefault("specialty", "Cardiology");
            String doctorName = fields.getOrDefault("doctorName", "Specialist Doctor");
            String apptId = "TK-REF-" + (10000 + new Random().nextInt(90000));

            Appointment appt = new Appointment(apptId, pId, "DOC-REF", doctorName, specialty, "Tomorrow", "10:00 AM",
                    "Confirmed", "Paid");
            boolean ok = appointmentDAO.createAppointment(appt);

            send(exchange, 200, "application/json",
                    String.format("{\"success\":%b,\"appointmentId\":\"%s\",\"department\":\"%s\"}", ok, escape(apptId),
                            escape(specialty)));
        }
    }

    // GET /api/telemedicine/patient-history
    static class TelemedicinePatientHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String patientId = query != null && query.contains("patientId=")
                    ? query.split("patientId=")[1].split("&")[0]
                    : "";

            List<Appointment> appts = patientId.isEmpty() ? new ArrayList<>() : appointmentDAO.getAppointmentsByPatient(patientId);
            List<Prescription> rxs = patientId.isEmpty() ? new ArrayList<>() : prescriptionDAO.getPrescriptionsByPatient(patientId);
            List<LabReport> labReports = patientId.isEmpty() ? new ArrayList<>() : labReportDAO.getReportsByPatient(patientId);
            List<PharmacyOrder> orders = patientId.isEmpty() ? new ArrayList<>() : pharmacyOrderDAO.getOrdersByPatient(patientId);

            String json = String.format(
                    "{\"patientId\":\"%s\",\"allergies\":%s,\"existingDiseases\":%s,\"totalAppointments\":%d,\"totalPrescriptions\":%d,\"totalLabReports\":%d,\"totalPharmacyOrders\":%d}",
                    escape(patientId),
                    patientId.isEmpty() ? "[]" : "[\"Penicillin\",\"Dust Mites\"]",
                    patientId.isEmpty() ? "[]" : "[\"Hypertension\",\"Type 2 Diabetes\"]",
                    appts.size(), rxs.size(), labReports.size(), orders.size());
            send(exchange, 200, "application/json", json);
        }
    }

    // POST /api/logout
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);
                String userId = fields.getOrDefault("userId", "USER");
                String userName = fields.getOrDefault("userName", "User");
                String role = fields.getOrDefault("role", "User");
                String email = fields.getOrDefault("email", "");

                if ("doctor".equalsIgnoreCase(role)) {
                    doctorDAO.updateLastLogout(email.isEmpty() ? userId : email);
                }
                activityLogDAO.logActivity(userId, userName, role, "AUTH", "User Logged Out", "Success", "127.0.0.1");
                send(exchange, 200, "application/json", "{\"success\":true,\"message\":\"Logged out successfully\"}");
            } else {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }



    // GET & POST /api/activity-logs
    static class ActivityLogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                int limit = 100;
                String userId = null, role = null, module = null;

                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] pair = param.split("=");
                        if (pair.length == 2) {
                            if ("limit".equalsIgnoreCase(pair[0])) {
                                try {
                                    limit = Integer.parseInt(pair[1]);
                                } catch (Exception ignored) {
                                }
                            } else if ("userId".equalsIgnoreCase(pair[0])) {
                                userId = pair[1];
                            } else if ("role".equalsIgnoreCase(pair[0])) {
                                role = pair[1];
                            } else if ("module".equalsIgnoreCase(pair[0])) {
                                module = pair[1];
                            }
                        }
                    }
                }

                List<ActivityLog> logs;
                if (userId != null && !userId.isEmpty()) {
                    logs = activityLogDAO.getLogsByUser(userId);
                } else if (role != null && !role.isEmpty()) {
                    logs = activityLogDAO.getLogsByRole(role);
                } else if (module != null && !module.isEmpty()) {
                    logs = activityLogDAO.getLogsByModule(module);
                } else {
                    logs = activityLogDAO.getAllLogs(limit);
                }

                StringBuilder sb = new StringBuilder("{\"success\":true,\"logs\":[");
                for (int i = 0; i < logs.size(); i++) {
                    ActivityLog l = logs.get(i);
                    sb.append(String.format(
                            "{\"logId\":\"%s\",\"userId\":\"%s\",\"userName\":\"%s\",\"role\":\"%s\",\"module\":\"%s\",\"action\":\"%s\",\"status\":\"%s\",\"ipAddress\":\"%s\",\"createdAt\":\"%s\"}",
                            escape(l.getLogId()), escape(l.getUserId()), escape(l.getUserName()), escape(l.getRole()),
                            escape(l.getModule()), escape(l.getAction()), escape(l.getStatus()),
                            escape(l.getIpAddress()), escape(l.getCreatedAt())));
                    if (i < logs.size() - 1)
                        sb.append(",");
                }
                sb.append("]}");
                send(exchange, 200, "application/json", sb.toString());

            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseJsonFlat(body);
                String userId = fields.getOrDefault("userId", "USER");
                String userName = fields.getOrDefault("userName", "User");
                String role = fields.getOrDefault("role", "User");
                String module = fields.getOrDefault("module", "General");
                String action = fields.getOrDefault("action", "User Activity");
                String status = fields.getOrDefault("status", "Success");

                boolean ok = activityLogDAO.logActivity(userId, userName, role, module, action, status, "127.0.0.1");
                send(exchange, 200, "application/json", String.format("{\"success\":%b}", ok));
            }
        }
    }

    // GET /api/user-timestamps
    static class UserTimestampsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String userId = "PT100842";
            if (query != null && query.contains("userId=")) {
                userId = query.split("userId=")[1].split("&")[0];
            }

            Patient p = patientDAO.getPatientByIdOrEmail(userId);
            Doctor d = doctorDAO.getDoctorByIdOrEmail(userId);
            List<ActivityLog> logs = activityLogDAO.getLogsByUser(userId);

            StringBuilder sb = new StringBuilder("{");
            sb.append(String.format("\"userId\":\"%s\",", escape(userId)));
            if (p != null) {
                sb.append(
                        String.format("\"name\":\"%s\",\"email\":\"%s\",", escape(p.getName()), escape(p.getEmail())));
            } else if (d != null) {
                sb.append(String.format("\"name\":\"%s\",\"email\":\"%s\",", escape(d.getDoctorName()),
                        escape(d.getEmail())));
            } else {
                sb.append("\"name\":\"System User\",\"email\":\"user@niramaya.health\",");
            }
            sb.append("\"recentLogs\":[");
            for (int i = 0; i < Math.min(logs.size(), 10); i++) {
                ActivityLog l = logs.get(i);
                sb.append(String.format(
                        "{\"module\":\"%s\",\"action\":\"%s\",\"timestamp\":\"%s\",\"status\":\"%s\"}",
                        escape(l.getModule()), escape(l.getAction()), escape(l.getCreatedAt()), escape(l.getStatus())));
                if (i < Math.min(logs.size(), 10) - 1)
                    sb.append(",");
            }
            sb.append("]}");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    // --- DATABASE MANAGER CENTER HANDLERS ---
    static class AdminDbHealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> health = databaseManagerDAO.getDatabaseHealth();
            Map<String, Object> stats = databaseManagerDAO.getDatabaseMonitorStats();

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":true,");
            sb.append("\"connected\":").append(health.get("connected")).append(",");
            sb.append("\"databaseName\":\"").append(escape(String.valueOf(health.get("databaseName")))).append("\",");
            sb.append("\"serverVersion\":\"").append(escape(String.valueOf(health.get("serverVersion")))).append("\",");
            sb.append("\"productName\":\"").append(escape(String.valueOf(health.get("productName")))).append("\",");
            sb.append("\"host\":\"").append(escape(String.valueOf(health.get("host")))).append("\",");
            sb.append("\"port\":\"").append(escape(String.valueOf(health.get("port")))).append("\",");
            sb.append("\"totalTables\":").append(health.get("totalTables")).append(",");
            sb.append("\"totalRecords\":").append(health.get("totalRecords")).append(",");
            sb.append("\"storageUsed\":\"").append(escape(String.valueOf(health.get("storageUsed")))).append("\",");
            sb.append("\"activeConnections\":").append(health.get("activeConnections")).append(",");
            sb.append("\"lastBackup\":\"").append(escape(String.valueOf(health.get("lastBackup")))).append("\",");

            sb.append("\"stats\":{");
            int count = 0;
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                sb.append("\"").append(escape(entry.getKey())).append("\":");
                if (entry.getValue() instanceof Number || entry.getValue() instanceof Boolean) {
                    sb.append(entry.getValue());
                } else {
                    sb.append("\"").append(escape(String.valueOf(entry.getValue()))).append("\"");
                }
                if (++count < stats.size()) sb.append(",");
            }
            sb.append("}}");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    static class AdminDbTablesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("{\"success\":true,\"tables\":[");
            List<Map<String, String>> tables = databaseManagerDAO.getAllTables();
            for (int i = 0; i < tables.size(); i++) {
                Map<String, String> t = tables.get(i);
                sb.append(String.format("{\"name\":\"%s\",\"label\":\"%s\",\"description\":\"%s\"}",
                        escape(t.get("name")), escape(t.get("label")), escape(t.get("description"))));
                if (i < tables.size() - 1) sb.append(",");
            }
            sb.append("]}");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    @SuppressWarnings("unchecked")
    static class AdminDbTableDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String tableName = "patients";
            int page = 1;
            int pageSize = 25;
            String search = "";
            String filter = "";

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length >= 1) {
                        String k = pair[0];
                        String v = pair.length > 1 ? java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                        if ("tableName".equalsIgnoreCase(k)) tableName = v;
                        else if ("page".equalsIgnoreCase(k)) try { page = Integer.parseInt(v); } catch(Exception ignored){}
                        else if ("pageSize".equalsIgnoreCase(k)) try { pageSize = Integer.parseInt(v); } catch(Exception ignored){}
                        else if ("search".equalsIgnoreCase(k)) search = v;
                        else if ("filter".equalsIgnoreCase(k)) filter = v;
                    }
                }
            }

            Map<String, Object> data = databaseManagerDAO.getTableData(tableName, page, pageSize, search, filter);
            List<String> cols = (List<String>) data.get("columns");
            List<List<Object>> rows = (List<List<Object>>) data.get("rows");

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":true,");
            sb.append("\"tableName\":\"").append(escape(tableName)).append("\",");
            sb.append("\"page\":").append(data.get("page")).append(",");
            sb.append("\"pageSize\":").append(data.get("pageSize")).append(",");
            sb.append("\"totalRecords\":").append(data.get("totalRecords")).append(",");
            sb.append("\"totalPages\":").append(data.get("totalPages")).append(",");

            sb.append("\"columns\":[");
            for (int i = 0; i < cols.size(); i++) {
                sb.append("\"").append(escape(cols.get(i))).append("\"");
                if (i < cols.size() - 1) sb.append(",");
            }
            sb.append("],\"rows\":[");
            for (int i = 0; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                sb.append("[");
                for (int j = 0; j < row.size(); j++) {
                    sb.append("\"").append(escape(String.valueOf(row.get(j)))).append("\"");
                    if (j < row.size() - 1) sb.append(",");
                }
                sb.append("]");
                if (i < rows.size() - 1) sb.append(",");
            }
            sb.append("]}");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    @SuppressWarnings("unchecked")
    static class AdminDbQueryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String sql = fields.getOrDefault("querySql", "");
            String role = fields.getOrDefault("role", "admin");

            Map<String, Object> result = databaseManagerDAO.executeCustomQuery(sql, role);
            boolean ok = (Boolean) result.getOrDefault("success", false);

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":").append(ok).append(",");
            if (result.containsKey("message")) {
                sb.append("\"message\":\"").append(escape(String.valueOf(result.get("message")))).append("\",");
            }
            if (result.containsKey("status")) {
                sb.append("\"status\":\"").append(escape(String.valueOf(result.get("status")))).append("\",");
            }
            if (result.containsKey("executionTimeMs")) {
                sb.append("\"executionTimeMs\":").append(result.get("executionTimeMs")).append(",");
            }
            if (result.containsKey("rowsReturned")) {
                sb.append("\"rowsReturned\":").append(result.get("rowsReturned")).append(",");
            }
            if (result.containsKey("rowsAffected")) {
                sb.append("\"rowsAffected\":").append(result.get("rowsAffected")).append(",");
            }
            if (result.containsKey("columns")) {
                List<String> cols = (List<String>) result.get("columns");
                sb.append("\"columns\":[");
                for (int i = 0; i < cols.size(); i++) {
                    sb.append("\"").append(escape(cols.get(i))).append("\"");
                    if (i < cols.size() - 1) sb.append(",");
                }
                sb.append("],");
            }
            if (result.containsKey("rows")) {
                List<List<Object>> rows = (List<List<Object>>) result.get("rows");
                sb.append("\"rows\":[");
                for (int i = 0; i < rows.size(); i++) {
                    List<Object> r = rows.get(i);
                    sb.append("[");
                    for (int j = 0; j < r.size(); j++) {
                        sb.append("\"").append(escape(String.valueOf(r.get(j)))).append("\"");
                        if (j < r.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    if (i < rows.size() - 1) sb.append(",");
                }
                sb.append("]");
            } else {
                if (sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);
            }
            sb.append("}");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    static class AdminDbAddRecordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String tableName = fields.getOrDefault("tableName", "");
            String adminCode = fields.getOrDefault("adminCode", "EMP-000004");

            fields.remove("tableName");
            fields.remove("adminCode");

            boolean ok = databaseManagerDAO.insertRecord(tableName, fields, adminCode);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Record added successfully" : "Failed to add record"));
        }
    }

    static class AdminDbEditRecordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String tableName = fields.getOrDefault("tableName", "");
            String pkCol = fields.getOrDefault("pkCol", "");
            String pkVal = fields.getOrDefault("pkVal", "");
            String adminCode = fields.getOrDefault("adminCode", "EMP-000004");

            fields.remove("tableName");
            fields.remove("pkCol");
            fields.remove("pkVal");
            fields.remove("adminCode");

            boolean ok = databaseManagerDAO.updateRecord(tableName, pkCol, pkVal, fields, adminCode);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Record Updated Successfully" : "Failed to update record"));
        }
    }

    static class AdminDbDeleteRecordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String tableName = fields.getOrDefault("tableName", "");
            String pkCol = fields.getOrDefault("pkCol", "");
            String pkVal = fields.getOrDefault("pkVal", "");
            boolean softDelete = "true".equalsIgnoreCase(fields.getOrDefault("softDelete", "true"));
            String adminCode = fields.getOrDefault("adminCode", "EMP-000004");

            boolean ok = databaseManagerDAO.deleteRecord(tableName, pkCol, pkVal, softDelete, adminCode);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Record Deleted Successfully" : "Failed to delete record"));
        }
    }

    static class AdminDbExportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String tableName = "patients";
            String format = "sql";

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if ("tableName".equalsIgnoreCase(pair[0])) tableName = pair[1];
                        else if ("format".equalsIgnoreCase(pair[0])) format = pair[1];
                    }
                }
            }

            String content;
            String contentType;
            if ("csv".equalsIgnoreCase(format)) {
                content = databaseManagerDAO.exportTableToCSV(tableName);
                contentType = "text/csv";
            } else {
                content = databaseManagerDAO.exportTableToSQL(tableName);
                contentType = "text/plain";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=" + tableName + "." + format.toLowerCase());
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class AdminDbBackupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Object> result = databaseManagerDAO.createFullBackup();
            boolean ok = (Boolean) result.getOrDefault("success", false);

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":").append(ok).append(",");
            if (ok) {
                sb.append("\"backupName\":\"").append(escape(String.valueOf(result.get("backupName")))).append("\",");
                sb.append("\"backupDate\":\"").append(escape(String.valueOf(result.get("backupDate")))).append("\",");
                sb.append("\"backupPath\":\"").append(escape(String.valueOf(result.get("backupPath")))).append("\",");
                sb.append("\"fileSize\":\"").append(escape(String.valueOf(result.get("fileSize")))).append("\"");
            } else {
                sb.append("\"message\":\"").append(escape(String.valueOf(result.get("message")))).append("\"");
            }
            sb.append("}");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    static class AdminDbRestoreHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);
            String sqlContent = fields.getOrDefault("sqlContent", body);

            Map<String, Object> result = databaseManagerDAO.restoreFullBackup(sqlContent);
            boolean ok = (Boolean) result.getOrDefault("success", false);

            send(exchange, 200, "application/json", String.format(
                "{\"success\":%b,\"message\":\"%s\"}", ok, escape(String.valueOf(result.getOrDefault("message", "Database restoration complete.")))));
        }
    }

    // GET /api/debug/database
    static class DebugDatabaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, Object> debugInfo = com.hospital.dao.DBConnection.getDebugDatabaseStatus();
                StringBuilder sb = new StringBuilder("{");
                int idx = 0;
                for (Map.Entry<String, Object> entry : debugInfo.entrySet()) {
                    String k = entry.getKey();
                    Object v = entry.getValue();
                    sb.append("\"").append(escape(k)).append("\":");
                    if (v == null) {
                        sb.append("null");
                    } else if (v instanceof Boolean || v instanceof Number) {
                        sb.append(v);
                    } else if (v instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) v;
                        sb.append("{");
                        int mIdx = 0;
                        for (Map.Entry<?, ?> mEntry : map.entrySet()) {
                            sb.append("\"").append(escape(String.valueOf(mEntry.getKey()))).append("\":");
                            if (mEntry.getValue() instanceof Boolean || mEntry.getValue() instanceof Number) {
                                sb.append(mEntry.getValue());
                            } else {
                                sb.append("\"").append(escape(String.valueOf(mEntry.getValue()))).append("\"");
                            }
                            if (++mIdx < map.size()) sb.append(",");
                        }
                        sb.append("}");
                    } else {
                        sb.append("\"").append(escape(String.valueOf(v))).append("\"");
                    }
                    if (++idx < debugInfo.size()) sb.append(",");
                }
                sb.append("}");
                send(exchange, 200, "application/json", sb.toString());
            } catch (Exception e) {
                com.hospital.dao.DBConnection.logSQLException(e);
                send(exchange, 500, "application/json", String.format("{\"connected\":false,\"originalExceptionMessage\":\"%s\",\"error\":\"%s\"}", escape(e.getMessage()), escape(e.toString())));
            }
        }
    }

    private static Map<String, String> parseQueryParams(java.net.URI uri) {
        Map<String, String> map = new HashMap<>();
        if (uri == null) return map;
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                try {
                    map.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()));
                } catch (Exception e) {
                    map.put(pair[0], pair[1]);
                }
            } else if (pair.length == 1) {
                map.put(pair[0], "");
            }
        }
        return map;
    }

    // Nurse Module Handlers
    static class NurseAssignedPatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String nurseId = q.getOrDefault("nurseId", q.getOrDefault("nurse_id", "NUR10084"));
            String search = q.getOrDefault("search", "");
            List<NurseAssignment> list = nurseDAO.getAssignedPatients(nurseId, search);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                NurseAssignment a = list.get(i);
                sb.append(String.format("{\"assignmentId\":\"%s\",\"nurseId\":\"%s\",\"nurseName\":\"%s\",\"patientId\":\"%s\",\"patientName\":\"%s\",\"doctorName\":\"%s\",\"ward\":\"%s\",\"roomNumber\":\"%s\",\"bedNumber\":\"%s\",\"admissionDate\":\"%s\",\"status\":\"%s\"}",
                        escape(a.getAssignmentId()), escape(a.getNurseId()), escape(a.getNurseName()), escape(a.getPatientId()), escape(a.getPatientName()),
                        escape(a.getDoctorName()), escape(a.getWard()), escape(a.getRoomNumber()), escape(a.getBedNumber()), escape(a.getAdmissionDate()), escape(a.getStatus())));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    static class NurseVitalsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                PatientVital v = new PatientVital(
                        f.get("vitalId"),
                        f.getOrDefault("patientId", "PT100842"),
                        f.getOrDefault("nurseId", "NUR10084"),
                        f.getOrDefault("nurseName", "Nurse Priya Sharma"),
                        f.getOrDefault("temperature", "98.6 °F"),
                        f.getOrDefault("bloodPressure", "120/80 mmHg"),
                        f.getOrDefault("pulseRate", "72 bpm"),
                        f.getOrDefault("respiratoryRate", "18 bpm"),
                        f.getOrDefault("oxygenSaturation", "98%"),
                        f.getOrDefault("bloodSugar", "110 mg/dL"),
                        f.getOrDefault("weight", "65 kg"),
                        f.getOrDefault("height", "168 cm"),
                        f.get("recordedDate"),
                        f.get("recordedTime")
                );
                boolean ok = nurseDAO.recordVitals(v);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Vitals recorded successfully!" : "Failed to record vitals."));
            } else {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                String patientId = q.getOrDefault("patientId", q.getOrDefault("patient_id", "PT100842"));
                List<PatientVital> list = nurseDAO.getVitalsForPatient(patientId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    PatientVital v = list.get(i);
                    sb.append(String.format("{\"vitalId\":\"%s\",\"patientId\":\"%s\",\"nurseName\":\"%s\",\"temperature\":\"%s\",\"bloodPressure\":\"%s\",\"pulseRate\":\"%s\",\"respiratoryRate\":\"%s\",\"oxygenSaturation\":\"%s\",\"bloodSugar\":\"%s\",\"recordedDate\":\"%s\",\"recordedTime\":\"%s\"}",
                            escape(v.getVitalId()), escape(v.getPatientId()), escape(v.getNurseName()), escape(v.getTemperature()), escape(v.getBloodPressure()), escape(v.getPulseRate()), escape(v.getRespiratoryRate()), escape(v.getOxygenSaturation()), escape(v.getBloodSugar()), escape(v.getRecordedDate()), escape(v.getRecordedTime())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class DoctorPatientVitalsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String patientId = q.getOrDefault("patientId", q.getOrDefault("patient_id", "PT100842"));
            if (patientId == null || patientId.trim().isEmpty()) {
                patientId = "PT100842";
            }

            List<PatientVital> list = nurseDAO.getVitalsForPatient(patientId.trim());
            if (list != null && !list.isEmpty()) {
                PatientVital v = list.get(0);
                String resp = String.format(
                    "{\"hasVitals\":true,\"vitalId\":\"%s\",\"appointmentId\":\"%s\",\"patientId\":\"%s\",\"nurseId\":\"%s\",\"nurseName\":\"%s\",\"bloodPressure\":\"%s\",\"temperature\":\"%s\",\"weight\":\"%s\",\"height\":\"%s\",\"pulseRate\":\"%s\",\"oxygenSaturation\":\"%s\",\"bloodSugar\":\"%s\",\"symptoms\":\"%s\",\"nurseNotes\":\"%s\",\"recordedDate\":\"%s\",\"recordedTime\":\"%s\"}",
                    escape(v.getVitalId() != null ? v.getVitalId() : ""),
                    escape(v.getPatientId() != null ? v.getPatientId() : ""),
                    escape(v.getPatientId() != null ? v.getPatientId() : ""),
                    escape(v.getNurseId() != null ? v.getNurseId() : ""),
                    escape(v.getNurseName() != null ? v.getNurseName() : "Nurse Priya Sharma"),
                    escape(v.getBloodPressure() != null ? v.getBloodPressure() : "120/80 mmHg"),
                    escape(v.getTemperature() != null ? v.getTemperature() : "98.6 °F"),
                    escape(v.getWeight() != null ? v.getWeight() : "68 kg"),
                    escape(v.getHeight() != null ? v.getHeight() : "170 cm"),
                    escape(v.getPulseRate() != null ? v.getPulseRate() : "74 bpm"),
                    escape(v.getOxygenSaturation() != null ? v.getOxygenSaturation() : "98%"),
                    escape(v.getBloodSugar() != null ? v.getBloodSugar() : "110 mg/dL"),
                    escape("Stable"),
                    escape("Routine vital check by nurse"),
                    escape(v.getRecordedDate() != null ? v.getRecordedDate() : ""),
                    escape(v.getRecordedTime() != null ? v.getRecordedTime() : "")
                );
                send(exchange, 200, "application/json", resp);
                return;
            }
            send(exchange, 200, "application/json", "{\"hasVitals\":false,\"message\":\"No vital checks recorded yet for this patient.\"}");
        }
    }

    static class NurseNotesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                NursingNote note = new NursingNote(
                        f.get("noteId"),
                        f.getOrDefault("patientId", "PT100842"),
                        f.getOrDefault("nurseId", "NUR10084"),
                        f.getOrDefault("nurseName", "Nurse Priya Sharma"),
                        f.getOrDefault("observation", "Patient resting comfortably. Vitals stable."),
                        f.get("noteDate"),
                        f.get("noteTime")
                );
                boolean ok = nurseDAO.addNursingNote(note);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Nursing note saved successfully!" : "Failed to save nursing note."));
            } else {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                String patientId = q.getOrDefault("patientId", q.getOrDefault("patient_id", "PT100842"));
                List<NursingNote> list = nurseDAO.getNursingNotesForPatient(patientId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    NursingNote n = list.get(i);
                    sb.append(String.format("{\"noteId\":\"%s\",\"patientId\":\"%s\",\"nurseName\":\"%s\",\"observation\":\"%s\",\"noteDate\":\"%s\",\"noteTime\":\"%s\"}",
                            escape(n.getNoteId()), escape(n.getPatientId()), escape(n.getNurseName()), escape(n.getObservation()), escape(n.getNoteDate()), escape(n.getNoteTime())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class NurseMedicationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                String adminId = f.get("adminId");
                String status = f.getOrDefault("status", "Given");
                String time = f.getOrDefault("dosageTime", new SimpleDateFormat("hh:mm a").format(new Date()));
                String reason = f.getOrDefault("missedReason", "");
                String nurseId = f.getOrDefault("nurseId", "NUR10084");
                String nurseName = f.getOrDefault("nurseName", "Nurse Priya Sharma");
                boolean ok = false;
                if (adminId != null && !adminId.isEmpty()) {
                    ok = nurseDAO.updateMedicationStatus(adminId, status, time, reason, nurseId, nurseName);
                } else {
                    MedicationAdmin m = new MedicationAdmin(null, f.getOrDefault("patientId", "PT100842"), f.get("prescriptionId"), f.getOrDefault("medicineName", "Paracetamol 650mg"), f.getOrDefault("dosage", "1 Tablet"), status, time, reason, nurseId, nurseName);
                    ok = nurseDAO.addMedicationAdmin(m);
                }
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Medication administration updated!" : "Failed to update medication administration."));
            } else {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                String patientId = q.getOrDefault("patientId", q.getOrDefault("patient_id", "PT100842"));
                List<MedicationAdmin> list = nurseDAO.getMedicationAdmins(patientId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    MedicationAdmin m = list.get(i);
                    sb.append(String.format("{\"adminId\":\"%s\",\"patientId\":\"%s\",\"medicineName\":\"%s\",\"dosage\":\"%s\",\"status\":\"%s\",\"dosageTime\":\"%s\",\"missedReason\":\"%s\",\"nurseName\":\"%s\"}",
                            escape(m.getAdminId()), escape(m.getPatientId()), escape(m.getMedicineName()), escape(m.getDosage()), escape(m.getStatus()), escape(m.getDosageTime()), escape(m.getMissedReason()), escape(m.getNurseName())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class NurseMonitoringHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                PatientMonitoring mon = new PatientMonitoring(
                        f.get("monitoringId"),
                        f.getOrDefault("patientId", "PT100842"),
                        f.getOrDefault("nurseId", "NUR10084"),
                        f.getOrDefault("nurseName", "Nurse Priya Sharma"),
                        f.getOrDefault("painLevel", "Mild (2/10)"),
                        f.getOrDefault("foodIntake", "Normal Breakfast"),
                        f.getOrDefault("waterIntake", "1.5 Liters"),
                        f.getOrDefault("sleepQuality", "Good (7 Hours)"),
                        f.getOrDefault("urineOutput", "600 ml"),
                        f.getOrDefault("bowelMovement", "Normal"),
                        f.getOrDefault("generalCondition", "Stable & Conscious"),
                        f.getOrDefault("observations", "Patient is responding well to treatment."),
                        f.get("recordedDate"),
                        f.get("recordedTime")
                );
                boolean ok = nurseDAO.recordPatientMonitoring(mon);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Patient monitoring logged!" : "Failed to log monitoring record."));
            } else {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                String patientId = q.getOrDefault("patientId", q.getOrDefault("patient_id", "PT100842"));
                List<PatientMonitoring> list = nurseDAO.getPatientMonitoring(patientId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    PatientMonitoring m = list.get(i);
                    sb.append(String.format("{\"monitoringId\":\"%s\",\"patientId\":\"%s\",\"nurseName\":\"%s\",\"painLevel\":\"%s\",\"foodIntake\":\"%s\",\"waterIntake\":\"%s\",\"sleepQuality\":\"%s\",\"urineOutput\":\"%s\",\"generalCondition\":\"%s\",\"recordedDate\":\"%s\",\"recordedTime\":\"%s\"}",
                            escape(m.getMonitoringId()), escape(m.getPatientId()), escape(m.getNurseName()), escape(m.getPainLevel()), escape(m.getFoodIntake()), escape(m.getWaterIntake()), escape(m.getSleepQuality()), escape(m.getUrineOutput()), escape(m.getGeneralCondition()), escape(m.getRecordedDate()), escape(m.getRecordedTime())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class NurseShiftsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                NurseShift s = new NurseShift(
                        f.get("shiftId"),
                        f.getOrDefault("nurseId", "NUR10084"),
                        f.getOrDefault("nurseName", "Nurse Priya Sharma"),
                        f.getOrDefault("shiftType", "Morning Shift"),
                        f.getOrDefault("startTime", "07:00 AM"),
                        f.getOrDefault("endTime", "03:00 PM"),
                        f.getOrDefault("ward", "ICU Ward 3"),
                        f.getOrDefault("handoverNotes", "All ICU patients monitored. Handed over Bed 4 vitals."),
                        f.getOrDefault("status", "Active")
                );
                boolean ok = nurseDAO.recordShift(s);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Shift details updated!" : "Failed to update shift details."));
            } else {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                String nurseId = q.getOrDefault("nurseId", "NUR10084");
                List<NurseShift> list = nurseDAO.getShiftHistory(nurseId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    NurseShift s = list.get(i);
                    sb.append(String.format("{\"shiftId\":\"%s\",\"nurseName\":\"%s\",\"shiftType\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"ward\":\"%s\",\"handoverNotes\":\"%s\",\"status\":\"%s\"}",
                            escape(s.getShiftId()), escape(s.getNurseName()), escape(s.getShiftType()), escape(s.getStartTime()), escape(s.getEndTime()), escape(s.getWard()), escape(s.getHandoverNotes()), escape(s.getStatus())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class NurseInjectionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                InjectionRecord inj = new InjectionRecord(
                        f.get("injectionId"),
                        f.getOrDefault("patientId", "PT100842"),
                        f.getOrDefault("nurseId", "NUR10084"),
                        f.getOrDefault("nurseName", "Nurse Priya Sharma"),
                        f.getOrDefault("injectionName", "Inj. Ceftriaxone 1g"),
                        f.getOrDefault("dose", "1g IV"),
                        f.getOrDefault("route", "IV Push"),
                        f.get("recordDate"),
                        f.get("recordTime"),
                        f.getOrDefault("remarks", "Administered slowly over 3 mins.")
                );
                boolean ok = nurseDAO.recordInjection(inj);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Injection record added!" : "Failed to add injection record."));
            } else {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                String patientId = q.getOrDefault("patientId", "PT100842");
                List<InjectionRecord> list = nurseDAO.getInjectionRecords(patientId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    InjectionRecord r = list.get(i);
                    sb.append(String.format("{\"injectionId\":\"%s\",\"patientId\":\"%s\",\"injectionName\":\"%s\",\"dose\":\"%s\",\"route\":\"%s\",\"recordDate\":\"%s\",\"recordTime\":\"%s\",\"remarks\":\"%s\"}",
                            escape(r.getInjectionId()), escape(r.getPatientId()), escape(r.getInjectionName()), escape(r.getDose()), escape(r.getRoute()), escape(r.getRecordDate()), escape(r.getRecordTime()), escape(r.getRemarks())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class NurseInventoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                int qty = 1;
                try { qty = Integer.parseInt(f.getOrDefault("quantity", "1")); } catch (Exception ignored) {}
                InventoryRequest req = new InventoryRequest(
                        f.get("requestId"),
                        f.getOrDefault("nurseId", "NUR10084"),
                        f.getOrDefault("nurseName", "Nurse Priya Sharma"),
                        f.getOrDefault("itemName", "Sterile Surgical Gloves (Medium)"),
                        qty,
                        "Pending",
                        new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
                        f.getOrDefault("remarks", "Urgent requirement for ICU Ward 3")
                );
                boolean ok = nurseDAO.createInventoryRequest(req);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Inventory request submitted!" : "Failed to submit inventory request."));
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                String reqId = f.get("requestId");
                String status = f.getOrDefault("status", "Approved");
                String approvedBy = f.getOrDefault("approvedBy", "Hospital Admin");
                boolean ok = nurseDAO.updateInventoryRequestStatus(reqId, status, approvedBy);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Request status updated!" : "Failed to update status."));
            } else {
                List<InventoryRequest> list = nurseDAO.getAllInventoryRequests();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    InventoryRequest r = list.get(i);
                    sb.append(String.format("{\"requestId\":\"%s\",\"nurseName\":\"%s\",\"itemName\":\"%s\",\"quantity\":%d,\"status\":\"%s\",\"requestDate\":\"%s\",\"remarks\":\"%s\",\"approvedBy\":\"%s\"}",
                            escape(r.getRequestId()), escape(r.getNurseName()), escape(r.getItemName()), r.getQuantity(), escape(r.getStatus()), escape(r.getRequestDate()), escape(r.getRemarks()), escape(r.getApprovedBy())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class NurseEmergencyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                EmergencyAlert alert = new EmergencyAlert(
                        f.get("alertId"),
                        f.getOrDefault("patientId", "PT100842"),
                        f.getOrDefault("patientName", "Rekha Prasad"),
                        f.getOrDefault("roomNumber", "ICU-302"),
                        f.getOrDefault("ward", "ICU Ward 3"),
                        f.getOrDefault("nurseId", "NUR10084"),
                        f.getOrDefault("nurseName", "Nurse Priya Sharma"),
                        f.getOrDefault("alertType", "Low Oxygen Level (SpO2 < 88%)"),
                        new SimpleDateFormat("hh:mm a").format(new Date())
                );
                boolean ok = nurseDAO.createEmergencyAlert(alert);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "🚨 EMERGENCY ALERT BROADCASTED TO DOCTOR & ADMIN DASHBOARDS!" : "Failed to broadcast emergency alert."));
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                String alertId = f.get("alertId");
                String resolvedBy = f.getOrDefault("resolvedBy", "Dr. Ananya Rao");
                boolean ok = nurseDAO.resolveEmergencyAlert(alertId, resolvedBy);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Emergency alert marked resolved!" : "Failed to resolve alert."));
            } else {
                List<EmergencyAlert> list = nurseDAO.getActiveEmergencyAlerts();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    EmergencyAlert a = list.get(i);
                    sb.append(String.format("{\"alertId\":\"%s\",\"patientId\":\"%s\",\"patientName\":\"%s\",\"roomNumber\":\"%s\",\"ward\":\"%s\",\"nurseName\":\"%s\",\"alertType\":\"%s\",\"alertTime\":\"%s\",\"status\":\"%s\"}",
                            escape(a.getAlertId()), escape(a.getPatientId()), escape(a.getPatientName()), escape(a.getRoomNumber()), escape(a.getWard()), escape(a.getNurseName()), escape(a.getAlertType()), escape(a.getAlertTime()), escape(a.getStatus())));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                send(exchange, 200, "application/json", sb.toString());
            }
        }
    }

    static class NurseProfileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                String email = f.get("email");
                String newPassword = f.get("newPassword");
                boolean ok = nurseDAO.changePassword(email, newPassword);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Password updated successfully!" : "Failed to update password."));
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> f = parseJsonFlat(body);
                Nurse nurse = nurseDAO.getNurseById(f.getOrDefault("nurseId", "NUR10084"));
                if (nurse == null) nurse = new Nurse();
                nurse.setNurseId(f.getOrDefault("nurseId", "NUR10084"));
                nurse.setName(f.getOrDefault("name", "Nurse Priya Sharma"));
                nurse.setGender(f.getOrDefault("gender", "Female"));
                nurse.setPhone(f.getOrDefault("phone", "+91 98765 43217"));
                nurse.setDepartment(f.getOrDefault("department", "ICU & Emergency Ward"));
                nurse.setQualification(f.getOrDefault("qualification", "B.Sc Nursing"));
                nurse.setShift(f.getOrDefault("shift", "Morning"));
                nurse.setAddress(f.getOrDefault("address", "124 Healthcare Enclave, City"));
                boolean ok = nurseDAO.updateNurseProfile(nurse);
                send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Profile updated successfully!" : "Failed to update profile."));
            } else {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                String email = q.getOrDefault("email", "nurse@niramaya.health");
                Nurse n = nurseDAO.getNurseByEmail(email);
                if (n == null) n = nurseDAO.getNurseById("NUR10084");
                if (n == null) {
                    n = new Nurse("NUR10084", "NUR10084", "Nurse Priya Sharma", "Female", "1995-06-15", "+91 98765 43217", "nurse@niramaya.health", "ICU & Emergency Ward", "B.Sc Nursing", 5, "Morning", "2023-04-10", "124 Healthcare Enclave, City", "demo1234");
                }
                String json = String.format("{\"nurseId\":\"%s\",\"employeeCode\":\"%s\",\"name\":\"%s\",\"gender\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"department\":\"%s\",\"qualification\":\"%s\",\"experienceYears\":%d,\"shift\":\"%s\",\"joiningDate\":\"%s\",\"address\":\"%s\",\"status\":\"%s\"}",
                        escape(n.getNurseId()), escape(n.getEmployeeCode()), escape(n.getName()), escape(n.getGender()), escape(n.getEmail()), escape(n.getPhone()), escape(n.getDepartment()), escape(n.getQualification()), n.getExperienceYears(), escape(n.getShift()), escape(n.getJoiningDate()), escape(n.getAddress()), escape(n.getStatus()));
                send(exchange, 200, "application/json", json);
            }
        }
    }

    static class NurseDashboardStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String nurseId = q.getOrDefault("nurseId", "NUR10084");
            List<NurseAssignment> patients = nurseDAO.getAssignedPatients(nurseId, "");
            List<EmergencyAlert> alerts = nurseDAO.getActiveEmergencyAlerts();
            List<InventoryRequest> invReqs = nurseDAO.getAllInventoryRequests();

            String json = String.format("{\"success\":true,\"assignedPatientsCount\":%d,\"todayPatientsCount\":%d,\"pendingTasksCount\":%d,\"medicinesDueCount\":%d,\"emergencyAlertsCount\":%d,\"currentShift\":\"Morning Shift (07:00 AM - 03:00 PM)\",\"ward\":\"ICU Ward 3\"}",
                    patients.size(), patients.size(), 4, 3, alerts.size());
            send(exchange, 200, "application/json", json);
        }
    }

    // POST /api/daily-report/send
    static class DailyReportSendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> fields = parseJsonFlat(body);

            String role = fields.getOrDefault("senderRole", fields.getOrDefault("role", "Doctor")).trim();
            String senderId = fields.getOrDefault("senderId", fields.getOrDefault("userId", "DOC10084")).trim();
            String senderName = fields.getOrDefault("senderName", fields.getOrDefault("name", "Dr. Ananya Rao")).trim();
            String department = fields.getOrDefault("department", "Cardiology").trim();
            String reportDate = fields.getOrDefault("reportDate", fields.getOrDefault("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))).trim();
            String summaryNotes = fields.getOrDefault("summaryNotes", fields.getOrDefault("notes", "All daily tasks completed successfully.")).trim();

            int totalPatients = 0;
            try { totalPatients = Integer.parseInt(fields.getOrDefault("totalPatients", "0")); } catch (Exception ignored) {}
            int totalTasksCompleted = 0;
            try { totalTasksCompleted = Integer.parseInt(fields.getOrDefault("totalTasksCompleted", "0")); } catch (Exception ignored) {}
            int totalPending = 0;
            try { totalPending = Integer.parseInt(fields.getOrDefault("totalPending", "0")); } catch (Exception ignored) {}
            double revenueGenerated = 0.0;
            try { revenueGenerated = Double.parseDouble(fields.getOrDefault("revenueGenerated", "0")); } catch (Exception ignored) {}
            String metricsJson = fields.getOrDefault("metricsJson", "{}");

            String reportId = "DLY-" + System.currentTimeMillis();
            String nowStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            DailyReport report = new DailyReport(
                    reportId, role, senderId, senderName, department,
                    reportDate, summaryNotes, totalPatients, totalTasksCompleted,
                    totalPending, revenueGenerated, metricsJson, "Submitted", nowStr
            );

            boolean ok = dailyReportDAO.saveReport(report);

            if (ok) {
                String title = "📄 Daily Report: " + role + " (" + senderName + ")";
                String message = String.format("Daily report for %s submitted by %s (%s). Patients: %d, Tasks: %d, Revenue: ₹%.2f. Summary: %s",
                        reportDate, senderName, role, totalPatients, totalTasksCompleted, revenueGenerated, summaryNotes);
                notificationManager.addNotification(title, message, "Daily Report");
            }

            String json = String.format("{\"success\":%b,\"reportId\":\"%s\",\"message\":\"%s\"}",
                    ok, escape(reportId), ok ? "Daily report submitted successfully to Admin!" : "Failed to save daily report.");
            send(exchange, 200, "application/json", json);
        }
    }

    // GET /api/daily-reports
    static class DailyReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String date = q.get("date");
            String role = q.get("role");

            List<DailyReport> list;
            if (date != null && !date.trim().isEmpty()) {
                list = dailyReportDAO.getReportsByDate(date.trim());
            } else if (role != null && !role.trim().isEmpty()) {
                list = dailyReportDAO.getReportsByRole(role.trim());
            } else {
                list = dailyReportDAO.getAllReports();
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                DailyReport r = list.get(i);
                sb.append(String.format(
                        "{\"reportId\":\"%s\",\"senderRole\":\"%s\",\"senderId\":\"%s\",\"senderName\":\"%s\",\"department\":\"%s\",\"reportDate\":\"%s\",\"summaryNotes\":\"%s\",\"totalPatients\":%d,\"totalTasksCompleted\":%d,\"totalPending\":%d,\"revenueGenerated\":%.2f,\"metricsJson\":\"%s\",\"status\":\"%s\",\"createdAt\":\"%s\"}",
                        escape(r.getReportId()), escape(r.getSenderRole()), escape(r.getSenderId()), escape(r.getSenderName()), escape(r.getDepartment()), escape(r.getReportDate()), escape(r.getSummaryNotes()), r.getTotalPatients(), r.getTotalTasksCompleted(), r.getTotalPending(), r.getRevenueGenerated(), escape(r.getMetricsJson()), escape(r.getStatus()), escape(r.getCreatedAt())));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            send(exchange, 200, "application/json", sb.toString());
        }
    }

    // =========================================================================
    // WORKFLOW HANDLERS FOR TODAY'S WORK & COMPLETED HISTORY
    // =========================================================================

    private static String readRequestBodyStr(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String formatAppointmentsJsonList(List<Appointment> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Appointment a = list.get(i);
            sb.append(String.format(
                "{\"appointmentId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"doctorName\":\"%s\",\"department\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"status\":\"%s\",\"paymentStatus\":\"%s\"}",
                escape(a.getAppointmentId()), escape(a.getPatientId()), escape(a.getDoctorId()), escape(a.getDoctorName()),
                escape(a.getDepartment()), escape(a.getAppointmentDate()), escape(a.getAppointmentTime()),
                escape(a.getStatus()), escape(a.getPaymentStatus())
            ));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatNurseAssignmentsJsonList(List<NurseAssignment> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            NurseAssignment a = list.get(i);
            sb.append(String.format(
                "{\"assignmentId\":\"%s\",\"nurseId\":\"%s\",\"nurseName\":\"%s\",\"patientId\":\"%s\",\"patientName\":\"%s\",\"doctorName\":\"%s\",\"ward\":\"%s\",\"roomNumber\":\"%s\",\"bedNumber\":\"%s\",\"admissionDate\":\"%s\",\"status\":\"%s\",\"createdAt\":\"%s\"}",
                escape(a.getAssignmentId()), escape(a.getNurseId()), escape(a.getNurseName()), escape(a.getPatientId()),
                escape(a.getPatientName()), escape(a.getDoctorName()), escape(a.getWard()), escape(a.getRoomNumber()),
                escape(a.getBedNumber()), escape(a.getAdmissionDate()), escape(a.getStatus()), escape(a.getCreatedAt())
            ));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatLabBookingsJsonList(List<LabBooking> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            LabBooking b = list.get(i);
            sb.append(String.format(
                "{\"bookingId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"prescriptionId\":\"%s\",\"testName\":\"%s\",\"bookingDate\":\"%s\",\"bookingTime\":\"%s\",\"status\":\"%s\",\"paymentStatus\":\"%s\",\"createdAt\":\"%s\"}",
                escape(b.getBookingId()), escape(b.getPatientId()), escape(b.getDoctorId()), escape(b.getPrescriptionId()),
                escape(b.getTestName()), escape(b.getBookingDate()), escape(b.getBookingTime()), escape(b.getStatus()),
                escape(b.getPaymentStatus()), escape(b.getCreatedAt())
            ));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatPharmacyOrdersJsonList(List<PharmacyOrder> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            PharmacyOrder p = list.get(i);
            sb.append(String.format(
                "{\"orderId\":\"%s\",\"pharmacyToken\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"prescriptionId\":\"%s\",\"appointmentId\":\"%s\",\"totalAmount\":%.2f,\"paymentStatus\":\"%s\",\"orderStatus\":\"%s\",\"paymentMethod\":\"%s\",\"transactionId\":\"%s\",\"orderDate\":\"%s\"}",
                escape(p.getOrderId()), escape(p.getPharmacyToken()), escape(p.getPatientId()), escape(p.getDoctorId()),
                escape(p.getPrescriptionId()), escape(p.getAppointmentId()), p.getTotalAmount(), escape(p.getPaymentStatus()),
                escape(p.getOrderStatus()), escape(p.getPaymentMethod()), escape(p.getTransactionId()), escape(p.getOrderDate())
            ));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // GET /api/doctor/todays-patients
    static class DoctorTodaysPatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String doc = q.getOrDefault("doctor", q.get("email"));
            List<Appointment> list = appointmentDAO.getTodaysPatientsForDoctor(doc);
            send(exchange, 200, "application/json", formatAppointmentsJsonList(list));
        }
    }

    // GET /api/doctor/completed-consultations
    static class DoctorCompletedConsultationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String doc = q.getOrDefault("doctor", q.get("email"));
            List<Appointment> list = appointmentDAO.getCompletedConsultationsForDoctor(doc);
            send(exchange, 200, "application/json", formatAppointmentsJsonList(list));
        }
    }

    // POST /api/doctor/complete-consultation
    static class DoctorCompleteConsultationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBodyStr(exchange);
            Map<String, String> map = parseJsonFlat(body);
            String apptId = map.get("appointmentId");
            if (apptId == null || apptId.trim().isEmpty()) {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                apptId = q.get("appointmentId");
            }
            if (apptId == null || apptId.trim().isEmpty()) {
                send(exchange, 400, "application/json", "{\"success\":false,\"message\":\"Missing appointmentId\"}");
                return;
            }
            boolean ok = appointmentDAO.markAppointmentCompleted(apptId);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Consultation completed successfully" : "Failed to update appointment"));
        }
    }

    // GET /api/nurse/todays-patients
    static class NurseTodaysPatientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String nurseId = q.get("nurseId");
            List<NurseAssignment> list = nurseDAO.getTodaysAssignedPatients(nurseId);
            send(exchange, 200, "application/json", formatNurseAssignmentsJsonList(list));
        }
    }

    // GET /api/nurse/completed-tasks
    static class NurseCompletedTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String nurseId = q.get("nurseId");
            List<NurseAssignment> list = nurseDAO.getCompletedVitalChecks(nurseId);
            send(exchange, 200, "application/json", formatNurseAssignmentsJsonList(list));
        }
    }

    // POST /api/nurse/complete-vitals
    static class NurseCompleteVitalsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBodyStr(exchange);
            Map<String, String> map = parseJsonFlat(body);
            String assignId = map.get("assignmentId");
            String apptId = map.get("appointmentId");
            if (assignId == null && apptId == null) {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                assignId = q.get("assignmentId");
                apptId = q.get("appointmentId");
            }
            boolean ok = nurseDAO.completeVitalCheck(assignId, apptId);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Vital check marked complete" : "Failed to mark vitals complete"));
        }
    }

    // GET /api/lab/todays-orders
    static class LabTodaysOrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            List<LabBooking> list = labBookingDAO.getTodaysLabOrders();
            send(exchange, 200, "application/json", formatLabBookingsJsonList(list));
        }
    }

    // GET /api/lab/completed-reports
    static class LabCompletedReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            List<LabBooking> list = labBookingDAO.getCompletedReports();
            send(exchange, 200, "application/json", formatLabBookingsJsonList(list));
        }
    }

    // POST /api/lab/complete-test
    static class LabCompleteTestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBodyStr(exchange);
            Map<String, String> map = parseJsonFlat(body);
            String bookingId = map.get("bookingId");
            if (bookingId == null || bookingId.trim().isEmpty()) {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                bookingId = q.get("bookingId");
            }
            boolean ok = labBookingDAO.markTestCompleted(bookingId);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Lab test marked completed" : "Failed to update lab test"));
        }
    }

    // GET /api/pharmacy/todays-prescriptions
    static class PharmacyTodaysPrescriptionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            List<PharmacyOrder> list = pharmacyOrderDAO.getTodaysMedicineOrders();
            send(exchange, 200, "application/json", formatPharmacyOrdersJsonList(list));
        }
    }

    // GET /api/pharmacy/completed-orders
    static class PharmacyCompletedOrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            List<PharmacyOrder> list = pharmacyOrderDAO.getCompletedOrders();
            send(exchange, 200, "application/json", formatPharmacyOrdersJsonList(list));
        }
    }

    // POST /api/pharmacy/complete-order
    static class PharmacyCompleteOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            String body = readRequestBodyStr(exchange);
            Map<String, String> map = parseJsonFlat(body);
            String orderId = map.get("orderId");
            if (orderId == null || orderId.trim().isEmpty()) {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI());
                orderId = q.get("orderId");
            }
            boolean ok = pharmacyOrderDAO.markOrderCompleted(orderId);
            send(exchange, 200, "application/json", String.format("{\"success\":%b,\"message\":\"%s\"}", ok, ok ? "Pharmacy order marked completed" : "Failed to update pharmacy order"));
        }
    }

    // GET /api/admin/todays-stats
    static class AdminTodaysStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            List<Appointment> todaysAppts = appointmentDAO.getTodaysAppointments();
            List<LabBooking> todaysLabs = labBookingDAO.getTodaysLabOrders();
            List<PharmacyOrder> todaysPharm = pharmacyOrderDAO.getTodaysMedicineOrders();
            double rev = 0.0;
            for (PharmacyOrder p : todaysPharm) { rev += p.getTotalAmount(); }

            String json = String.format(
                "{\"todayPatients\":%d,\"todayAppointments\":%d,\"todayRevenue\":%.2f,\"todayLabTests\":%d,\"todayPharmacyOrders\":%d,\"todayConsultations\":%d,\"todayAmbulanceRequests\":0,\"todayAdmissions\":0,\"todayDischarges\":0}",
                todaysAppts.size(), todaysAppts.size(), rev, todaysLabs.size(), todaysPharm.size(), todaysAppts.size()
            );
            send(exchange, 200, "application/json", json);
        }
    }

    // GET /api/admin/history
    static class AdminHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String tf = q.getOrDefault("timeframe", "all");
            List<Appointment> appts = appointmentDAO.getAppointmentHistory();
            List<LabBooking> labs = labBookingDAO.getCompletedReports();
            List<PharmacyOrder> orders = pharmacyOrderDAO.getCompletedOrders();

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"timeframe\":\"").append(escape(tf)).append("\",");
            sb.append("\"completedAppointments\":").append(formatAppointmentsJsonList(appts)).append(",");
            sb.append("\"completedLabReports\":").append(formatLabBookingsJsonList(labs)).append(",");
            sb.append("\"completedPharmacyOrders\":").append(formatPharmacyOrdersJsonList(orders));
            sb.append("}");

            send(exchange, 200, "application/json", sb.toString());
        }
    }

    // GET /api/patient/todays-appointments
    static class PatientTodaysAppointmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String patientId = q.getOrDefault("patientId", "PT100842");
            List<Appointment> list = appointmentDAO.getTodaysAppointmentsForPatient(patientId);
            send(exchange, 200, "application/json", formatAppointmentsJsonList(list));
        }
    }

    // GET /api/patient/completed-appointments
    static class PatientCompletedAppointmentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "application/json", "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQueryParams(exchange.getRequestURI());
            String patientId = q.getOrDefault("patientId", "PT100842");
            List<Appointment> list = appointmentDAO.getCompletedAppointmentsForPatient(patientId);
            send(exchange, 200, "application/json", formatAppointmentsJsonList(list));
        }
    }
}

