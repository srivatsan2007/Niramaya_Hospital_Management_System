package com.hospital.gui;

import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Dashboard GUI in Java Swing for Niramaya Hospitals.
 * Allows hospital administrators to monitor all doctor statuses and enable/disable availability.
 */
public class AdminDashboardSwing extends JFrame {

    private DoctorDAO doctorDAO = new DoctorDAO();
    private JTable tblDoctors;
    private DefaultTableModel model;

    public AdminDashboardSwing() {
        setTitle("Niramaya Hospitals — Admin Doctor Monitoring Console");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 42, 74));
        headerPanel.setPreferredSize(new Dimension(950, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("NIRAMAYA HOSPITALS — ADMIN CONTROL PANEL");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JButton btnRefresh = new JButton("🔄 Refresh Doctors List");
        btnRefresh.setFont(new Font("Inter", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadDoctors());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel with Doctors Table
        String[] cols = {"Doctor ID", "Doctor Name", "Department", "Online Status", "Accepting Appts", "Consultation Fee", "Action"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        tblDoctors = new JTable(model);
        tblDoctors.setRowHeight(40);
        tblDoctors.setFont(new Font("Inter", Font.PLAIN, 13));
        tblDoctors.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(tblDoctors);
        sp.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(sp, BorderLayout.CENTER);

        // Control Panel
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        southPanel.setBackground(new Color(245, 247, 250));

        JButton btnToggleSelected = new JButton("⚡ Toggle Selected Doctor Availability");
        btnToggleSelected.setFont(new Font("Inter", Font.BOLD, 13));
        btnToggleSelected.setBackground(new Color(13, 110, 253));
        btnToggleSelected.setForeground(Color.WHITE);
        btnToggleSelected.addActionListener(e -> toggleSelectedDoctor());

        southPanel.add(btnToggleSelected);
        add(southPanel, BorderLayout.SOUTH);

        loadDoctors();
    }

    private void loadDoctors() {
        model.setRowCount(0);
        List<Doctor> docs = doctorDAO.getAllDoctors();
        for (Doctor d : docs) {
            model.addRow(new Object[]{
                d.getDoctorId(),
                d.getDoctorName(),
                d.getDepartment(),
                d.getStatus(),
                d.getAcceptAppointments(),
                "₹" + d.getConsultationFee(),
                "Toggle Availability"
            });
        }
    }

    private void toggleSelectedDoctor() {
        int row = tblDoctors.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the table first.", "Select Doctor", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String docId = (String) model.getValueAt(row, 0);
        String currentStatus = (String) model.getValueAt(row, 3);

        boolean isOnline = "Online".equalsIgnoreCase(currentStatus);
        String nextStatus = isOnline ? "Offline" : "Online";
        String nextAccept = isOnline ? "No" : "Yes";

        doctorDAO.updateAvailability(docId, nextStatus, nextAccept);
        loadDoctors();
        JOptionPane.showMessageDialog(this, "Doctor " + docId + " status toggled to: " + nextStatus, "Admin Action Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboardSwing().setVisible(true));
    }
}
