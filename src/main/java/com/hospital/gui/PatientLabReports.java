package com.hospital.gui;

import com.hospital.dao.LabReportDAO;
import com.hospital.model.LabReport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Swing view for Patient Lab Reports — filtered strictly by Patient ID.
 */
public class PatientLabReports extends JPanel {

    private final String patientId;
    private final LabReportDAO dao = new LabReportDAO();
    private final DefaultTableModel tableModel;

    public PatientLabReports(String patientId) {
        this.patientId = patientId != null ? patientId : "PT100842";
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("📄 My Digital Laboratory Reports — Patient ID: " + this.patientId);
        title.setFont(new Font("Sora", Font.BOLD, 18));
        title.setForeground(new Color(6, 48, 107));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Report ID", "Test Name", "Recommending Doctor", "Report Date", "Status", "Actions"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadReports();
    }

    public void loadReports() {
        tableModel.setRowCount(0);
        List<LabReport> list = dao.getReportsByPatient(patientId);
        for (LabReport r : list) {
            tableModel.addRow(new Object[]{r.getReportId(), r.getTestName(), r.getDoctorName(), r.getReportDate(), r.getStatus(), "View / Download PDF"});
        }
    }
}
