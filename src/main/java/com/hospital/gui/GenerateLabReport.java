package com.hospital.gui;

import com.hospital.dao.LabBookingDAO;
import com.hospital.dao.LabReportDAO;
import com.hospital.model.LabBooking;
import com.hospital.model.LabReport;
import com.hospital.service.PDFGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Swing Frame for Laboratory Report Generation.
 * Displays read-only patient & test metadata, embeds DynamicReportForm,
 * provides action buttons: Preview Report, Generate PDF, Save Report, Clear, Cancel.
 */
public class GenerateLabReport extends JFrame {

    private final LabBooking booking;
    private final JTextField txtPatientId;
    private final JTextField txtPatientName;
    private final JTextField txtAge;
    private final JTextField txtGender;
    private final JTextField txtDoctorName;
    private final JTextField txtDepartment;
    private final JTextField txtTestName;
    private final JTextField txtBookingId;
    private final JTextField txtReportDate;

    private DynamicReportForm dynamicForm;
    private final LabReportDAO labReportDAO = new LabReportDAO();
    private final LabBookingDAO labBookingDAO = new LabBookingDAO();

    public GenerateLabReport(LabBooking booking) {
        this.booking = booking != null ? booking : new LabBooking("LAB-100842", "PT100842", "DOC1001", "RX-100842", "Complete Blood Count (CBC)", "2026-07-30", "Morning", "Pending", "Paid", new Date().toString());

        setTitle("Niramaya Hospitals — Digital Laboratory Report Generator");
        setSize(850, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(6, 48, 107));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("🧪 Generate Clinical Laboratory Report");
        titleLbl.setFont(new Font("Sora", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Niramaya Hospitals Diagnostics & Pathology Engine");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(95, 214, 204));

        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(subLbl, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Read-only Metadata Grid
        JPanel metaPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        metaPanel.setBackground(new Color(245, 248, 251));
        metaPanel.setBorder(BorderFactory.createTitledBorder("Patient & Booking Information (Read-Only)"));

        txtPatientId = createReadOnlyField(this.booking.getPatientId());
        txtPatientName = createReadOnlyField("Rekha Prasad");
        txtAge = createReadOnlyField("28 Yrs");
        txtGender = createReadOnlyField("Female");
        txtDoctorName = createReadOnlyField("Dr. Ananya Rao");
        txtDepartment = createReadOnlyField("Cardiology");
        txtTestName = createReadOnlyField(this.booking.getTestName());
        txtBookingId = createReadOnlyField(this.booking.getBookingId());
        txtReportDate = createReadOnlyField(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));

        metaPanel.add(new JLabel("Patient Unique ID:")); metaPanel.add(txtPatientId);
        metaPanel.add(new JLabel("Patient Name:")); metaPanel.add(txtPatientName);
        metaPanel.add(new JLabel("Age / Gender:")); metaPanel.add(txtAge);
        metaPanel.add(new JLabel("Doctor Name:")); metaPanel.add(txtDoctorName);
        metaPanel.add(new JLabel("Department:")); metaPanel.add(txtDepartment);
        metaPanel.add(new JLabel("Test Name:")); metaPanel.add(txtTestName);
        metaPanel.add(new JLabel("Booking ID:")); metaPanel.add(txtBookingId);
        metaPanel.add(new JLabel("Report Date:")); metaPanel.add(txtReportDate);

        // Dynamic Form
        dynamicForm = new DynamicReportForm(this.booking.getTestName());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(metaPanel, BorderLayout.NORTH);
        centerPanel.add(dynamicForm, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        btnPanel.setBackground(Color.WHITE);

        JButton btnPreview = new JButton("👁️ Preview Report");
        JButton btnGeneratePDF = new JButton("📄 Generate PDF");
        JButton btnSave = new JButton("💾 Save Report");
        JButton btnClear = new JButton("🧹 Clear");
        JButton btnCancel = new JButton("✕ Cancel");

        btnPreview.setBackground(new Color(11, 95, 203)); btnPreview.setForeground(Color.WHITE);
        btnGeneratePDF.setBackground(new Color(10, 178, 167)); btnGeneratePDF.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(0, 200, 83)); btnSave.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(226, 232, 240));
        btnCancel.setBackground(new Color(225, 91, 91)); btnCancel.setForeground(Color.WHITE);

        btnPreview.addActionListener(e -> previewReport());
        btnGeneratePDF.addActionListener(e -> generatePDFOnly());
        btnSave.addActionListener(e -> saveReportAction());
        btnClear.addActionListener(e -> dynamicForm.setTestName(this.booking.getTestName()));
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnPreview);
        btnPanel.add(btnGeneratePDF);
        btnPanel.add(btnSave);
        btnPanel.add(btnClear);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private JTextField createReadOnlyField(String text) {
        JTextField tf = new JTextField(text != null ? text : "");
        tf.setEditable(false);
        tf.setBackground(new Color(240, 244, 248));
        tf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return tf;
    }

    private void previewReport() {
        String html = PDFGenerator.buildReportHTML(buildReportModel(), dynamicForm.buildHtmlTable());
        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);
        JDialog dialog = new JDialog(this, "Lab Report Preview — Niramaya Hospitals", true);
        dialog.setSize(750, 650);
        dialog.setLocationRelativeTo(this);
        dialog.add(new JScrollPane(editorPane));
        dialog.setVisible(true);
    }

    private void generatePDFOnly() {
        LabReport report = buildReportModel();
        File pdfFile = PDFGenerator.generateReportPDF(report, dynamicForm.buildHtmlTable());
        if (pdfFile != null) {
            JOptionPane.showMessageDialog(this, "✓ PDF Laboratory Report generated successfully!\nPath: " + pdfFile.getAbsolutePath(), "PDF Generated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveReportAction() {
        Map<String, String> formData = dynamicForm.getFormData();
        if (formData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter mandatory diagnostic parameters before saving.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LabReport report = buildReportModel();
        boolean saved = labReportDAO.createReport(report);
        labBookingDAO.updateStatus(booking.getBookingId(), "Completed");

        // Automatically Generate PDF file
        File pdf = PDFGenerator.generateReportPDF(report, dynamicForm.buildHtmlTable());

        if (saved || pdf != null) {
            JOptionPane.showMessageDialog(this, "✓ Report saved successfully into MySQL Database!\nReport ID: " + report.getReportId() + "\nPDF Path: Reports/" + pdf.getName(), "Report Saved", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error saving report into database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LabReport buildReportModel() {
        String reportId = "REP-" + (100000 + (int)(Math.random() * 900000));
        LabReport report = new LabReport();
        report.setReportId(reportId);
        report.setBookingId(booking.getBookingId());
        report.setPatientId(booking.getPatientId());
        report.setPatientName(txtPatientName.getText());
        report.setPatientAge(txtAge.getText());
        report.setPatientGender(txtGender.getText());
        report.setDoctorId(booking.getDoctorId());
        report.setDoctorName(txtDoctorName.getText());
        report.setDepartment(txtDepartment.getText());
        report.setTestName(booking.getTestName());
        report.setResult("Normal Clinical Limits");
        report.setObservation("Observed parameters are within normal physiological diagnostic ranges.");
        report.setRemarks("Verified by Niramaya Pathologist.");
        report.setReportFile("LabReport_" + booking.getPatientId() + "_" + reportId + ".pdf");
        report.setUploadedBy("Senior Lab Technologist");
        report.setVerifiedBy("Dr. Ananya Rao (Chief Pathologist)");
        report.setReportDate(txtReportDate.getText());
        report.setStatus("Ready");
        return report;
    }
}
