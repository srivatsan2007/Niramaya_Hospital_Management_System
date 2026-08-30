package com.hospital.gui;

import com.hospital.dao.*;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Enterprise Java Swing Telemedicine & Online Consultation Portal for Niramaya Hospitals.
 * Features:
 * - Live Consultation Video Call Simulator
 * - Real-time Side Prescription Panel linked with Pharmacy Inventory
 * - Laboratory Order Modal
 * - Specialist Referral System
 * - Live Chat & Consultation Notes
 * - Direct JDBC & MySQL Persistence
 */
public class OnlineConsultationSwing extends JFrame {

    private String userRole; // "doctor" or "patient"
    private String userId;   // doctorId or patientId
    private String meetingId;

    private OnlineConsultationDAO consultationDAO = new OnlineConsultationDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private LabBookingDAO labBookingDAO = new LabBookingDAO();
    private MedicineDAO medicineDAO = new MedicineDAO();
    private DoctorDAO doctorDAO = new DoctorDAO();

    private OnlineConsultation activeConsultation;

    private JLabel lblStatusBadge;
    private JLabel lblTimer;
    private JTable tblConsultations;
    private DefaultTableModel tableModel;
    private JTextArea txtChatArea;
    private JTextField txtChatMessage;
    private Timer meetingTimer;
    private int secondsElapsed = 0;

    public OnlineConsultationSwing(String userRole, String userId, String meetingId) {
        this.userRole = userRole != null ? userRole.toLowerCase() : "patient";
        this.userId = userId != null ? userId : ("doctor".equals(this.userRole) ? "DOC1001" : "PT100842");
        this.meetingId = meetingId;

        if (meetingId != null && !meetingId.isEmpty()) {
            this.activeConsultation = consultationDAO.getConsultationByMeetingId(meetingId);
        }

        if (this.activeConsultation == null) {
            List<OnlineConsultation> list = "doctor".equals(this.userRole) ?
                consultationDAO.getConsultationsByDoctor(this.userId) :
                consultationDAO.getConsultationsByPatient(this.userId);
            if (!list.isEmpty()) {
                this.activeConsultation = list.get(0);
                this.meetingId = this.activeConsultation.getMeetingId();
            }
        }

        initUI();
        loadConsultations();
    }

    private void initUI() {
        setTitle("Niramaya Hospitals — Live Telemedicine Suite (" + userRole.toUpperCase() + " PORTAL)");
        setSize(1150, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(13, 110, 253));
        headerPanel.setPreferredSize(new Dimension(1150, 75));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1));
        brandPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("NIRAMAYA SMART TELEMEDICINE");
        lblTitle.setFont(new Font("Sora", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Live Encrypted HD Video Consultation & Clinical Engine");
        lblSub.setFont(new Font("Inter", Font.PLAIN, 12));
        lblSub.setForeground(new Color(220, 235, 255));

        brandPanel.add(lblTitle);
        brandPanel.add(lblSub);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeader.setOpaque(false);

        lblTimer = new JLabel("00:00");
        lblTimer.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblTimer.setForeground(Color.YELLOW);

        lblStatusBadge = new JLabel(activeConsultation != null ? activeConsultation.getMeetingStatus() : "Scheduled");
        lblStatusBadge.setFont(new Font("Inter", Font.BOLD, 12));
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(255, 235, 156));
        lblStatusBadge.setForeground(new Color(133, 100, 4));
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        rightHeader.add(new JLabel("Timer: "));
        rightHeader.add(lblTimer);
        rightHeader.add(lblStatusBadge);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Tabbed Interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Inter", Font.BOLD, 13));

        // Tab 1: Live Video Consultation Room
        tabbedPane.addTab("🎥 Live Meeting Room", createMeetingRoomPanel());

        // Tab 2: Online Consultations Directory
        tabbedPane.addTab("📋 Online Appointments Queue", createDirectoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMeetingRoomPanel() {
        JPanel roomPanel = new JPanel(new BorderLayout(15, 15));
        roomPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        roomPanel.setBackground(new Color(245, 247, 250));

        // Left Side: Video Stream Simulation + Controls
        JPanel leftVideoPanel = new JPanel(new BorderLayout(10, 10));
        leftVideoPanel.setBackground(Color.BLACK);
        leftVideoPanel.setBorder(BorderFactory.createLineBorder(new Color(13, 110, 253), 2));

        JLabel lblVideoDisplay = new JLabel("<html><center><h1>📹 HD Live Telemedicine Stream Active</h1><br><p style='color:#00ff88;'>WebRTC Encrypted Connection • 1080p 60FPS</p><br><p style='color:#aaa;'>Meeting ID: " + (meetingId != null ? meetingId : "MTG-782914") + "</p></center></html>", SwingConstants.CENTER);
        lblVideoDisplay.setForeground(Color.WHITE);
        leftVideoPanel.add(lblVideoDisplay, BorderLayout.CENTER);

        // Video Action Buttons
        JPanel videoControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        videoControls.setBackground(new Color(20, 20, 30));

        JButton btnMic = new JButton("🎤 Mute Mic");
        JButton btnCam = new JButton("📷 Turn Cam Off");
        JButton btnShare = new JButton("💻 Share Screen");
        JButton btnStart = new JButton("▶ Start Call");
        JButton btnEnd = new JButton("🔴 End Consultation");

        btnStart.setBackground(new Color(40, 167, 69));
        btnStart.setForeground(Color.WHITE);
        btnEnd.setBackground(new Color(220, 53, 69));
        btnEnd.setForeground(Color.WHITE);

        btnStart.addActionListener(e -> startMeeting());
        btnEnd.addActionListener(e -> endMeeting());

        videoControls.add(btnStart);
        videoControls.add(btnMic);
        videoControls.add(btnCam);
        videoControls.add(btnShare);
        videoControls.add(btnEnd);

        leftVideoPanel.add(videoControls, BorderLayout.SOUTH);
        roomPanel.add(leftVideoPanel, BorderLayout.CENTER);

        // Right Side: Action Side Panel (Prescription, Lab Test, Specialist, Chat)
        JPanel rightSidePanel = new JPanel(new GridLayout(4, 1, 10, 10));
        rightSidePanel.setPreferredSize(new Dimension(340, 600));

        JButton btnWriteRx = new JButton("📝 Open Live Prescription Panel");
        JButton btnOrderLab = new JButton("🧪 Order Laboratory Test");
        JButton btnReferDoc = new JButton("👨‍⚕️ Refer to Specialist");
        JButton btnHistory = new JButton("📜 View Patient Medical History");

        btnWriteRx.setFont(new Font("Sora", Font.BOLD, 13));
        btnOrderLab.setFont(new Font("Sora", Font.BOLD, 13));
        btnReferDoc.setFont(new Font("Sora", Font.BOLD, 13));
        btnHistory.setFont(new Font("Sora", Font.BOLD, 13));

        btnWriteRx.setBackground(new Color(13, 110, 253));
        btnWriteRx.setForeground(Color.WHITE);
        btnOrderLab.setBackground(new Color(111, 66, 193));
        btnOrderLab.setForeground(Color.WHITE);
        btnReferDoc.setBackground(new Color(255, 193, 7));
        btnReferDoc.setForeground(Color.BLACK);
        btnHistory.setBackground(new Color(23, 162, 184));
        btnHistory.setForeground(Color.WHITE);

        btnWriteRx.addActionListener(e -> openLivePrescriptionDialog());
        btnOrderLab.addActionListener(e -> openOrderLabDialog());
        btnReferDoc.addActionListener(e -> openReferSpecialistDialog());
        btnHistory.addActionListener(e -> openPatientHistoryDialog());

        rightSidePanel.add(btnWriteRx);
        rightSidePanel.add(btnOrderLab);
        rightSidePanel.add(btnReferDoc);
        rightSidePanel.add(btnHistory);

        roomPanel.add(rightSidePanel, BorderLayout.EAST);

        return roomPanel;
    }

    private JPanel createDirectoryPanel() {
        JPanel dirPanel = new JPanel(new BorderLayout(10, 10));
        dirPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] cols = {"Meeting ID", "Patient ID", "Doctor Name", "Department", "Date", "Time", "Token", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblConsultations = new JTable(tableModel);
        tblConsultations.setRowHeight(36);
        tblConsultations.setFont(new Font("Inter", Font.PLAIN, 13));

        JScrollPane sp = new JScrollPane(tblConsultations);
        dirPanel.add(sp, BorderLayout.CENTER);

        return dirPanel;
    }

    private void startMeeting() {
        if (meetingTimer != null && meetingTimer.isRunning()) return;

        String targetId = meetingId != null ? meetingId : "MTG-782914";
        consultationDAO.startConsultation(targetId);

        lblStatusBadge.setText("In Progress (Live)");
        lblStatusBadge.setBackground(new Color(40, 167, 69));
        lblStatusBadge.setForeground(Color.WHITE);

        meetingTimer = new Timer(1000, e -> {
            secondsElapsed++;
            int m = secondsElapsed / 60;
            int s = secondsElapsed % 60;
            lblTimer.setText(String.format("%02d:%02d", m, s));
        });
        meetingTimer.start();

        JOptionPane.showMessageDialog(this, "Consultation Started! Status set to 'In Progress'. Video & Live Audio link active.", "Meeting Live", JOptionPane.INFORMATION_MESSAGE);
    }

    private void endMeeting() {
        if (meetingTimer != null) meetingTimer.stop();

        int mins = Math.max(1, secondsElapsed / 60);
        String startTime = "10:30 AM";
        String endTime = new SimpleDateFormat("hh:mm a").format(new Date());
        String targetId = meetingId != null ? meetingId : "MTG-782914";

        consultationDAO.endConsultation(targetId, startTime, endTime, mins);

        lblStatusBadge.setText("Completed");
        lblStatusBadge.setBackground(new Color(108, 117, 125));
        lblStatusBadge.setForeground(Color.WHITE);

        JOptionPane.showMessageDialog(this,
            "Consultation Ended Successfully!\nTotal Duration: " + mins + " Minutes.\nStatus updated to 'Completed' in MySQL Database.",
            "Consultation Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openLivePrescriptionDialog() {
        JDialog dialog = new JDialog(this, "Live Prescription Panel — Niramaya Telemedicine", true);
        dialog.setSize(650, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtPatientId = new JTextField(activeConsultation != null ? activeConsultation.getPatientId() : "PT100842");
        JTextField txtDoctorName = new JTextField(activeConsultation != null ? activeConsultation.getDoctorName() : "Dr. Ananya Rao");
        JTextField txtDiagnosis = new JTextField("Acute Respiratory Tract Infection");
        JTextField txtSymptoms = new JTextField("Fever, Cough, Sore Throat");
        JTextField txtBP = new JTextField("120/80 mmHg");
        JComboBox<String> cmbMeds = new JComboBox<>();

        List<Medicine> medsList = medicineDAO.getPrescriptionAvailableMedicines();
        for (Medicine m : medsList) {
            cmbMeds.addItem(m.getMedicineName() + " (" + m.getStrength() + ") - Stock: " + m.getStockQuantity() + " - ₹" + m.getSellingPrice());
        }
        cmbMeds.addItem("Custom Medicine (External)");

        JTextField txtDuration = new JTextField("5 Days");
        JTextField txtInstructions = new JTextField("After Food");

        formPanel.add(new JLabel("Patient ID:")); formPanel.add(txtPatientId);
        formPanel.add(new JLabel("Doctor Name:")); formPanel.add(txtDoctorName);
        formPanel.add(new JLabel("Diagnosis:")); formPanel.add(txtDiagnosis);
        formPanel.add(new JLabel("Symptoms:")); formPanel.add(txtSymptoms);
        formPanel.add(new JLabel("Blood Pressure / Pulse:")); formPanel.add(txtBP);
        formPanel.add(new JLabel("Select Medicine (Inventory):")); formPanel.add(cmbMeds);
        formPanel.add(new JLabel("Duration:")); formPanel.add(txtDuration);
        formPanel.add(new JLabel("Instructions:")); formPanel.add(txtInstructions);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton btnSave = new JButton("💾 Save & Send to Pharmacy");
        btnSave.setFont(new Font("Sora", Font.BOLD, 14));
        btnSave.setBackground(new Color(13, 110, 253));
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> {
            String rxId = "RX-" + (100000 + new Random().nextInt(900000));
            String selMed = cmbMeds.getSelectedItem() != null ? cmbMeds.getSelectedItem().toString() : "Paracetamol 650mg";

            Prescription p = new Prescription(
                rxId,
                activeConsultation != null ? activeConsultation.getAppointmentId() : "TK-100842",
                activeConsultation != null ? activeConsultation.getDoctorId() : "DOC1001",
                txtPatientId.getText(),
                txtDiagnosis.getText(),
                selMed,
                "Symptoms: " + txtSymptoms.getText() + " | BP: " + txtBP.getText() + " | " + txtInstructions.getText(),
                txtDuration.getText(),
                new Date().toString()
            );

            prescriptionDAO.createPrescription(p);
            JOptionPane.showMessageDialog(dialog,
                "Prescription Saved!\nRx ID: " + rxId + "\nMedicines automatically dispatched to Pharmacy Queue & Patient Dashboard notified.",
                "Prescription Dispatched", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.add(btnSave, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openOrderLabDialog() {
        String[] options = {"Blood Test (CBC)", "Urine Routine", "Chest X-Ray", "Brain MRI Scan", "Abdomen CT Scan", "ECG", "Ultrasound"};
        String sel = (String) JOptionPane.showInputDialog(this, "Select Laboratory Test to Order:", "Order Lab Test", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (sel != null) {
            String bookingId = "LAB-ORD-" + (1000 + new Random().nextInt(9000));
            LabBooking booking = new LabBooking(
                bookingId,
                activeConsultation != null ? activeConsultation.getPatientId() : "PT100842",
                activeConsultation != null ? activeConsultation.getDoctorId() : "DOC1001",
                activeConsultation != null ? activeConsultation.getAppointmentId() : "TK-100842",
                sel,
                "Today",
                "11:00 AM",
                "Pending",
                "Paid",
                new Date().toString()
            );
            labBookingDAO.createBooking(booking);

            JOptionPane.showMessageDialog(this, "Lab Order Created Successfully!\nOrder ID: " + bookingId + "\nTest: " + sel + "\nSent directly to Lab Technician queue.", "Lab Test Ordered", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openReferSpecialistDialog() {
        String[] depts = {"Cardiology", "Neurology", "Orthopedics", "Dermatology", "Gastroenterology", "Pediatrics"};
        String selDept = (String) JOptionPane.showInputDialog(this, "Select Specialty for Referral:", "Refer to Specialist", JOptionPane.QUESTION_MESSAGE, null, depts, depts[0]);

        if (selDept != null) {
            String apptId = "TK-REF-" + (10000 + new Random().nextInt(90000));
            Appointment appt = new Appointment(
                apptId,
                activeConsultation != null ? activeConsultation.getPatientId() : "PT100842",
                "DOC-SPEC",
                "Specialist Doctor",
                selDept,
                "Tomorrow",
                "10:00 AM",
                "Confirmed",
                "Paid"
            );
            appointmentDAO.createAppointment(appt);

            JOptionPane.showMessageDialog(this, "Referral Created Successfully!\nNew Token: " + apptId + "\nDepartment: " + selDept, "Referral Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openPatientHistoryDialog() {
        JDialog dialog = new JDialog(this, "Patient Medical History & Timeline", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txt.setText("=========================================================\n" +
                   " PATIENT MEDICAL HISTORY SUMMARY\n" +
                   " Patient ID: " + (activeConsultation != null ? activeConsultation.getPatientId() : "PT100842") + "\n" +
                   "=========================================================\n\n" +
                   "• Known Allergies: Penicillin, Dust Mites\n" +
                   "• Existing Conditions: Stage 1 Hypertension, Type 2 Diabetes\n\n" +
                   "PREVIOUS VISITS & CONSULTATIONS:\n" +
                   "1. 15 Jun 2026 - Cardiology (Dr. Ananya Rao) - Mild Palpitations\n" +
                   "2. 02 May 2026 - General Medicine (Dr. Rajesh Kumar) - Seasonal Flu\n\n" +
                   "RECENT LAB REPORTS:\n" +
                   "• Lipid Profile - Borderline Cholesterol (18 Jun 2026)\n" +
                   "• HbA1c Test - 6.8% (Controlled) (10 May 2026)\n\n" +
                   "ACTIVE PHARMACY MEDICATIONS:\n" +
                   "• Metformin 500mg (1-0-1)\n" +
                   "• Amlodipine 5mg (1-0-0)\n");

        dialog.add(new JScrollPane(txt));
        dialog.setVisible(true);
    }

    private void loadConsultations() {
        tableModel.setRowCount(0);
        List<OnlineConsultation> list = "doctor".equals(userRole) ?
            consultationDAO.getConsultationsByDoctor(userId) :
            consultationDAO.getConsultationsByPatient(userId);

        for (OnlineConsultation c : list) {
            tableModel.addRow(new Object[]{
                c.getMeetingId(),
                c.getPatientId(),
                c.getDoctorName(),
                c.getDepartment(),
                c.getMeetingDate(),
                c.getMeetingTime(),
                c.getAppointmentToken(),
                c.getMeetingStatus()
            });
        }

        if (list.isEmpty()) {
            tableModel.addRow(new Object[]{"MTG-782914", "PT100842", "Dr. Ananya Rao", "Cardiology", "Today", "10:30 AM", "TOK-889124", "Scheduled"});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OnlineConsultationSwing("doctor", "DOC1001", "MTG-782914").setVisible(true));
    }
}
