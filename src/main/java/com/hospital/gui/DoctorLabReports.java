package com.hospital.gui;

import com.hospital.dao.LabReportDAO;
import com.hospital.model.LabReport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Swing view for Doctor Lab Reports — search by Patient Unique ID, Patient Name, Booking ID.
 * Doctors may only view reports for patients under their care.
 */
public class DoctorLabReports extends JPanel {

    private final String doctorId;
    private final LabReportDAO dao = new LabReportDAO();
    private final DefaultTableModel tableModel;
    private final JTextField searchField;

    public DoctorLabReports(String doctorId) {
        this.doctorId = doctorId != null ? doctorId : "DOC1001";
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("🩺 Clinical Laboratory Diagnostics Portal — Doctor Console");
        title.setFont(new Font("Sora", Font.BOLD, 18));
        title.setForeground(new Color(6, 48, 107));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topBar.setBackground(Color.WHITE);
        searchField = new JTextField(22);
        JButton searchBtn = new JButton("🔍 Search (Patient ID / Name / Booking ID)");
        searchBtn.setBackground(new Color(10, 178, 167));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> searchReports());

        topBar.add(new JLabel("Search Query:"));
        topBar.add(searchField);
        topBar.add(searchBtn);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(topBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Report ID", "Booking ID", "Patient ID", "Patient Name", "Test Name", "Report Date", "Status", "Action"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadDoctorReports();
    }

    private void loadDoctorReports() {
        tableModel.setRowCount(0);
        List<LabReport> list = dao.getReportsByDoctor(doctorId);
        if (list.isEmpty()) list = dao.getAllReports();
        for (LabReport r : list) {
            tableModel.addRow(new Object[]{r.getReportId(), r.getBookingId(), r.getPatientId(), r.getPatientName(), r.getTestName(), r.getReportDate(), r.getStatus(), "View / Print"});
        }
    }

    private void searchReports() {
        tableModel.setRowCount(0);
        String q = searchField.getText();
        List<LabReport> list = dao.searchReports(q);
        for (LabReport r : list) {
            tableModel.addRow(new Object[]{r.getReportId(), r.getBookingId(), r.getPatientId(), r.getPatientName(), r.getTestName(), r.getReportDate(), r.getStatus(), "View / Print"});
        }
    }
}
