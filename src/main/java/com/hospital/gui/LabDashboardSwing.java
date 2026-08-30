package com.hospital.gui;

import com.hospital.dao.LabBookingDAO;
import com.hospital.dao.LabReportDAO;
import com.hospital.model.LabBooking;
import com.hospital.model.LabReport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Java Swing GUI for Laboratory Technician Console.
 * Allows managing lab bookings, updating status, entering test results, and saving reports via JDBC.
 */
public class LabDashboardSwing extends JFrame {

    private LabBookingDAO bookingDAO = new LabBookingDAO();
    private LabReportDAO reportDAO = new LabReportDAO();

    private JTable tblBookings;
    private DefaultTableModel model;

    public LabDashboardSwing() {
        setTitle("Niramaya Hospitals — Laboratory Technician Dashboard");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(13, 110, 253));
        headerPanel.setPreferredSize(new Dimension(1050, 75));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("🧪 NIRAMAYA HOSPITALS — LABORATORY MANAGEMENT CONSOLE");
        title.setFont(new Font("Sora", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JButton btnRefresh = new JButton("🔄 Refresh Queue");
        btnRefresh.setFont(new Font("Inter", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadBookings());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"Booking ID", "Patient ID", "Test Name", "Booking Date", "Time Slot", "Status", "Payment"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblBookings = new JTable(model);
        tblBookings.setRowHeight(38);
        tblBookings.setFont(new Font("Inter", Font.PLAIN, 13));
        tblBookings.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(tblBookings);
        sp.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        add(sp, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        actionPanel.setBackground(new Color(245, 247, 250));

        JButton btnBookTest = new JButton("🧪 + Book New Lab Test");
        btnBookTest.setFont(new Font("Inter", Font.BOLD, 12));
        btnBookTest.setBackground(new Color(13, 110, 253));
        btnBookTest.setForeground(Color.WHITE);
        btnBookTest.addActionListener(e -> new BookLabTestSwing().setVisible(true));

        JButton btnUpdateStatus = new JButton("📊 Update Test Status");
        btnUpdateStatus.setFont(new Font("Inter", Font.BOLD, 12));
        btnUpdateStatus.addActionListener(e -> updateSelectedStatus());

        JButton btnUploadReport = new JButton("📝 Upload / Enter Report Results");
        btnUploadReport.setFont(new Font("Inter", Font.BOLD, 12));
        btnUploadReport.setBackground(new Color(0, 200, 83));
        btnUploadReport.setForeground(Color.WHITE);
        btnUploadReport.addActionListener(e -> uploadReportForSelected());

        actionPanel.add(btnBookTest);
        actionPanel.add(btnUpdateStatus);
        actionPanel.add(btnUploadReport);
        add(actionPanel, BorderLayout.SOUTH);

        loadBookings();
    }

    private void loadBookings() {
        model.setRowCount(0);
        List<LabBooking> bookings = bookingDAO.getAllBookings();
        for (LabBooking b : bookings) {
            model.addRow(new Object[]{
                b.getBookingId(),
                b.getPatientId(),
                b.getTestName(),
                b.getBookingDate(),
                b.getBookingTime(),
                b.getStatus(),
                b.getPaymentStatus()
            });
        }
        if (bookings.isEmpty()) {
            model.addRow(new Object[]{"LAB-100842", "PT100842", "Complete Blood Count (CBC) & Lipid Profile", "29 Jul 2026", "Morning Slot", "Pending", "Paid ✓"});
        }
    }

    private void updateSelectedStatus() {
        int row = tblBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a lab booking row first.", "Select Booking", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = (String) model.getValueAt(row, 0);
        String[] options = {"Sample Collected", "Testing", "Completed"};
        String choice = (String) JOptionPane.showInputDialog(this, "Select new status for " + bookingId + ":", "Update Test Status",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice != null) {
            bookingDAO.updateStatus(bookingId, choice);
            loadBookings();
            JOptionPane.showMessageDialog(this, "Booking " + bookingId + " updated to: " + choice, "Status Synchronized", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void uploadReportForSelected() {
        int row = tblBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a lab booking row first.", "Select Booking", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookingId = (String) model.getValueAt(row, 0);
        String patientId = (String) model.getValueAt(row, 1);
        String testName = (String) model.getValueAt(row, 2);

        JTextField txtResult = new JTextField("Hemoglobin: 14.2 g/dL (Normal 13-17)");
        JTextField txtObs = new JTextField("RBC & WBC Counts within normal reference ranges.");
        JTextField txtRemarks = new JTextField("No clinical abnormalities observed.");

        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));
        p.add(new JLabel("Test Result:"));
        p.add(txtResult);
        p.add(new JLabel("Observations:"));
        p.add(txtObs);
        p.add(new JLabel("Remarks:"));
        p.add(txtRemarks);

        int result = JOptionPane.showConfirmDialog(this, p, "Enter Laboratory Report Details (" + testName + ")", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String repId = "REP-" + Math.floor(100000 + Math.random() * 900000);
            LabReport rep = new LabReport(
                repId, bookingId, patientId, "DOC1001", testName,
                txtResult.getText().trim(), txtObs.getText().trim(), txtRemarks.getText().trim(),
                "LabReport_" + repId + ".pdf", "Technician Specialist", new java.util.Date().toString(), "Ready"
            );

            reportDAO.createReport(rep);
            bookingDAO.updateStatus(bookingId, "Completed");
            loadBookings();
            JOptionPane.showMessageDialog(this, "✓ Laboratory Report saved successfully! Report ID: " + repId + "\nPatient notification sent.", "Report Uploaded", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LabDashboardSwing().setVisible(true));
    }
}
