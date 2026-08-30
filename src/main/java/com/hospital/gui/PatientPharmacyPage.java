package com.hospital.gui;

import com.hospital.dao.PharmacyOrderDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.PharmacyOrder;
import com.hospital.model.Prescription;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Swing view for Patient Pharmacy Module.
 * Fulfills the requirement for PatientPharmacyPage.java
 */
public class PatientPharmacyPage extends JPanel {

    private final String patientId;
    private final PharmacyOrderDAO orderDAO = new PharmacyOrderDAO();
    private final PrescriptionDAO rxDAO = new PrescriptionDAO();
    
    private final DefaultTableModel rxTableModel;
    private final DefaultTableModel orderTableModel;
    private final DefaultTableModel invoiceTableModel;

    public PatientPharmacyPage(String patientId) {
        this.patientId = patientId != null && !patientId.isEmpty() ? patientId : "PT100842";
        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("💊 My Pharmacy Dashboard — Patient ID: " + this.patientId);
        title.setFont(new Font("Sora", Font.BOLD, 22));
        title.setForeground(new Color(6, 48, 107));
        headerPanel.add(title, BorderLayout.WEST);
        
        JButton btnRefresh = new JButton("🔄 Refresh Data");
        btnRefresh.setBackground(new Color(10, 178, 167));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadData());
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // TABBED PANE FOR SECTIONS
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // 1. ACTIVE PRESCRIPTIONS TAB
        String[] rxCols = {"Prescription ID", "Doctor Name", "Date", "Pharmacy Token", "Total Medicines", "Status"};
        rxTableModel = new DefaultTableModel(rxCols, 0);
        JTable rxTable = createTable(rxTableModel);
        tabbedPane.addTab("Active Prescriptions", createTabPanel("Active Prescriptions", rxTable));

        // 2. PHARMACY ORDERS TAB
        String[] orderCols = {"Pharmacy Token", "Prescription ID", "Order Date", "Bill Amount", "Payment Status", "Order Status"};
        orderTableModel = new DefaultTableModel(orderCols, 0);
        JTable orderTable = createTable(orderTableModel);
        tabbedPane.addTab("Pharmacy Orders", createTabPanel("Pharmacy Orders", orderTable));

        // 3. INVOICE HISTORY TAB
        String[] invCols = {"Invoice Number", "Pharmacy Token", "Date", "Amount", "Status"};
        invoiceTableModel = new DefaultTableModel(invCols, 0);
        JTable invTable = createTable(invoiceTableModel);
        tabbedPane.addTab("Invoice History", createTabPanel("Paid Pharmacy Invoices", invTable));

        add(tabbedPane, BorderLayout.CENTER);

        loadData();
    }
    
    private JPanel createTabPanel(String titleStr, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel title = new JLabel(titleStr);
        title.setFont(new Font("Sora", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        panel.add(title, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    
    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setGridColor(new Color(226, 232, 240));
        return table;
    }

    public void loadData() {
        rxTableModel.setRowCount(0);
        orderTableModel.setRowCount(0);
        invoiceTableModel.setRowCount(0);

        try {
            // Load Prescriptions
            List<Prescription> rxList = rxDAO.getPrescriptionsByPatient(patientId);
            for (Prescription r : rxList) {
                rxTableModel.addRow(new Object[]{
                        r.getPrescriptionId(),
                        r.getDoctorId(),
                        r.getCreatedDate(),
                        "Check Orders",
                        "View Details",
                        "Saved"
                });
            }

            // Load Pharmacy Orders
            List<PharmacyOrder> orderList = orderDAO.getOrdersByPatient(patientId);
            for (PharmacyOrder o : orderList) {
                orderTableModel.addRow(new Object[]{
                        o.getPharmacyToken(),
                        o.getPrescriptionId(),
                        o.getOrderDate(),
                        "₹" + String.format("%.2f", o.getTotalAmount()),
                        o.getPaymentStatus(),
                        o.getOrderStatus()
                });
                
                // If Paid, add to Invoices
                if ("Paid".equalsIgnoreCase(o.getPaymentStatus())) {
                    invoiceTableModel.addRow(new Object[]{
                            "INV-" + o.getOrderId(),
                            o.getPharmacyToken(),
                            o.getOrderDate(),
                            "₹" + String.format("%.2f", o.getTotalAmount()),
                            "Paid"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading pharmacy data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
