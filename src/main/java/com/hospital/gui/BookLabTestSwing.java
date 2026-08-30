package com.hospital.gui;

import com.hospital.dao.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Niramaya Hospitals — Laboratory Test Booking Swing Interface.
 * Handles test selection, validation, JDBC database insertion into LabBookings,
 * debugging output, and automatic transition to Lab Payment page.
 */
public class BookLabTestSwing extends JFrame {

    private JTextField txtPatientId;
    private JTextField txtDoctorId;
    private JTextField txtBookingDate;
    private JComboBox<String> cmbTimeSlot;

    private JCheckBox cbCBC;
    private JCheckBox cbECG;
    private JCheckBox cbXRay;
    private JCheckBox cbMRI;
    private JCheckBox cbLipid;
    private JCheckBox cbThyroid;

    private JLabel lblTotalAmount;
    private JButton btnBookTest;

    public BookLabTestSwing() {
        this("PT100842", "DOC1001");
    }

    public BookLabTestSwing(String patientId, String doctorId) {
        setTitle("Niramaya Hospitals — Book Diagnostic Lab Test");
        setSize(650, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(13, 110, 253));
        headerPanel.setPreferredSize(new Dimension(650, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("🧪 NIRAMAYA HOSPITALS — BOOK DIAGNOSTIC LAB TEST");
        title.setFont(new Font("Sora", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Form Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(248, 250, 252));

        // Patient & Doctor Metadata Panel
        JPanel metaPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        metaPanel.setBorder(BorderFactory.createTitledBorder("Booking Credentials"));
        metaPanel.setOpaque(false);

        metaPanel.add(new JLabel("Patient ID:"));
        txtPatientId = new JTextField(patientId);
        metaPanel.add(txtPatientId);

        metaPanel.add(new JLabel("Doctor ID:"));
        txtDoctorId = new JTextField(doctorId);
        metaPanel.add(txtDoctorId);

        mainPanel.add(metaPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Prescribed Tests Checkboxes Panel
        JPanel testsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        testsPanel.setBorder(BorderFactory.createTitledBorder("Select Prescribed Diagnostic Tests"));
        testsPanel.setOpaque(false);

        cbCBC = new JCheckBox("Blood Count (CBC) - ₹500");
        cbCBC.setSelected(true);
        cbECG = new JCheckBox("Electrocardiogram (ECG) - ₹800");
        cbECG.setSelected(true);
        cbXRay = new JCheckBox("Digital Chest X-Ray - ₹700");
        cbMRI = new JCheckBox("Brain MRI Scan - ₹2,500");
        cbLipid = new JCheckBox("Lipid Profile - ₹950");
        cbThyroid = new JCheckBox("Thyroid Profile - ₹650");

        ActionListener totalListener = e -> updateCalculatedTotal();
        cbCBC.addActionListener(totalListener);
        cbECG.addActionListener(totalListener);
        cbXRay.addActionListener(totalListener);
        cbMRI.addActionListener(totalListener);
        cbLipid.addActionListener(totalListener);
        cbThyroid.addActionListener(totalListener);

        testsPanel.add(cbCBC);
        testsPanel.add(cbECG);
        testsPanel.add(cbXRay);
        testsPanel.add(cbMRI);
        testsPanel.add(cbLipid);
        testsPanel.add(cbThyroid);

        mainPanel.add(testsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Date & Time Slot Panel
        JPanel dateTimePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dateTimePanel.setBorder(BorderFactory.createTitledBorder("Schedule Date & Time Slot"));
        dateTimePanel.setOpaque(false);

        dateTimePanel.add(new JLabel("Booking Date (YYYY-MM-DD):"));
        txtBookingDate = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(System.currentTimeMillis() + 86400000L)));
        dateTimePanel.add(txtBookingDate);

        dateTimePanel.add(new JLabel("Preferred Time Slot:"));
        String[] slots = {"Select Time Slot", "Morning (08:00 AM - 11:30 AM)", "Afternoon (12:00 PM - 03:30 PM)", "Evening (04:00 PM - 07:00 PM)"};
        cmbTimeSlot = new JComboBox<>(slots);
        cmbTimeSlot.setSelectedIndex(1);
        dateTimePanel.add(cmbTimeSlot);

        mainPanel.add(dateTimePanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Total Amount Panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setOpaque(false);
        lblTotalAmount = new JLabel("Total Payable Fee: ₹1,300");
        lblTotalAmount.setFont(new Font("Sora", Font.BOLD, 15));
        lblTotalAmount.setForeground(new Color(13, 110, 253));
        totalPanel.add(lblTotalAmount);
        mainPanel.add(totalPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Footer Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actionPanel.setBackground(new Color(241, 245, 249));

        btnBookTest = new JButton("Book Test");
        btnBookTest.setFont(new Font("Inter", Font.BOLD, 14));
        btnBookTest.setBackground(new Color(0, 200, 83));
        btnBookTest.setForeground(Color.WHITE);
        btnBookTest.setPreferredSize(new Dimension(140, 40));

        // 1. Attach ActionListener to "Book Test" button
        btnBookTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // DEBUG OUTPUT REQUIREMENT
                System.out.println("Book Test Button Clicked");
                performBookTest();
            }
        });

        actionPanel.add(btnBookTest);
        add(actionPanel, BorderLayout.SOUTH);

        updateCalculatedTotal();
    }

    private double calculateTotalAmount() {
        double total = 0;
        if (cbCBC.isSelected()) total += 500;
        if (cbECG.isSelected()) total += 800;
        if (cbXRay.isSelected()) total += 700;
        if (cbMRI.isSelected()) total += 2500;
        if (cbLipid.isSelected()) total += 950;
        if (cbThyroid.isSelected()) total += 650;
        return total;
    }

    private void updateCalculatedTotal() {
        lblTotalAmount.setText("Total Payable Fee: ₹" + (int) calculateTotalAmount());
    }

    private List<String> getSelectedTests() {
        List<String> list = new ArrayList<>();
        if (cbCBC.isSelected()) list.add("Complete Blood Count (CBC)");
        if (cbECG.isSelected()) list.add("Electrocardiogram (ECG)");
        if (cbXRay.isSelected()) list.add("Digital Chest X-Ray");
        if (cbMRI.isSelected()) list.add("Brain MRI Scan");
        if (cbLipid.isSelected()) list.add("Lipid Profile");
        if (cbThyroid.isSelected()) list.add("Thyroid Profile");
        return list;
    }

    /**
     * 2. Booking method called when Book Test button is clicked.
     */
    private void performBookTest() {
        List<String> selectedTests = getSelectedTests();
        String date = txtBookingDate.getText().trim();
        String slot = (String) cmbTimeSlot.getSelectedItem();

        // 3. Validation
        if (selectedTests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Validation Failed: At least one diagnostic lab test must be selected.", "Booking Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Validation Failed: Please enter/select a preferred booking date.", "Booking Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (slot == null || slot.trim().isEmpty() || "Select Time Slot".equals(slot)) {
            JOptionPane.showMessageDialog(this, "Validation Failed: Please select a valid preferred time slot.", "Booking Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate unique Booking ID
        String bookingId = "LAB-" + (100000 + new Random().nextInt(900000));
        String pId = txtPatientId.getText().trim();
        String docId = txtDoctorId.getText().trim();
        String testNameStr = String.join(", ", selectedTests);
        double totalAmount = calculateTotalAmount();

        // DEBUG OUTPUT REQUIREMENT
        System.out.println("Saving Booking...");

        // Save into LabBookings table using Java JDBC and PreparedStatement
        String sql = "INSERT INTO LabBookings (booking_id, patient_id, doctor_id, prescription_id, test_name, booking_date, booking_time, status, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookingId);
            ps.setString(2, pId);
            ps.setString(3, docId);
            ps.setString(4, "RX-100842");
            ps.setString(5, testNameStr);
            ps.setString(6, date);
            ps.setString(7, slot);
            ps.setString(8, "Pending");
            ps.setString(9, "Unpaid");

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                // DEBUG OUTPUT REQUIREMENT
                System.out.println("Booking Saved Successfully");

                // AFTER SUCCESSFUL INSERT
                JOptionPane.showMessageDialog(this,
                        "Lab Test Booked Successfully!\n\nBooking ID: " + bookingId + "\nTest: " + testNameStr + "\nDate: " + date + " (" + slot + ")",
                        "Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);

                // Automatically navigate to Lab Payment page
                navigateToLabPaymentPage(bookingId, pId, docId, testNameStr, totalAmount);
            }
        } catch (Exception ex) {
            // IF DATABASE INSERT FAILS
            System.err.println("Database Insert Exception:");
            ex.printStackTrace(); // Print complete exception in console
            JOptionPane.showMessageDialog(this,
                    "SQL Error while inserting Lab Booking into MySQL Database:\n" + ex.getMessage(),
                    "Database Insertion Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Automatic transition to Lab Payment Page passing:
     * Booking ID, Patient ID, Doctor ID, Selected Test, Amount
     */
    private void navigateToLabPaymentPage(String bookingId, String patientId, String doctorId, String testName, double amount) {
        JFrame payFrame = new JFrame("Niramaya Hospitals — Laboratory Payment Console");
        payFrame.setSize(520, 420);
        payFrame.setLocationRelativeTo(this);
        payFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        payFrame.setLayout(new BorderLayout());

        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(new Color(15, 42, 74));
        pHeader.setPreferredSize(new Dimension(520, 60));
        pHeader.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("💳 LAB DIAGNOSTIC PAYMENT RECEIPT");
        lblTitle.setFont(new Font("Sora", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        pHeader.add(lblTitle, BorderLayout.WEST);
        payFrame.add(pHeader, BorderLayout.NORTH);

        JPanel pDetails = new JPanel(new GridLayout(6, 2, 8, 8));
        pDetails.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        pDetails.setBackground(Color.WHITE);

        pDetails.add(new JLabel("Booking ID:"));
        pDetails.add(new JLabel("<html><b>" + bookingId + "</b></html>"));

        pDetails.add(new JLabel("Patient ID:"));
        pDetails.add(new JLabel(patientId));

        pDetails.add(new JLabel("Prescribing Doctor ID:"));
        pDetails.add(new JLabel(doctorId));

        pDetails.add(new JLabel("Selected Test:"));
        pDetails.add(new JLabel("<html><body style='width: 220px'>" + testName + "</body></html>"));

        pDetails.add(new JLabel("Total Fee Payable:"));
        JLabel lblPay = new JLabel("₹" + (int) amount);
        lblPay.setFont(new Font("Sora", Font.BOLD, 15));
        lblPay.setForeground(new Color(0, 200, 83));
        pDetails.add(lblPay);

        pDetails.add(new JLabel("Payment Status:"));
        pDetails.add(new JLabel("Pending Payment"));

        payFrame.add(pDetails, BorderLayout.CENTER);

        JPanel pAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pAction.setBackground(new Color(245, 247, 250));

        JButton btnPayNow = new JButton("💳 Pay ₹" + (int) amount + " Now");
        btnPayNow.setFont(new Font("Inter", Font.BOLD, 13));
        btnPayNow.setBackground(new Color(13, 110, 253));
        btnPayNow.setForeground(Color.WHITE);

        btnPayNow.addActionListener(e -> {
            // Save payment via JDBC
            try (Connection conn = DBConnection.getConnection()) {
                String paySql = "INSERT INTO LabPayments (payment_id, booking_id, patient_id, amount, payment_method, transaction_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(paySql);
                String payId = "PAY-" + (100000 + new Random().nextInt(900000));
                ps.setString(1, payId);
                ps.setString(2, bookingId);
                ps.setString(3, patientId);
                ps.setDouble(4, amount);
                ps.setString(5, "UPI / Card");
                ps.setString(6, "TXN" + System.currentTimeMillis());
                ps.setString(7, "Paid");
                ps.executeUpdate();

                // Update booking status
                PreparedStatement ps2 = conn.prepareStatement("UPDATE LabBookings SET payment_status='Paid' WHERE booking_id=?");
                ps2.setString(1, bookingId);
                ps2.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(payFrame, "✓ Payment Received Successfully!\nReceipt ID: PAY-" + System.currentTimeMillis() + "\nStatus: Confirmed", "Payment Success", JOptionPane.INFORMATION_MESSAGE);
            payFrame.dispose();
        });

        pAction.add(btnPayNow);
        payFrame.add(pAction, BorderLayout.SOUTH);

        payFrame.setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookLabTestSwing().setVisible(true));
    }
}
