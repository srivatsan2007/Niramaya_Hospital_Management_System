package com.hospital.gui;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.AppointmentDAO;
import com.hospital.model.Doctor;
import com.hospital.model.Appointment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Enterprise Java Swing Doctor Dashboard for Niramaya Hospitals.
 * Features: Multi-Doctor Isolation, Animated Availability Toggle (ONLINE/OFFLINE), JDBC Connection.
 */
public class DoctorDashboardSwing extends JFrame {

    private String doctorId;
    private Doctor currentDoctor;

    private DoctorDAO doctorDAO = new DoctorDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    private JLabel lblDoctorName;
    private JLabel lblStatusBadge;
    private JToggleButton btnToggleAvailability;
    private JTable tblAppointments;
    private DefaultTableModel tableModel;

    public DoctorDashboardSwing(String doctorId) {
        this.doctorId = doctorId;
        this.currentDoctor = doctorDAO.getDoctorByIdOrEmail(doctorId);
        if (this.currentDoctor == null) {
            // Default demo doctor if not found in database
            this.currentDoctor = new Doctor("DOC1001", "Dr. Ananya Rao", "+91 98765 43211", 38, "Female", "ananya.rao@niramaya.health", "demo1234", "MD, DM", "Cardiology", 800.0, "Mon - Sat", "09:00 AM - 04:00 PM", "Offline", false, "2026-07-29");
            doctorDAO.createDoctor(this.currentDoctor);
        }

        initUI();
        loadAppointments();
    }

    private void initUI() {
        setTitle("Niramaya Hospitals — Doctor Dashboard (" + currentDoctor.getDoctorName() + ")");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel (Blue Glassmorphic Style)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(13, 110, 253));
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1));
        brandPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("NIRAMAYA HOSPITALS");
        lblTitle.setFont(new Font("Sora", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        lblDoctorName = new JLabel(currentDoctor.getDoctorName() + " (" + currentDoctor.getDepartment() + ")");
        lblDoctorName.setFont(new Font("Inter", Font.PLAIN, 13));
        lblDoctorName.setForeground(new Color(220, 235, 255));

        brandPanel.add(lblTitle);
        brandPanel.add(lblDoctorName);

        // Toggle Switch & Status Panel
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        togglePanel.setOpaque(false);

        lblStatusBadge = new JLabel(currentDoctor.getStatus() != null ? currentDoctor.getStatus() : "Offline");
        lblStatusBadge.setFont(new Font("Inter", Font.BOLD, 12));
        lblStatusBadge.setOpaque(true);
        updateStatusBadgeColors();

        boolean isOnline = "Online".equalsIgnoreCase(currentDoctor.getStatus());
        btnToggleAvailability = new JToggleButton(isOnline ? "ONLINE [ ON ]" : "OFFLINE [ OFF ]", isOnline);
        btnToggleAvailability.setFont(new Font("Inter", Font.BOLD, 12));
        btnToggleAvailability.setFocusPainted(false);
        btnToggleAvailability.setPreferredSize(new Dimension(150, 36));
        btnToggleAvailability.setBackground(isOnline ? new Color(0, 200, 83) : new Color(220, 53, 69));
        btnToggleAvailability.setForeground(Color.WHITE);

        btnToggleAvailability.addActionListener(e -> toggleAvailability());

        togglePanel.add(lblStatusBadge);
        togglePanel.add(btnToggleAvailability);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(togglePanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Queue Panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        centerPanel.setBackground(new Color(245, 247, 250));

        JLabel lblQueueHeader = new JLabel("Today's Patient Appointments Queue");
        lblQueueHeader.setFont(new Font("Sora", Font.BOLD, 16));
        lblQueueHeader.setForeground(new Color(15, 42, 74));
        centerPanel.add(lblQueueHeader, BorderLayout.NORTH);

        // Table
        String[] columns = {"Token / ID", "Patient ID", "Date", "Time", "Consultation", "Payment", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblAppointments = new JTable(tableModel);
        tblAppointments.setRowHeight(36);
        tblAppointments.setFont(new Font("Inter", Font.PLAIN, 13));
        tblAppointments.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        tblAppointments.getTableHeader().setBackground(new Color(230, 238, 250));

        JScrollPane scrollPane = new JScrollPane(tblAppointments);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Telemedicine Action Panel
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomActionPanel.setBackground(new Color(245, 247, 250));

        JButton btnLaunchTelemedicine = new JButton("🎥 Open Online Telemedicine Suite");
        btnLaunchTelemedicine.setFont(new Font("Sora", Font.BOLD, 13));
        btnLaunchTelemedicine.setBackground(new Color(13, 110, 253));
        btnLaunchTelemedicine.setForeground(Color.WHITE);
        btnLaunchTelemedicine.setPreferredSize(new Dimension(280, 40));
        btnLaunchTelemedicine.addActionListener(e -> {
            new OnlineConsultationSwing("doctor", currentDoctor.getDoctorId(), "MTG-782914").setVisible(true);
        });

        JButton btnLaunchNursePortal = new JButton("👩‍⚕️ Nurse Vitals & SOS Alerts");
        btnLaunchNursePortal.setFont(new Font("Sora", Font.BOLD, 13));
        btnLaunchNursePortal.setBackground(new Color(13, 148, 136));
        btnLaunchNursePortal.setForeground(Color.WHITE);
        btnLaunchNursePortal.setPreferredSize(new Dimension(240, 40));
        btnLaunchNursePortal.addActionListener(e -> {
            new NurseDashboardSwing(null).setVisible(true);
        });

        bottomActionPanel.add(btnLaunchTelemedicine);
        bottomActionPanel.add(btnLaunchNursePortal);
        centerPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void toggleAvailability() {
        boolean selected = btnToggleAvailability.isSelected();
        String newStatus = selected ? "Online" : "Offline";
        String newAccept = selected ? "Yes" : "No";

        currentDoctor.setStatus(newStatus);
        currentDoctor.setAcceptAppointments(newAccept);

        // Update via JDBC PreparedStatements
        doctorDAO.updateAvailability(currentDoctor.getDoctorId(), newStatus, newAccept);

        btnToggleAvailability.setText(selected ? "ONLINE [ ON ]" : "OFFLINE [ OFF ]");
        btnToggleAvailability.setBackground(selected ? new Color(0, 200, 83) : new Color(220, 53, 69));

        lblStatusBadge.setText(newStatus + (selected ? " & Accepting" : ""));
        updateStatusBadgeColors();

        JOptionPane.showMessageDialog(this,
            "Availability status updated to: " + newStatus + "\nPatients can " + (selected ? "NOW" : "NO LONGER") + " book appointments with you.",
            "Status Synchronized", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatusBadgeColors() {
        boolean isOnline = "Online".equalsIgnoreCase(currentDoctor.getStatus());
        lblStatusBadge.setBackground(isOnline ? new Color(220, 247, 230) : new Color(254, 226, 226));
        lblStatusBadge.setForeground(isOnline ? new Color(6, 95, 70) : new Color(185, 28, 28));
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        List<Appointment> appts = appointmentDAO.getAppointmentsByDoctor(currentDoctor.getDoctorId());
        for (Appointment a : appts) {
            tableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientId(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getDepartment(),
                a.getPaymentStatus(),
                "Write Prescription"
            });
        }

        if (appts.isEmpty()) {
            tableModel.addRow(new Object[]{"TK-100842", "PT100842", "29 Jul 2026", "10:20 AM", currentDoctor.getDepartment(), "Paid ✓", "Write Prescription"});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DoctorDashboardSwing("DOC1001").setVisible(true));
    }
}
