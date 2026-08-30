package com.hospital.gui;

import com.hospital.dao.PharmacyOrderDAO;
import com.hospital.dao.MedicineDAO;
import com.hospital.model.PharmacyOrder;
import com.hospital.model.PharmacyOrderItem;
import com.hospital.model.Medicine;
import com.hospital.service.PDFGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Java Swing GUI for Pharmacy Console in Niramaya Hospitals.
 * Provides full management for Pharmacy Orders, Tokens, Billing, Payments, and Dispensing.
 */
public class PharmacyDashboardSwing extends JFrame {

    private PharmacyOrderDAO orderDAO = new PharmacyOrderDAO();
    private MedicineDAO medicineDAO = new MedicineDAO();

    private JTable tblOrders;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public PharmacyDashboardSwing() {
        setTitle("Niramaya Hospitals — Pharmacy Management Console");
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 42, 74));
        headerPanel.setPreferredSize(new Dimension(1150, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("💊 NIRAMAYA HOSPITALS — PHARMACY & DISPENSING CONSOLE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setToolTipText("Search by Token or Patient ID");

        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.addActionListener(e -> loadOrders());

        JButton btnRefresh = new JButton("🔄 Refresh Orders");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadOrders();
        });

        headerRight.add(new JLabel("<html><b style='color:white;'>Filter: </b></html>"));
        headerRight.add(txtSearch);
        headerRight.add(btnSearch);
        headerRight.add(btnRefresh);

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"Pharmacy Token", "Order ID", "Patient ID", "Doctor ID", "Rx ID", "Amount (₹)", "Payment", "Order Status", "Order Date"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblOrders = new JTable(model);
        tblOrders.setRowHeight(38);
        tblOrders.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblOrders.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane sp = new JScrollPane(tblOrders);
        sp.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        add(sp, BorderLayout.CENTER);

        // South Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        actionPanel.setBackground(new Color(248, 250, 252));

        JButton btnViewItems = new JButton("📋 View Prescribed Medicines");
        btnViewItems.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnViewItems.setBackground(new Color(13, 110, 253));
        btnViewItems.setForeground(Color.WHITE);
        btnViewItems.addActionListener(e -> viewSelectedItems());

        JButton btnBillPay = new JButton("💳 Generate Bill & Process Payment");
        btnBillPay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBillPay.setBackground(new Color(0, 200, 83));
        btnBillPay.setForeground(Color.WHITE);
        btnBillPay.addActionListener(e -> processBillAndPayment());

        JButton btnDispense = new JButton("💊 Dispense Medicines");
        btnDispense.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDispense.setBackground(new Color(245, 158, 11));
        btnDispense.setForeground(Color.WHITE);
        btnDispense.addActionListener(e -> dispenseSelectedOrder());

        JButton btnInventory = new JButton("📦 View Medicine Inventory");
        btnInventory.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnInventory.addActionListener(e -> showInventoryCatalog());

        actionPanel.add(btnInventory);
        actionPanel.add(btnViewItems);
        actionPanel.add(btnBillPay);
        actionPanel.add(btnDispense);
        add(actionPanel, BorderLayout.SOUTH);

        loadOrders();
    }

    private void loadOrders() {
        model.setRowCount(0);
        String query = txtSearch.getText().trim().toLowerCase();
        List<PharmacyOrder> orders = orderDAO.getAllOrders();

        for (PharmacyOrder o : orders) {
            boolean matches = query.isEmpty()
                || (o.getPharmacyToken() != null && o.getPharmacyToken().toLowerCase().contains(query))
                || (o.getPatientId() != null && o.getPatientId().toLowerCase().contains(query))
                || (o.getOrderId() != null && o.getOrderId().toLowerCase().contains(query));

            if (matches) {
                model.addRow(new Object[]{
                    o.getPharmacyToken(),
                    o.getOrderId(),
                    o.getPatientId(),
                    o.getDoctorId(),
                    o.getPrescriptionId(),
                    String.format("₹%.2f", o.getTotalAmount()),
                    o.getPaymentStatus(),
                    o.getOrderStatus(),
                    o.getOrderDate()
                });
            }
        }
    }

    private void viewSelectedItems() {
        int r = tblOrders.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to view medicines.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String token = (String) model.getValueAt(r, 0);
        String orderId = (String) model.getValueAt(r, 1);
        List<PharmacyOrderItem> items = orderDAO.getOrderItems(orderId);

        StringBuilder sb = new StringBuilder();
        sb.append("📋 PRESCRIBED MEDICINES FOR TOKEN: ").append(token).append("\n");
        sb.append("=========================================================\n\n");

        if (items.isEmpty()) {
            // Previously this showed a hardcoded demo list (Paracetamol/Amoxicillin/Vitamin D3)
            // whenever the real items weren't found, which made it look like every order had
            // the same prescribed medicines regardless of what the doctor actually wrote.
            // Show an honest empty state instead so a missing-data bug is never mistaken
            // for a real (but wrong) prescription.
            sb.append("⚠ No medicine items found for this order.\n");
            sb.append("This usually means the order items were not saved correctly to the\n");
            sb.append("database when the prescription was processed. Please check the order\n");
            sb.append("creation logs (PharmacyOrderDAO) rather than assuming this list is accurate.\n");
        } else {
            int idx = 1;
            for (PharmacyOrderItem it : items) {
                sb.append(idx++).append(". ").append(it.getMedicineName()).append(" (").append(it.getStrength()).append(")\n");
                sb.append("   Dosage: ").append(it.getMorning()).append("-").append(it.getAfternoon()).append("-").append(it.getNight());
                sb.append(" | ").append(it.getDosage()).append(" | Duration: ").append(it.getDuration()).append("\n");
                sb.append("   Quantity: ").append(it.getQuantity()).append(" units | Unit Price: ₹").append(String.format("%.2f", it.getUnitPrice()));
                sb.append(" | Subtotal: ₹").append(String.format("%.2f", it.getSubtotal())).append("\n\n");
            }
        }

        JTextArea ta = new JTextArea(sb.toString(), 16, 50);
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Prescription Items — " + token, JOptionPane.INFORMATION_MESSAGE);
    }

    private void processBillAndPayment() {
        int r = tblOrders.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table to process payment.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String token = (String) model.getValueAt(r, 0);
        String orderId = (String) model.getValueAt(r, 1);
        String patientId = (String) model.getValueAt(r, 2);
        String doctorId = (String) model.getValueAt(r, 3);

        PharmacyOrder order = orderDAO.getOrderById(orderId);
        List<PharmacyOrderItem> items = orderDAO.getOrderItems(orderId);

        String[] options = {"UPI (GPay / PhonePe)", "Credit / Debit Card", "Cash at Desk", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Select Payment Method for Pharmacy Order " + token + "\nAmount: " + model.getValueAt(r, 5),
            "Process Pharmacy Bill & Payment",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice >= 0 && choice < 3) {
            String payMethod = options[choice].split(" ")[0];
            String txnId = "TXN" + System.currentTimeMillis();

            orderDAO.updatePayment(orderId, payMethod, txnId, "Paid");
            if (order != null) {
                PDFGenerator.generatePharmacyInvoicePDF(order, items, "Patient " + patientId, "Doctor " + doctorId);
            }

            JOptionPane.showMessageDialog(this, "✅ Payment Successfully Processed!\nPayment Method: " + payMethod + "\nTxn ID: " + txnId + "\nTax Invoice PDF Generated in Reports folder.", "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
            loadOrders();
        }
    }

    private void dispenseSelectedOrder() {
        int r = tblOrders.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to mark as Dispensed.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String token = (String) model.getValueAt(r, 0);
        String orderId = (String) model.getValueAt(r, 1);

        orderDAO.updateOrderStatus(orderId, "Dispensed");
        JOptionPane.showMessageDialog(this, "✅ Order " + token + " marked as DISPENSED & READY FOR PATIENT PICKUP!", "Dispense Complete", JOptionPane.INFORMATION_MESSAGE);
        loadOrders();
    }

    private void showInventoryCatalog() {
        List<Medicine> list = medicineDAO.getAllMedicines();
        String[] cols = {"Med ID", "Medicine Name", "Strength", "Unit Price (₹)", "Stock Qty", "Manufacturer", "Expiry Date"};
        DefaultTableModel invModel = new DefaultTableModel(cols, 0);

        for (Medicine m : list) {
            invModel.addRow(new Object[]{
                m.getMedicineId(),
                m.getMedicineName(),
                m.getStrength(),
                String.format("₹%.2f", m.getUnitPrice()),
                m.getStockQuantity(),
                m.getManufacturer(),
                m.getExpiryDate()
            });
        }

        JTable tblInv = new JTable(invModel);
        tblInv.setRowHeight(32);
        tblInv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblInv.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(tblInv);
        sp.setPreferredSize(new Dimension(850, 400));
        JOptionPane.showMessageDialog(this, sp, "Niramaya Central Pharmacy Inventory Catalog", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PharmacyDashboardSwing().setVisible(true));
    }
}
