package com.hospital.gui;

import com.hospital.dao.NurseDAO;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NurseDashboardSwing extends JFrame {

    private final NurseDAO nurseDAO = new NurseDAO();
    private Nurse currentNurse;
    private String selectedPatientId = "PT100842";
    private String selectedPatientName = "Rekha Prasad";

    // UI Components
    private JLabel lblNurseName, lblDepartment, lblShift;
    private JLabel cardPatients, cardVitals, cardMeds, cardAlerts;
    private JTable tablePatients, tableVitals, tableMeds, tableNotes, tableInjections, tableInventory, tableEmergency;
    private DefaultTableModel modelPatients, modelVitals, modelMeds, modelNotes, modelInjections, modelInventory, modelEmergency;

    public NurseDashboardSwing(Nurse nurse) {
        this.currentNurse = nurse != null ? nurse : nurseDAO.getNurseById("NUR10084");
        if (this.currentNurse == null) {
            this.currentNurse = new Nurse("NUR10084", "NUR10084", "Nurse Priya Sharma", "Female", "1995-06-15",
                    "+91 98765 43217", "nurse@niramaya.health", "ICU & Emergency Ward", "B.Sc Nursing", 5,
                    "Morning", "2023-04-10", "124 Healthcare Enclave", "demo1234");
        }

        setTitle("Niramaya Nurse Portal - " + currentNurse.getName());
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Header Navigation Bar
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main Tabbed Workspace
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("📊 Dashboard", createDashboardOverviewPanel());
        tabbedPane.addTab("👥 Assigned Patients", createAssignedPatientsPanel());
        tabbedPane.addTab("🩸 Vital Signs", createVitalSignsPanel());
        tabbedPane.addTab("💊 Medication Admin", createMedicationAdminPanel());
        tabbedPane.addTab("📝 Nursing Notes", createNursingNotesPanel());
        tabbedPane.addTab("💉 Injection Records", createInjectionPanel());
        tabbedPane.addTab("📊 Patient Monitoring", createPatientMonitoringPanel());
        tabbedPane.addTab("🚨 Emergency SOS", createEmergencyPanel());
        tabbedPane.addTab("📦 Inventory Requests", createInventoryPanel());
        tabbedPane.addTab("👤 My Profile", createProfilePanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Load initial live data
        refreshAllData();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Dark Slate Header
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel left = new JPanel(new GridLayout(2, 1, 4, 4));
        left.setOpaque(false);
        lblNurseName = new JLabel("👩‍⚕️ " + currentNurse.getName() + " (" + currentNurse.getNurseId() + ")");
        lblNurseName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNurseName.setForeground(Color.WHITE);

        lblDepartment = new JLabel("Department: " + currentNurse.getDepartment() + " | Shift: " + currentNurse.getShift());
        lblDepartment.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDepartment.setForeground(new Color(148, 163, 184));

        left.add(lblNurseName);
        left.add(lblDepartment);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnSOS = new JButton("🚨 TRIGGER EMERGENCY SOS");
        btnSOS.setBackground(new Color(225, 29, 72));
        btnSOS.setForeground(Color.WHITE);
        btnSOS.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSOS.setFocusPainted(false);
        btnSOS.addActionListener(e -> triggerEmergencyDialog());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setBackground(new Color(30, 41, 59));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> refreshAllData());

        right.add(btnSOS);
        right.add(btnRefresh);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createDashboardOverviewPanel() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(248, 250, 252));

        // Stats Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 15));
        cardsGrid.setOpaque(false);

        cardPatients = createCard("Assigned Patients", "0", new Color(14, 165, 233));
        cardVitals = createCard("Vitals Recorded Today", "0", new Color(16, 185, 129));
        cardMeds = createCard("Pending Medications", "0", new Color(245, 158, 11));
        cardAlerts = createCard("Active Emergency Alerts", "0", new Color(239, 68, 68));

        cardsGrid.add(cardPatients);
        cardsGrid.add(cardVitals);
        cardsGrid.add(cardMeds);
        cardsGrid.add(cardAlerts);

        // Center Content with Quick Actions & Active Alerts
        JPanel center = new JPanel(new GridLayout(1, 2, 15, 15));
        center.setOpaque(false);

        // Emergency Alerts Summary Box
        JPanel alertBox = new JPanel(new BorderLayout());
        alertBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), "🚨 Live Emergency Broadcasts", 0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(15, 23, 42)));
        alertBox.setBackground(Color.WHITE);

        String[] colsEmergency = {"Alert ID", "Patient", "Room / Ward", "Issue / Alert", "Time", "Status"};
        modelEmergency = new DefaultTableModel(colsEmergency, 0);
        tableEmergency = new JTable(modelEmergency);
        tableEmergency.setRowHeight(28);
        alertBox.add(new JScrollPane(tableEmergency), BorderLayout.CENTER);

        // Quick Actions Box
        JPanel quickBox = new JPanel(new GridLayout(5, 1, 10, 10));
        quickBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), "⚡ Quick Nursing Operations", 0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(15, 23, 42)));
        quickBox.setBackground(Color.WHITE);

        JButton b1 = new JButton("➕ Record Patient Vitals (SpO2, BP, Temp)");
        JButton b2 = new JButton("📝 Add Clinical Nursing Progress Note");
        JButton b3 = new JButton("💉 Log Injection Administration");
        JButton b4 = new JButton("📦 Request Pharmacy Supply / Consumables");
        JButton b5 = new JButton("🔄 Submit Shift Handover Report");

        styleButton(b1, new Color(14, 165, 233));
        styleButton(b2, new Color(16, 185, 129));
        styleButton(b3, new Color(139, 92, 246));
        styleButton(b4, new Color(245, 158, 11));
        styleButton(b5, new Color(71, 85, 105));

        b1.addActionListener(e -> recordVitalsDialog());
        b2.addActionListener(e -> addNursingNoteDialog());
        b3.addActionListener(e -> recordInjectionDialog());
        b4.addActionListener(e -> createInventoryRequestDialog());
        b5.addActionListener(e -> submitShiftHandoverDialog());

        quickBox.add(b1);
        quickBox.add(b2);
        quickBox.add(b3);
        quickBox.add(b4);
        quickBox.add(b5);

        center.add(alertBox);
        center.add(quickBox);

        main.add(cardsGrid, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        return main;
    }

    private JLabel createCard(String title, String value, Color color) {
        JLabel label = new JLabel("<html><div style='text-align: center;'><span style='font-size: 11px; color: #64748B;'>" + title + "</span><br><span style='font-size: 24px; font-weight: bold; color: " + toHex(color) + ";'>" + value + "</span></div></html>", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return label;
    }

    private JPanel createAssignedPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Assignment ID", "Patient ID", "Patient Name", "Doctor", "Ward", "Room No", "Bed No", "Admission Date", "Status"};
        modelPatients = new DefaultTableModel(cols, 0);
        tablePatients = new JTable(modelPatients);
        tablePatients.setRowHeight(30);

        tablePatients.getSelectionModel().addListSelectionListener(e -> {
            int row = tablePatients.getSelectedRow();
            if (row >= 0) {
                selectedPatientId = (String) modelPatients.getValueAt(row, 1);
                selectedPatientName = (String) modelPatients.getValueAt(row, 2);
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnRecordVitals = new JButton("🩸 Record Vitals");
        JButton btnAddNote = new JButton("📝 Add Clinical Note");
        JButton btnMedStatus = new JButton("💊 Administer Medicine");
        JButton btnMonitor = new JButton("📊 Log Monitoring");

        styleButton(btnRecordVitals, new Color(14, 165, 233));
        styleButton(btnAddNote, new Color(16, 185, 129));
        styleButton(btnMedStatus, new Color(245, 158, 11));
        styleButton(btnMonitor, new Color(139, 92, 246));

        btnRecordVitals.addActionListener(e -> recordVitalsDialog());
        btnAddNote.addActionListener(e -> addNursingNoteDialog());
        btnMedStatus.addActionListener(e -> recordMedicationDialog());
        btnMonitor.addActionListener(e -> recordMonitoringDialog());

        toolbar.add(btnRecordVitals);
        toolbar.add(btnAddNote);
        toolbar.add(btnMedStatus);
        toolbar.add(btnMonitor);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablePatients), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVitalSignsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Vital ID", "Patient ID", "Nurse", "Temp", "BP", "Pulse", "Resp Rate", "SpO2", "Blood Sugar", "Date", "Time"};
        modelVitals = new DefaultTableModel(cols, 0);
        tableVitals = new JTable(modelVitals);
        tableVitals.setRowHeight(28);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnAdd = new JButton("➕ Record New Vitals");
        styleButton(btnAdd, new Color(16, 185, 129));
        btnAdd.addActionListener(e -> recordVitalsDialog());
        toolbar.add(btnAdd);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableVitals), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMedicationAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Admin ID", "Patient ID", "Medicine", "Dosage", "Status", "Dosage Time", "Missed Reason", "Nurse"};
        modelMeds = new DefaultTableModel(cols, 0);
        tableMeds = new JTable(modelMeds);
        tableMeds.setRowHeight(28);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnUpdate = new JButton("✔️ Mark Dose Administered / Missed");
        styleButton(btnUpdate, new Color(245, 158, 11));
        btnUpdate.addActionListener(e -> recordMedicationDialog());
        toolbar.add(btnUpdate);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableMeds), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNursingNotesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Note ID", "Patient ID", "Nurse Name", "Clinical Observation", "Date", "Time"};
        modelNotes = new DefaultTableModel(cols, 0);
        tableNotes = new JTable(modelNotes);
        tableNotes.setRowHeight(28);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnAdd = new JButton("📝 Add Clinical Note");
        styleButton(btnAdd, new Color(16, 185, 129));
        btnAdd.addActionListener(e -> addNursingNoteDialog());
        toolbar.add(btnAdd);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableNotes), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInjectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Injection ID", "Patient ID", "Injection Name", "Dose", "Route", "Date", "Time", "Remarks"};
        modelInjections = new DefaultTableModel(cols, 0);
        tableInjections = new JTable(modelInjections);
        tableInjections.setRowHeight(28);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnAdd = new JButton("💉 Record Injection");
        styleButton(btnAdd, new Color(139, 92, 246));
        btnAdd.addActionListener(e -> recordInjectionDialog());
        toolbar.add(btnAdd);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableInjections), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPatientMonitoringPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextArea txtMonitoringLog = new JTextArea();
        txtMonitoringLog.setEditable(false);
        txtMonitoringLog.setFont(new Font("Consolas", Font.PLAIN, 13));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnLog = new JButton("📊 Log Patient Monitoring Status");
        styleButton(btnLog, new Color(14, 165, 233));
        btnLog.addActionListener(e -> recordMonitoringDialog());
        toolbar.add(btnLog);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtMonitoringLog), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEmergencyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnPanic = new JButton("🚨 BROADCAST EMERGENCY SOS ALERT");
        btnPanic.setBackground(new Color(225, 29, 72));
        btnPanic.setForeground(Color.WHITE);
        btnPanic.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPanic.addActionListener(e -> triggerEmergencyDialog());

        JButton btnResolve = new JButton("✔️ Resolve Emergency Alert");
        styleButton(btnResolve, new Color(16, 185, 129));
        btnResolve.addActionListener(e -> {
            int row = tableEmergency.getSelectedRow();
            if (row >= 0) {
                String alertId = (String) modelEmergency.getValueAt(row, 0);
                nurseDAO.resolveEmergencyAlert(alertId, currentNurse.getName());
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Emergency alert marked as resolved.");
            } else {
                JOptionPane.showMessageDialog(this, "Select an active emergency alert from table first.");
            }
        });

        toolbar.add(btnPanic);
        toolbar.add(btnResolve);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableEmergency), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Request ID", "Nurse Name", "Item Required", "Qty", "Status", "Request Date", "Remarks", "Approved By"};
        modelInventory = new DefaultTableModel(cols, 0);
        tableInventory = new JTable(modelInventory);
        tableInventory.setRowHeight(28);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnAdd = new JButton("📦 Create Consumable Supply Request");
        styleButton(btnAdd, new Color(245, 158, 11));
        btnAdd.addActionListener(e -> createInventoryRequestDialog());
        toolbar.add(btnAdd);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableInventory), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = new JTextField(currentNurse.getName(), 20);
        JTextField txtPhone = new JTextField(currentNurse.getPhone(), 20);
        JTextField txtDept = new JTextField(currentNurse.getDepartment(), 20);
        JTextField txtQual = new JTextField(currentNurse.getQualification(), 20);
        JTextField txtShift = new JTextField(currentNurse.getShift(), 20);
        JPasswordField txtPass = new JPasswordField(currentNurse.getPassword(), 20);

        int y = 0;
        addFormRow(panel, gbc, "Nurse ID:", new JLabel(currentNurse.getNurseId()), y++);
        addFormRow(panel, gbc, "Full Name:", txtName, y++);
        addFormRow(panel, gbc, "Phone Number:", txtPhone, y++);
        addFormRow(panel, gbc, "Department / Ward:", txtDept, y++);
        addFormRow(panel, gbc, "Qualification:", txtQual, y++);
        addFormRow(panel, gbc, "Assigned Shift:", txtShift, y++);
        addFormRow(panel, gbc, "Password:", txtPass, y++);

        JButton btnSave = new JButton("💾 Save Profile Changes");
        styleButton(btnSave, new Color(16, 185, 129));
        btnSave.addActionListener(e -> {
            currentNurse.setName(txtName.getText());
            currentNurse.setPhone(txtPhone.getText());
            currentNurse.setDepartment(txtDept.getText());
            currentNurse.setQualification(txtQual.getText());
            currentNurse.setShift(txtShift.getText());
            currentNurse.setPassword(new String(txtPass.getPassword()));
            nurseDAO.updateNurseProfile(currentNurse);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            lblNurseName.setText("👩‍⚕️ " + currentNurse.getName() + " (" + currentNurse.getNurseId() + ")");
        });

        gbc.gridx = 1; gbc.gridy = y;
        panel.add(btnSave, gbc);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(l, gbc);
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    // Operations Dialogs
    private void recordVitalsDialog() {
        JTextField txtTemp = new JTextField("98.6 °F");
        JTextField txtBp = new JTextField("120/80 mmHg");
        JTextField txtPulse = new JTextField("72 bpm");
        JTextField txtResp = new JTextField("18 bpm");
        JTextField txtSpO2 = new JTextField("98%");
        JTextField txtSugar = new JTextField("110 mg/dL");

        Object[] message = {
                "Patient ID: " + selectedPatientId + " (" + selectedPatientName + ")",
                "Temperature (°F):", txtTemp,
                "Blood Pressure (mmHg):", txtBp,
                "Pulse Rate (bpm):", txtPulse,
                "Respiratory Rate (bpm):", txtResp,
                "Oxygen Saturation (SpO2 %):", txtSpO2,
                "Blood Sugar (mg/dL):", txtSugar
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Record Patient Vital Signs", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            PatientVital v = new PatientVital(null, selectedPatientId, currentNurse.getNurseId(), currentNurse.getName(),
                    txtTemp.getText(), txtBp.getText(), txtPulse.getText(), txtResp.getText(),
                    txtSpO2.getText(), txtSugar.getText(), "65 kg", "168 cm", null, null);
            nurseDAO.recordVitals(v);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Vitals recorded successfully!");
        }
    }

    private void addNursingNoteDialog() {
        JTextArea txtObs = new JTextArea(5, 30);
        JScrollPane sp = new JScrollPane(txtObs);

        Object[] message = {
                "Patient ID: " + selectedPatientId + " (" + selectedPatientName + ")",
                "Clinical Observations & Daily Notes:", sp
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Nursing Progress Note", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION && !txtObs.getText().trim().isEmpty()) {
            NursingNote n = new NursingNote(null, selectedPatientId, currentNurse.getNurseId(), currentNurse.getName(),
                    txtObs.getText().trim(), null, null);
            nurseDAO.addNursingNote(n);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Nursing note saved!");
        }
    }

    private void recordMedicationDialog() {
        JTextField txtMed = new JTextField("Paracetamol 650mg");
        JTextField txtDose = new JTextField("1 Tablet");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Given", "Missed", "Refused by Patient"});
        JTextField txtReason = new JTextField();

        Object[] message = {
                "Patient ID: " + selectedPatientId + " (" + selectedPatientName + ")",
                "Medicine Name:", txtMed,
                "Dosage:", txtDose,
                "Status:", cbStatus,
                "Missed / Refused Reason (if any):", txtReason
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Administer Medication", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            MedicationAdmin m = new MedicationAdmin(null, selectedPatientId, "RX-908124", txtMed.getText(),
                    txtDose.getText(), (String) cbStatus.getSelectedItem(), new SimpleDateFormat("hh:mm a").format(new Date()),
                    txtReason.getText(), currentNurse.getNurseId(), currentNurse.getName());
            nurseDAO.addMedicationAdmin(m);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Medication status recorded!");
        }
    }

    private void recordInjectionDialog() {
        JTextField txtInj = new JTextField("Inj. Ceftriaxone 1g");
        JTextField txtDose = new JTextField("1g IV");
        JComboBox<String> cbRoute = new JComboBox<>(new String[]{"IV Push", "IV Infusion", "IM", "Subcutaneous"});
        JTextField txtRemarks = new JTextField("Administered slowly over 3 mins.");

        Object[] message = {
                "Patient ID: " + selectedPatientId + " (" + selectedPatientName + ")",
                "Injection Name:", txtInj,
                "Dose:", txtDose,
                "Administration Route:", cbRoute,
                "Remarks:", txtRemarks
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Record Injection Administration", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            InjectionRecord inj = new InjectionRecord(null, selectedPatientId, currentNurse.getNurseId(), currentNurse.getName(),
                    txtInj.getText(), txtDose.getText(), (String) cbRoute.getSelectedItem(), null, null, txtRemarks.getText());
            nurseDAO.recordInjection(inj);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Injection record saved!");
        }
    }

    private void recordMonitoringDialog() {
        JComboBox<String> cbPain = new JComboBox<>(new String[]{"No Pain (0/10)", "Mild (2/10)", "Moderate (5/10)", "Severe (8/10)"});
        JTextField txtFood = new JTextField("Normal Breakfast & Fluids");
        JTextField txtWater = new JTextField("1.5 Liters");
        JTextField txtSleep = new JTextField("Good (7 Hours)");
        JTextField txtUrine = new JTextField("600 ml");
        JTextField txtCond = new JTextField("Stable & Conscious");

        Object[] message = {
                "Patient ID: " + selectedPatientId + " (" + selectedPatientName + ")",
                "Pain Scale:", cbPain,
                "Food Intake:", txtFood,
                "Water Intake:", txtWater,
                "Sleep Quality:", txtSleep,
                "Urine Output:", txtUrine,
                "General Condition:", txtCond
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Log Patient Monitoring", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            PatientMonitoring mon = new PatientMonitoring(null, selectedPatientId, currentNurse.getNurseId(), currentNurse.getName(),
                    (String) cbPain.getSelectedItem(), txtFood.getText(), txtWater.getText(), txtSleep.getText(),
                    txtUrine.getText(), "Normal", txtCond.getText(), "Responding well to treatment.", null, null);
            nurseDAO.recordPatientMonitoring(mon);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Monitoring log saved!");
        }
    }

    private void createInventoryRequestDialog() {
        JTextField txtItem = new JTextField("Sterile Surgical Gloves (Medium)");
        JTextField txtQty = new JTextField("50");
        JTextField txtRemarks = new JTextField("Required for ICU Ward 3");

        Object[] message = {
                "Item Name Required:", txtItem,
                "Quantity Required:", txtQty,
                "Remarks / Ward:", txtRemarks
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Request Consumable Supplies", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int qty = 1;
            try { qty = Integer.parseInt(txtQty.getText()); } catch (Exception ignored) {}
            InventoryRequest req = new InventoryRequest(null, currentNurse.getNurseId(), currentNurse.getName(),
                    txtItem.getText(), qty, "Pending", null, txtRemarks.getText());
            nurseDAO.createInventoryRequest(req);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Inventory request submitted to pharmacy/hospital store!");
        }
    }

    private void triggerEmergencyDialog() {
        JComboBox<String> cbAlert = new JComboBox<>(new String[]{
                "Critical Cardiac Arrest / Code Blue",
                "Low Oxygen Saturation (SpO2 < 85%)",
                "Severe Hypotension / Septic Shock",
                "Uncontrolled Post-Op Bleeding",
                "Respiratory Distress"
        });
        JTextField txtRoom = new JTextField("ICU-302");
        JTextField txtWard = new JTextField("ICU Ward 3");

        Object[] message = {
                "Patient ID: " + selectedPatientId + " (" + selectedPatientName + ")",
                "Room Number:", txtRoom,
                "Ward / Unit:", txtWard,
                "Emergency Type / Panic Condition:", cbAlert
        };

        int option = JOptionPane.showConfirmDialog(this, message, "🚨 TRIGGER EMERGENCY SOS BROADCAST", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            EmergencyAlert alert = new EmergencyAlert(null, selectedPatientId, selectedPatientName,
                    txtRoom.getText(), txtWard.getText(), currentNurse.getNurseId(), currentNurse.getName(),
                    (String) cbAlert.getSelectedItem(), new SimpleDateFormat("hh:mm a").format(new Date()));
            nurseDAO.createEmergencyAlert(alert);
            refreshAllData();
            JOptionPane.showMessageDialog(this, "🚨 EMERGENCY ALERT BROADCASTED TO DOCTOR & ADMIN CONSOLES!", "CRITICAL ALERT SENT", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void submitShiftHandoverDialog() {
        JTextArea txtNotes = new JTextArea(5, 30);
        txtNotes.setText("All assigned patients monitored. Bed-04 vitals normal. High risk alert resolved.");

        Object[] message = {
                "Current Shift: " + currentNurse.getShift(),
                "Handover Notes & Patient Status Summary:", new JScrollPane(txtNotes)
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Submit Nurse Shift Handover", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            NurseShift shift = new NurseShift(null, currentNurse.getNurseId(), currentNurse.getName(),
                    currentNurse.getShift() + " Shift", "07:00 AM", "03:00 PM", currentNurse.getDepartment(),
                    txtNotes.getText().trim(), "Completed");
            nurseDAO.recordShift(shift);
            JOptionPane.showMessageDialog(this, "Shift handover logged successfully!");
        }
    }

    private void refreshAllData() {
        // Assigned Patients
        modelPatients.setRowCount(0);
        List<NurseAssignment> patients = nurseDAO.getAssignedPatients(currentNurse.getNurseId(), "");
        for (NurseAssignment a : patients) {
            modelPatients.addRow(new Object[]{
                    a.getAssignmentId(), a.getPatientId(), a.getPatientName(), a.getDoctorName(),
                    a.getWard(), a.getRoomNumber(), a.getBedNumber(), a.getAdmissionDate(), a.getStatus()
            });
        }

        // Vitals
        modelVitals.setRowCount(0);
        List<PatientVital> vitals = nurseDAO.getVitalsForPatient(selectedPatientId);
        for (PatientVital v : vitals) {
            modelVitals.addRow(new Object[]{
                    v.getVitalId(), v.getPatientId(), v.getNurseName(), v.getTemperature(),
                    v.getBloodPressure(), v.getPulseRate(), v.getRespiratoryRate(),
                    v.getOxygenSaturation(), v.getBloodSugar(), v.getRecordedDate(), v.getRecordedTime()
            });
        }

        // Meds
        modelMeds.setRowCount(0);
        List<MedicationAdmin> meds = nurseDAO.getMedicationAdmins(selectedPatientId);
        for (MedicationAdmin m : meds) {
            modelMeds.addRow(new Object[]{
                    m.getAdminId(), m.getPatientId(), m.getMedicineName(), m.getDosage(),
                    m.getStatus(), m.getDosageTime(), m.getMissedReason(), m.getNurseName()
            });
        }

        // Notes
        modelNotes.setRowCount(0);
        List<NursingNote> notes = nurseDAO.getNursingNotesForPatient(selectedPatientId);
        for (NursingNote n : notes) {
            modelNotes.addRow(new Object[]{
                    n.getNoteId(), n.getPatientId(), n.getNurseName(), n.getObservation(), n.getNoteDate(), n.getNoteTime()
            });
        }

        // Injections
        modelInjections.setRowCount(0);
        List<InjectionRecord> injs = nurseDAO.getInjectionRecords(selectedPatientId);
        for (InjectionRecord i : injs) {
            modelInjections.addRow(new Object[]{
                    i.getInjectionId(), i.getPatientId(), i.getInjectionName(), i.getDose(),
                    i.getRoute(), i.getRecordDate(), i.getRecordTime(), i.getRemarks()
            });
        }

        // Inventory
        modelInventory.setRowCount(0);
        List<InventoryRequest> reqs = nurseDAO.getAllInventoryRequests();
        for (InventoryRequest r : reqs) {
            modelInventory.addRow(new Object[]{
                    r.getRequestId(), r.getNurseName(), r.getItemName(), r.getQuantity(),
                    r.getStatus(), r.getRequestDate(), r.getRemarks(), r.getApprovedBy()
            });
        }

        // Emergency
        modelEmergency.setRowCount(0);
        List<EmergencyAlert> alerts = nurseDAO.getActiveEmergencyAlerts();
        for (EmergencyAlert e : alerts) {
            modelEmergency.addRow(new Object[]{
                    e.getAlertId(), e.getPatientName(), e.getRoomNumber() + " (" + e.getWard() + ")",
                    e.getAlertType(), e.getAlertTime(), e.getStatus()
            });
        }

        // Cards Update
        cardPatients.setText("<html><div style='text-align: center;'><span style='font-size: 11px; color: #64748B;'>Assigned Patients</span><br><span style='font-size: 24px; font-weight: bold; color: #0EA5E9;'>" + patients.size() + "</span></div></html>");
        cardVitals.setText("<html><div style='text-align: center;'><span style='font-size: 11px; color: #64748B;'>Vitals Recorded Today</span><br><span style='font-size: 24px; font-weight: bold; color: #10B981;'>" + vitals.size() + "</span></div></html>");
        cardMeds.setText("<html><div style='text-align: center;'><span style='font-size: 11px; color: #64748B;'>Pending Medications</span><br><span style='font-size: 24px; font-weight: bold; color: #F59E0B;'>" + meds.size() + "</span></div></html>");
        cardAlerts.setText("<html><div style='text-align: center;'><span style='font-size: 11px; color: #64748B;'>Active Emergency Alerts</span><br><span style='font-size: 24px; font-weight: bold; color: #EF4444;'>" + alerts.size() + "</span></div></html>");
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NurseDashboardSwing frame = new NurseDashboardSwing(null);
            frame.setVisible(true);
        });
    }
}
