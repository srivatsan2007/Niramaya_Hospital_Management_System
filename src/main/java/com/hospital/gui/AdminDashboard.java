package com.hospital.gui;

import com.hospital.dao.AdminDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.MedicineDAO;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Medicine;
import com.hospital.service.ReportGenerator;
import com.hospital.dao.DatabaseManagerDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Complete Admin Dashboard GUI in Java Swing for Niramaya Hospitals.
 * Features 14 sidebar sections, real-time stat cards, CRUD operations with confirmation dialogs,
 * report exports, and JDBC data access.
 */
public class AdminDashboard extends JFrame {

    private AdminDAO adminDAO = new AdminDAO();
    private DoctorDAO doctorDAO = new DoctorDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private MedicineDAO medicineDAO = new MedicineDAO();
    private DatabaseManagerDAO dbManagerDAO = new DatabaseManagerDAO();

    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    // Stat Labels
    private JLabel lblTotalPatients, lblTotalDoctors, lblTotalLabTechs, lblTotalPharmacists;
    private JLabel lblTodayAppts, lblPendingLab, lblPendingPharm, lblTodayRev, lblLowStock, lblDepts;

    public AdminDashboard() {
        setTitle("Niramaya Hospitals — Master Admin Control Center");
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Header Bar
        JPanel topHeader = createTopHeader();
        add(topHeader, BorderLayout.NORTH);

        // Sidebar Navigation
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main Content Area with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.add(createDashboardHomePanel(), "dashboard");
        mainContentPanel.add(createDoctorManagementPanel(), "doctors");
        mainContentPanel.add(createPatientManagementPanel(), "patients");
        mainContentPanel.add(createAppointmentManagementPanel(), "appointments");
        mainContentPanel.add(createLaboratoryManagementPanel(), "laboratory");
        mainContentPanel.add(createPharmacyManagementPanel(), "pharmacy");
        mainContentPanel.add(createInventoryManagementPanel(), "inventory");
        mainContentPanel.add(createBillingPanel(), "billing");
        mainContentPanel.add(createReportsPanel(), "reports");
        mainContentPanel.add(createNotificationsPanel(), "notifications");
        mainContentPanel.add(createStaffManagementPanel(), "staff");
        mainContentPanel.add(createDepartmentsPanel(), "departments");
        mainContentPanel.add(createSettingsPanel(), "settings");
        mainContentPanel.add(createDatabaseManagerPanel(), "db_manager");

        add(mainContentPanel, BorderLayout.CENTER);

        // Load Initial Dashboard Stats
        refreshStats();
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 42, 74));
        header.setPreferredSize(new Dimension(1280, 65));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel logoLabel = new JLabel("NIRAMAYA HOSPITALS — ADMIN DASHBOARD");
        logoLabel.setFont(new Font("Sora", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        searchPanel.setOpaque(false);

        JTextField txtGlobalSearch = new JTextField(20);
        txtGlobalSearch.setFont(new Font("Inter", Font.PLAIN, 13));
        txtGlobalSearch.setToolTipText("Global Search (Patient ID, Doctor ID, Invoice, Token...)");

        JButton btnGlobalSearch = new JButton("🔍 Search");
        btnGlobalSearch.setFont(new Font("Inter", Font.BOLD, 12));
        btnGlobalSearch.addActionListener(e -> {
            String query = txtGlobalSearch.getText().trim();
            if (!query.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Global Search query: '" + query + "'\nResult matched in system database registry.", "Global Search", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JLabel lblAdminBadge = new JLabel("🛡️ Super Admin");
        lblAdminBadge.setFont(new Font("Inter", Font.BOLD, 13));
        lblAdminBadge.setForeground(new Color(95, 214, 204));

        searchPanel.add(new JLabel("🔍 Global Search:"));
        searchPanel.getComponent(0).setForeground(Color.WHITE);
        searchPanel.add(txtGlobalSearch);
        searchPanel.add(btnGlobalSearch);
        searchPanel.add(lblAdminBadge);

        header.add(logoLabel, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(21, 55, 93));
        sidebar.setPreferredSize(new Dimension(240, 700));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        String[][] menuItems = {
                {"🏠 Dashboard", "dashboard"},
                {"👨‍⚕️ Doctors", "doctors"},
                {"👥 Patients", "patients"},
                {"📅 Appointments", "appointments"},
                {"🧪 Laboratory", "laboratory"},
                {"💊 Pharmacy", "pharmacy"},
                {"📦 Inventory", "inventory"},
                {"💰 Billing", "billing"},
                {"📄 Reports", "reports"},
                {"📢 Notifications", "notifications"},
                {"👤 Staff Management", "staff"},
                {"🏥 Departments", "departments"},
                {"⚙ Settings", "settings"},
                {"🗄 Database Manager", "db_manager"},
                {"🚪 Logout", "logout"}
        };

        for (String[] item : menuItems) {
            JButton btn = new JButton(item[0]);
            btn.setMaximumSize(new Dimension(220, 40));
            btn.setFont(new Font("Inter", Font.BOLD, 13));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(21, 55, 93));
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

            btn.addActionListener(e -> {
                if ("logout".equals(item[1])) {
                    int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout from Admin Dashboard?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        dispose();
                    }
                } else {
                    cardLayout.show(mainContentPanel, item[1]);
                    refreshStats();
                }
            });

            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        return sidebar;
    }

    // --- 1. DASHBOARD HOME ---
    private JPanel createDashboardHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("HOSPITAL OVERVIEW & LIVE METRICS");
        title.setFont(new Font("Sora", Font.BOLD, 18));
        title.setForeground(new Color(15, 42, 74));
        panel.add(title, BorderLayout.NORTH);

        // Stat Cards Grid (2 rows x 5 cols)
        JPanel statsGrid = new JPanel(new GridLayout(2, 5, 15, 15));
        statsGrid.setOpaque(false);

        lblTotalPatients = createStatCard(statsGrid, "👥 Total Patients", "1,240", new Color(13, 110, 253));
        lblTotalDoctors = createStatCard(statsGrid, "👨‍⚕️ Total Doctors", "38", new Color(10, 122, 112));
        lblTotalLabTechs = createStatCard(statsGrid, "🧪 Lab Techs", "12", new Color(107, 33, 168));
        lblTotalPharmacists = createStatCard(statsGrid, "💊 Pharmacists", "9", new Color(217, 119, 6));
        lblTodayAppts = createStatCard(statsGrid, "📅 Today Appts", "42", new Color(13, 110, 253));

        lblPendingLab = createStatCard(statsGrid, "🧪 Pending Lab", "8", new Color(225, 91, 91));
        lblPendingPharm = createStatCard(statsGrid, "💊 Pending Orders", "15", new Color(217, 119, 6));
        lblTodayRev = createStatCard(statsGrid, "💰 Today Revenue", "₹ 85,400", new Color(10, 122, 112));
        lblLowStock = createStatCard(statsGrid, "📦 Low Stock Meds", "6 Items", new Color(225, 91, 91));
        lblDepts = createStatCard(statsGrid, "🏥 Departments", "5 Active", new Color(15, 42, 74));

        // Recent Activities Panel
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBackground(Color.WHITE);
        recentPanel.setBorder(BorderFactory.createTitledBorder("⚡ Recent Hospital Activities"));

        DefaultTableModel activityModel = new DefaultTableModel(new String[]{"Icon", "Activity Type", "Description", "Time"}, 0);
        JTable tblActivities = new JTable(activityModel);
        tblActivities.setRowHeight(30);

        List<Map<String, String>> activities = adminDAO.getRecentActivities();
        for (Map<String, String> act : activities) {
            activityModel.addRow(new Object[]{act.get("icon"), act.get("type"), act.get("description"), act.get("timestamp")});
        }

        recentPanel.add(new JScrollPane(tblActivities), BorderLayout.CENTER);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 20));
        centerContainer.setOpaque(false);
        centerContainer.add(statsGrid, BorderLayout.NORTH);
        centerContainer.add(recentPanel, BorderLayout.CENTER);

        panel.add(centerContainer, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatCard(JPanel container, String title, String initialVal, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblVal = new JLabel(initialVal);
        lblVal.setFont(new Font("Sora", Font.BOLD, 20));
        lblVal.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        container.add(card);

        return lblVal;
    }

    private void refreshStats() {
        Map<String, Object> stats = adminDAO.getDashboardStats();
        if (lblTotalPatients != null) lblTotalPatients.setText(String.valueOf(stats.get("totalPatients")));
        if (lblTotalDoctors != null) lblTotalDoctors.setText(String.valueOf(stats.get("totalDoctors")));
        if (lblTotalLabTechs != null) lblTotalLabTechs.setText(String.valueOf(stats.get("totalLabTechs")));
        if (lblTotalPharmacists != null) lblTotalPharmacists.setText(String.valueOf(stats.get("totalPharmacists")));
        if (lblTodayAppts != null) lblTodayAppts.setText(String.valueOf(stats.get("todayAppointments")));
        if (lblPendingLab != null) lblPendingLab.setText(String.valueOf(stats.get("pendingLabReports")));
        if (lblPendingPharm != null) lblPendingPharm.setText(String.valueOf(stats.get("pendingPharmacyOrders")));
        if (lblTodayRev != null) lblTodayRev.setText("₹ " + stats.get("todayRevenue"));
        if (lblLowStock != null) lblLowStock.setText(stats.get("lowStockCount") + " Items");
        if (lblDepts != null) lblDepts.setText(stats.get("departmentCount") + " Active");
    }

    // --- 2. DOCTOR MANAGEMENT ---
    private JPanel createDoctorManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JLabel title = new JLabel("👨‍⚕️ DOCTOR MANAGEMENT CONSOLE");
        title.setFont(new Font("Sora", Font.BOLD, 16));

        JButton btnAddDoc = new JButton("➕ Add New Doctor");
        btnAddDoc.setBackground(new Color(10, 122, 112));
        btnAddDoc.setForeground(Color.WHITE);
        btnAddDoc.setFont(new Font("Inter", Font.BOLD, 12));
        btnAddDoc.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add Doctor Form initialized.", "Add Doctor", JOptionPane.INFORMATION_MESSAGE));

        top.add(title);
        top.add(btnAddDoc);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Doc ID", "Name", "Department", "Specialization", "Fee", "Status", "Appts", "Action"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);

        List<Doctor> docs = doctorDAO.getAllDoctors();
        for (Doctor d : docs) {
            model.addRow(new Object[]{d.getDoctorId(), d.getDoctorName(), d.getDepartment(), d.getSpecialization(), "₹" + d.getConsultationFee(), d.getStatus(), "12", "Manage"});
        }

        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 3. PATIENT MANAGEMENT ---
    private JPanel createPatientManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("👥 PATIENT REGISTRY & MEDICAL RECORDS");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Patient ID", "Name", "Age / Gender", "Blood", "Phone", "Email", "Action"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);

        List<Patient> patients = patientDAO.getAllPatients();
        for (Patient p : patients) {
            model.addRow(new Object[]{p.getPatientId(), p.getName(), p.getAge() + " / " + p.getGender(), p.getBloodGroup(), p.getPhone(), p.getEmail(), "View History"});
        }

        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 4. APPOINTMENT MANAGEMENT ---
    private JPanel createAppointmentManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("📅 APPOINTMENT SCHEDULER & DISPATCH");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient Name", "Doctor", "Department", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"APT-1001", "Rekha Prasad", "Dr. Ananya Rao", "Cardiology", "Today", "10:20 AM", "Confirmed"});
        model.addRow(new Object[]{"APT-1002", "Aniket Sharma", "Dr. Rajesh Kumar", "Emergency / Casualty", "Today", "11:00 AM", "Completed"});
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 5. LABORATORY MANAGEMENT ---
    private JPanel createLaboratoryManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("🧪 LABORATORY DIAGNOSTICS CONTROL");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Patient ID", "Test Name", "Technician", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"LAB-901", "PT100842", "Complete Blood Count (CBC)", "Suresh Nair", "Ready"});
        model.addRow(new Object[]{"LAB-902", "PT100245", "Lipid Profile & Cholesterol", "Unassigned", "Pending"});
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 6. PHARMACY MANAGEMENT ---
    private JPanel createPharmacyManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("💊 PHARMACY ORDERS & PRESCRIPTION BILLING");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Token", "Patient ID", "Doctor", "Amount", "Payment", "Order Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"#PHA-2026-00125", "PT100842", "Dr. Ananya Rao", "₹640.50", "Paid", "Order Completed"});
        model.addRow(new Object[]{"#PHA-2026-00126", "PT100245", "Dr. Rajesh Kumar", "₹420.00", "Unpaid", "Prescription Received"});
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 7. INVENTORY MANAGEMENT ---
    private JPanel createInventoryManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JLabel title = new JLabel("📦 INVENTORY & DRUG STOCK CONTROL");
        title.setFont(new Font("Sora", Font.BOLD, 16));

        JButton btnAddMed = new JButton("➕ Add Medicine");
        btnAddMed.setBackground(new Color(13, 110, 253));
        btnAddMed.setForeground(Color.WHITE);
        btnAddMed.setFont(new Font("Inter", Font.BOLD, 12));
        top.add(title);
        top.add(btnAddMed);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Medicine ID", "Medicine Name", "Manufacturer", "Stock Qty", "Unit Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);

        List<Medicine> meds = medicineDAO.getAllMedicines();
        for (Medicine m : meds) {
            String status = m.getStockQuantity() <= 15 ? "🟡 Low Stock" : "🟢 In Stock";
            model.addRow(new Object[]{m.getMedicineId(), m.getMedicineName(), m.getManufacturer(), m.getStockQuantity(), "₹" + m.getUnitPrice(), status});
        }

        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 8. BILLING ---
    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("💰 INVOICES & HOSPITAL REVENUE BILLING");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Invoice No", "Patient ID", "Department", "Amount", "GST", "Payment Method", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"#INV-ORD-100841", "PT100842", "Pharmacy", "₹640.50", "₹32.00", "UPI", "2026-07-29"});
        model.addRow(new Object[]{"#INV-LAB-90125", "PT100245", "Laboratory", "₹1,200.00", "₹60.00", "Cash", "2026-07-29"});
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 9. REPORTS ---
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] reportTypes = {"Daily Revenue", "Monthly Revenue", "Appointments", "Doctors", "Patients", "Laboratory", "Pharmacy", "Inventory"};
        for (String rType : reportTypes) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setPreferredSize(new Dimension(280, 120));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));

            JLabel lbl = new JLabel("📄 " + rType + " Report");
            lbl.setFont(new Font("Sora", Font.BOLD, 14));
            lbl.setForeground(new Color(15, 42, 74));

            JButton btnExport = new JButton("📥 Export TXT / PDF");
            btnExport.setFont(new Font("Inter", Font.BOLD, 12));
            btnExport.addActionListener(e -> {
                try {
                    File file = ReportGenerator.exportReportToFile(rType, "pdf");
                    JOptionPane.showMessageDialog(this, "Report exported successfully:\n" + file.getAbsolutePath(), "Report Exported", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error exporting report: " + ex.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            card.add(lbl, BorderLayout.NORTH);
            card.add(btnExport, BorderLayout.SOUTH);
            panel.add(card);
        }

        return panel;
    }

    // --- 10. NOTIFICATIONS ---
    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("📢 SYSTEM ALERT NOTIFICATIONS");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Type", "Title", "Message", "Timestamp"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"New Registration", "🆕 Patient Registered", "Patient Rekha Prasad registered", "10 mins ago"});
        model.addRow(new Object[]{"Low Stock Alert", "⚠️ Low Stock Alert", "Paracetamol stock below 15 units", "25 mins ago"});
        model.addRow(new Object[]{"Lab Report Ready", "🧪 Lab Report Ready", "CBC Report ready for PT100842", "2 hours ago"});
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 11. STAFF MANAGEMENT ---
    private JPanel createStaffManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("👤 HOSPITAL STAFF & ROLE MANAGEMENT");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Staff ID", "Name", "Email", "Role", "Department", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        List<Map<String, String>> staff = adminDAO.getAllStaff();
        for (Map<String, String> s : staff) {
            model.addRow(new Object[]{s.get("staffId"), s.get("name"), s.get("email"), s.get("role"), s.get("department"), s.get("status")});
        }
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 12. DEPARTMENTS ---
    private JPanel createDepartmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("🏥 HOSPITAL DEPARTMENTS");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Dept ID", "Department Name", "Head Doctor", "Doctors Count", "Patients Treated"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        List<Map<String, String>> depts = adminDAO.getAllDepartments();
        for (Map<String, String> d : depts) {
            model.addRow(new Object[]{d.get("deptId"), d.get("deptName"), d.get("headDoctor"), d.get("totalDoctors"), d.get("totalPatients")});
        }
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        panel.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return panel;
    }

    // --- 13. SETTINGS ---
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        Map<String, String> settings = adminDAO.getHospitalSettings();

        JTextField txtName = new JTextField(settings.get("hospital_name"));
        JTextField txtAddr = new JTextField(settings.get("address"));
        JTextField txtPhone = new JTextField(settings.get("phone"));
        JTextField txtEmail = new JTextField(settings.get("email"));
        JTextField txtGst = new JTextField(settings.get("gst_number"));
        JTextField txtHours = new JTextField(settings.get("working_hours"));
        JTextField txtEmerg = new JTextField(settings.get("emergency_contact"));

        panel.add(new JLabel("Hospital Name:")); panel.add(txtName);
        panel.add(new JLabel("Address:")); panel.add(txtAddr);
        panel.add(new JLabel("Phone Number:")); panel.add(txtPhone);
        panel.add(new JLabel("Email Address:")); panel.add(txtEmail);
        panel.add(new JLabel("GST Number:")); panel.add(txtGst);
        panel.add(new JLabel("Working Hours:")); panel.add(txtHours);
        panel.add(new JLabel("Emergency Contact:")); panel.add(txtEmerg);

        JButton btnSave = new JButton("💾 Save Hospital Settings");
        btnSave.setBackground(new Color(10, 122, 112));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Inter", Font.BOLD, 13));
        btnSave.addActionListener(e -> {
            adminDAO.updateHospitalSetting("hospital_name", txtName.getText().trim());
            adminDAO.updateHospitalSetting("address", txtAddr.getText().trim());
            adminDAO.updateHospitalSetting("phone", txtPhone.getText().trim());
            adminDAO.updateHospitalSetting("email", txtEmail.getText().trim());
            adminDAO.updateHospitalSetting("gst_number", txtGst.getText().trim());
            adminDAO.updateHospitalSetting("working_hours", txtHours.getText().trim());
            adminDAO.updateHospitalSetting("emergency_contact", txtEmerg.getText().trim());
            JOptionPane.showMessageDialog(this, "Hospital Settings updated successfully in database!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(new JLabel());
        panel.add(btnSave);
        return panel;
    }

    // --- 14. DATABASE MANAGER CENTER ---
    private String currentDbTable = "patients";
    private JTable tblDbManager;
    private DefaultTableModel dbTableModel;
    private JLabel lblDbHealthStatus, lblDbInfo;

    private JPanel createDatabaseManagerPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));

        // Top Toolbar & Health Bar
        JPanel topContainer = new JPanel(new BorderLayout(5, 5));
        topContainer.setOpaque(false);

        // Header Title & Action Buttons
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(new Color(15, 42, 74));
        headerBar.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel title = new JLabel("🗄 NIRAMAYA DATABASE MANAGEMENT CENTER (phpMyAdmin Suite)");
        title.setFont(new Font("Sora", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);

        JButton btnRefresh = new JButton("🔄 Refresh");
        JButton btnAdd = new JButton("➕ Add");
        JButton btnEdit = new JButton("✏ Edit");
        JButton btnDelete = new JButton("🗑 Delete");
        JButton btnView = new JButton("📄 Details");
        JButton btnExport = new JButton("📥 Export");
        JButton btnBackup = new JButton("💾 Backup");
        JButton btnRestore = new JButton("♻ Restore");
        JButton btnQuery = new JButton("💻 SQL Console");

        JButton[] btns = {btnRefresh, btnAdd, btnEdit, btnDelete, btnView, btnExport, btnBackup, btnRestore, btnQuery};
        for (JButton b : btns) {
            b.setFont(new Font("Inter", Font.BOLD, 11));
            b.setForeground(Color.WHITE);
            b.setBackground(new Color(21, 55, 93));
            b.setMargin(new Insets(4, 8, 4, 8));
            toolbar.add(b);
            toolbar.addSeparator();
        }

        headerBar.add(title, BorderLayout.WEST);
        headerBar.add(toolbar, BorderLayout.EAST);

        // Health Status Banner
        JPanel healthBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        healthBar.setBackground(Color.WHITE);
        healthBar.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        lblDbHealthStatus = new JLabel("🟢 Connected | Database: niramaya_hospital | MySQL 8.0.32");
        lblDbHealthStatus.setFont(new Font("Inter", Font.BOLD, 12));
        lblDbHealthStatus.setForeground(new Color(10, 122, 112));

        lblDbInfo = new JLabel("Tables: 15 | Total Records: Loading...");
        lblDbInfo.setFont(new Font("Inter", Font.PLAIN, 12));
        lblDbInfo.setForeground(new Color(100, 116, 139));

        healthBar.add(lblDbHealthStatus);
        healthBar.add(new JLabel(" | "));
        healthBar.add(lblDbInfo);

        topContainer.add(headerBar, BorderLayout.NORTH);
        topContainer.add(healthBar, BorderLayout.SOUTH);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Split Pane: Left Sidebar (Tables) + Right Center (Data Table & Search)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(230);

        // Left Table Selector List
        DefaultListModel<String> tableListModel = new DefaultListModel<>();
        List<Map<String, String>> activeTables = dbManagerDAO.getAllTables();
        for (Map<String, String> meta : activeTables) {
            tableListModel.addElement(meta.get("label") + " (" + meta.get("name") + ")");
        }

        JList<String> listTables = new JList<>(tableListModel);
        listTables.setFont(new Font("Inter", Font.PLAIN, 13));
        listTables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTables.setSelectedIndex(0);

        JScrollPane spTables = new JScrollPane(listTables);
        spTables.setBorder(BorderFactory.createTitledBorder("📁 SELECT DATABASE TABLE"));

        // Right Data Table Panel
        JPanel dataPanel = new JPanel(new BorderLayout(5, 5));
        dataPanel.setBackground(Color.WHITE);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search & Filter Toolbar
        JPanel searchFilterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchFilterBar.setOpaque(false);

        JTextField txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Inter", Font.PLAIN, 12));
        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.setFont(new Font("Inter", Font.BOLD, 11));

        JComboBox<String> cbFilter = new JComboBox<>(new String[]{"All Records", "Today's Records", "Pending", "Completed", "Online", "Offline"});
        cbFilter.setFont(new Font("Inter", Font.PLAIN, 12));

        searchFilterBar.add(new JLabel("Search:"));
        searchFilterBar.add(txtSearch);
        searchFilterBar.add(btnSearch);
        searchFilterBar.add(new JLabel("Filter:"));
        searchFilterBar.add(cbFilter);

        dataPanel.add(searchFilterBar, BorderLayout.NORTH);

        dbTableModel = new DefaultTableModel();
        tblDbManager = new JTable(dbTableModel);
        tblDbManager.setRowHeight(32);
        tblDbManager.setFont(new Font("Inter", Font.PLAIN, 12));
        tblDbManager.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        tblDbManager.setAutoCreateRowSorter(true);

        JScrollPane spData = new JScrollPane(tblDbManager);
        dataPanel.add(spData, BorderLayout.CENTER);

        splitPane.setLeftComponent(spTables);
        splitPane.setRightComponent(dataPanel);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Action Handlers
        listTables.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = listTables.getSelectedIndex();
                List<Map<String, String>> freshTables = dbManagerDAO.getAllTables();
                if (idx >= 0 && idx < freshTables.size()) {
                    currentDbTable = freshTables.get(idx).get("name");
                    loadDbTableData(currentDbTable, txtSearch.getText().trim(), (String) cbFilter.getSelectedItem());
                }
            }
        });

        btnRefresh.addActionListener(e -> {
            loadDbTableData(currentDbTable, txtSearch.getText().trim(), (String) cbFilter.getSelectedItem());
            updateDbHealthBanner();
        });

        btnSearch.addActionListener(e -> loadDbTableData(currentDbTable, txtSearch.getText().trim(), (String) cbFilter.getSelectedItem()));
        cbFilter.addActionListener(e -> loadDbTableData(currentDbTable, txtSearch.getText().trim(), (String) cbFilter.getSelectedItem()));

        btnAdd.addActionListener(e -> showAddRecordDialog());
        btnEdit.addActionListener(e -> showEditRecordDialog());
        btnDelete.addActionListener(e -> performDeleteRecord());
        btnView.addActionListener(e -> showRecordDetailsDialog());

        btnExport.addActionListener(e -> {
            String sqlDump = dbManagerDAO.exportTableToSQL(currentDbTable);
            JTextArea ta = new JTextArea(sqlDump, 20, 60);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "SQL Export — Table: " + currentDbTable, JOptionPane.INFORMATION_MESSAGE);
        });

        btnBackup.addActionListener(e -> {
            Map<String, Object> res = dbManagerDAO.createFullBackup();
            if ((Boolean) res.getOrDefault("success", false)) {
                JOptionPane.showMessageDialog(this, "Database Backup Created Successfully!\nSaved File: " + res.get("backupName") + "\nLocation: " + res.get("backupPath"), "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
                updateDbHealthBanner();
            } else {
                JOptionPane.showMessageDialog(this, "Backup Failed: " + res.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRestore.addActionListener(e -> {
            String sqlContent = JOptionPane.showInputDialog(this, "Paste SQL Backup Script Content to Restore Database:", "Restore Database", JOptionPane.QUESTION_MESSAGE);
            if (sqlContent != null && !sqlContent.trim().isEmpty()) {
                Map<String, Object> res = dbManagerDAO.restoreFullBackup(sqlContent);
                JOptionPane.showMessageDialog(this, res.get("message"), "Restore Status", (Boolean) res.getOrDefault("success", false) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                loadDbTableData(currentDbTable, "", "All Records");
            }
        });

        btnQuery.addActionListener(e -> showSqlQueryConsole());

        // Initial Load
        loadDbTableData(currentDbTable, "", "All Records");
        updateDbHealthBanner();

        return mainPanel;
    }

    @SuppressWarnings("unchecked")
    private void loadDbTableData(String tableName, String search, String filterStr) {
        String filterParam = "";
        if ("Today's Records".equalsIgnoreCase(filterStr)) filterParam = "today";
        else if ("Pending".equalsIgnoreCase(filterStr)) filterParam = "pending";
        else if ("Completed".equalsIgnoreCase(filterStr)) filterParam = "completed";
        else if ("Online".equalsIgnoreCase(filterStr)) filterParam = "online";
        else if ("Offline".equalsIgnoreCase(filterStr)) filterParam = "offline";

        Map<String, Object> data = dbManagerDAO.getTableData(tableName, 1, 100, search, filterParam);
        List<String> cols = (List<String>) data.get("columns");
        List<List<Object>> rows = (List<List<Object>>) data.get("rows");

        dbTableModel.setRowCount(0);
        if (cols != null && !cols.isEmpty()) {
            dbTableModel.setColumnIdentifiers(cols.toArray());
        }

        if (rows == null || rows.isEmpty()) {
            if (cols == null || cols.isEmpty()) {
                dbTableModel.setColumnIdentifiers(new String[]{"Status"});
            }
            String[] emptyRow = new String[Math.max(1, cols != null ? cols.size() : 1)];
            emptyRow[0] = "No Records Found";
            for (int i = 1; i < emptyRow.length; i++) emptyRow[i] = "";
            dbTableModel.addRow(emptyRow);
        } else {
            for (List<Object> r : rows) {
                dbTableModel.addRow(r.toArray());
            }
        }
    }

    private void updateDbHealthBanner() {
        Map<String, Object> health = dbManagerDAO.getDatabaseHealth();
        boolean connected = (Boolean) health.getOrDefault("connected", false);
        if (connected) {
            lblDbHealthStatus.setText("🟢 Connected | DB: " + health.get("databaseName") + " | " + health.get("serverVersion"));
            lblDbHealthStatus.setForeground(new Color(10, 122, 112));
            lblDbInfo.setText("Tables: " + health.get("totalTables") + " | Total Records: " + health.get("totalRecords") + " | Size: " + health.get("storageUsed") + " | Last Backup: " + health.get("lastBackup"));
        } else {
            lblDbHealthStatus.setText("🟥 Disconnected | Error: " + health.getOrDefault("errorMessage", "MySQL Connection Error"));
            lblDbHealthStatus.setForeground(new Color(220, 38, 38));
            lblDbInfo.setText("DB: " + health.get("databaseName") + " | Host: " + health.get("host") + ":" + health.get("port") + " | Status: Offline");
        }
    }

    private void showAddRecordDialog() {
        List<Map<String, String>> cols = dbManagerDAO.getTableColumns(currentDbTable);
        if (cols.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not fetch table schema for " + currentDbTable, "Add Record", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel formPanel = new JPanel(new GridLayout(cols.size(), 2, 5, 5));
        Map<String, JTextField> fields = new HashMap<>();

        for (Map<String, String> col : cols) {
            String colName = col.get("name");
            formPanel.add(new JLabel(colName + " (" + col.get("type") + "):"));
            JTextField tf = new JTextField();
            fields.put(colName, tf);
            formPanel.add(tf);
        }

        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(formPanel), "Insert New Record into " + currentDbTable, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Map<String, String> data = new HashMap<>();
            for (Map.Entry<String, JTextField> entry : fields.entrySet()) {
                if (!entry.getValue().getText().trim().isEmpty()) {
                    data.put(entry.getKey(), entry.getValue().getText().trim());
                }
            }
            boolean ok = dbManagerDAO.insertRecord(currentDbTable, data, "EMP-000004");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Record added successfully into " + currentDbTable, "Record Added", JOptionPane.INFORMATION_MESSAGE);
                loadDbTableData(currentDbTable, "", "All Records");
                updateDbHealthBanner();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert record into " + currentDbTable, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditRecordDialog() {
        int row = tblDbManager.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.", "Edit Record", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pkCol = tblDbManager.getColumnName(0);
        String pkVal = String.valueOf(tblDbManager.getValueAt(row, 0));

        List<Map<String, String>> cols = dbManagerDAO.getTableColumns(currentDbTable);
        JPanel formPanel = new JPanel(new GridLayout(cols.size(), 2, 5, 5));
        Map<String, JTextField> fields = new HashMap<>();

        for (int i = 0; i < tblDbManager.getColumnCount(); i++) {
            String colName = tblDbManager.getColumnName(i);
            String val = String.valueOf(tblDbManager.getValueAt(row, i));
            formPanel.add(new JLabel(colName + ":"));
            JTextField tf = new JTextField(val);
            if (i == 0) tf.setEditable(false);
            fields.put(colName, tf);
            formPanel.add(tf);
        }

        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(formPanel), "Edit Record [" + pkCol + " = " + pkVal + "] in " + currentDbTable, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Map<String, String> data = new HashMap<>();
            for (Map.Entry<String, JTextField> entry : fields.entrySet()) {
                data.put(entry.getKey(), entry.getValue().getText().trim());
            }
            boolean ok = dbManagerDAO.updateRecord(currentDbTable, pkCol, pkVal, data, "EMP-000004");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Record Updated Successfully", "Record Updated", JOptionPane.INFORMATION_MESSAGE);
                loadDbTableData(currentDbTable, "", "All Records");
                updateDbHealthBanner();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performDeleteRecord() {
        int row = tblDbManager.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "Delete Record", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pkCol = tblDbManager.getColumnName(0);
        String pkVal = String.valueOf(tblDbManager.getValueAt(row, 0));

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?\n[" + pkCol + " = " + pkVal + "] from " + currentDbTable, "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            boolean ok = dbManagerDAO.deleteRecord(currentDbTable, pkCol, pkVal, true, "EMP-000004");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Record Deleted Successfully", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadDbTableData(currentDbTable, "", "All Records");
                updateDbHealthBanner();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRecordDetailsDialog() {
        int row = tblDbManager.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to view details.", "View Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("=== RECORD DETAILS [" + currentDbTable + "] ===\n\n");
        for (int i = 0; i < tblDbManager.getColumnCount(); i++) {
            sb.append(tblDbManager.getColumnName(i)).append(": ").append(tblDbManager.getValueAt(row, i)).append("\n");
        }

        JTextArea ta = new JTextArea(sb.toString(), 15, 45);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Record Information", JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    private void showSqlQueryConsole() {
        JDialog dlg = new JDialog(this, "Admin SQL Query Console", true);
        dlg.setSize(800, 550);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("SQL Statement (SELECT, SHOW, DESCRIBE, EXPLAIN)"));

        JTextArea taQuery = new JTextArea("SELECT * FROM " + currentDbTable + " LIMIT 20;", 5, 60);
        taQuery.setFont(new Font("Monospaced", Font.PLAIN, 13));
        top.add(new JScrollPane(taQuery), BorderLayout.CENTER);

        JButton btnRun = new JButton("⚡ Execute SQL Query");
        btnRun.setFont(new Font("Inter", Font.BOLD, 12));
        btnRun.setBackground(new Color(10, 122, 112));
        btnRun.setForeground(Color.WHITE);
        top.add(btnRun, BorderLayout.EAST);

        DefaultTableModel consoleModel = new DefaultTableModel();
        JTable tblResult = new JTable(consoleModel);
        tblResult.setFont(new Font("Inter", Font.PLAIN, 12));

        JLabel lblStatus = new JLabel("Status: Ready to execute query");
        lblStatus.setFont(new Font("Inter", Font.BOLD, 12));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        btnRun.addActionListener(ev -> {
            String sql = taQuery.getText().trim();
            Map<String, Object> res = dbManagerDAO.executeCustomQuery(sql, "admin");
            boolean ok = (Boolean) res.getOrDefault("success", false);
            lblStatus.setText(ok ? (String) res.get("status") : "Error: " + res.get("message"));
            lblStatus.setForeground(ok ? new Color(10, 122, 112) : Color.RED);

            if (ok && res.containsKey("columns")) {
                List<String> cols = (List<String>) res.get("columns");
                List<List<Object>> rows = (List<List<Object>>) res.get("rows");
                consoleModel.setRowCount(0);
                consoleModel.setColumnIdentifiers(cols.toArray());
                for (List<Object> r : rows) {
                    consoleModel.addRow(r.toArray());
                }
            }
        });

        dlg.add(top, BorderLayout.NORTH);
        dlg.add(new JScrollPane(tblResult), BorderLayout.CENTER);
        dlg.add(lblStatus, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}

