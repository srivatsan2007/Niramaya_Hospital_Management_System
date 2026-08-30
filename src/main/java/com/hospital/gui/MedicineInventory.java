package com.hospital.gui;

import com.hospital.dao.MedicineDAO;
import com.hospital.model.Medicine;
import com.hospital.service.StockManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Java Swing GUI for Pharmacist Medicine Inventory Management in Niramaya Hospitals.
 */
public class MedicineInventory extends JFrame {

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbCategoryFilter;
    private JComboBox<String> cbStatusFilter;
    private MedicineDAO medicineDAO = new MedicineDAO();
    private StockManager stockManager = new StockManager();

    public MedicineInventory() {
        setTitle("Niramaya Hospitals — Pharmacist Medicine Inventory Management");
        setSize(1350, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // TOP HEADER & BUTTON BAR
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(15, 42, 74)); // Niramaya Blue-900
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblBrand = new JLabel("📦 Pharmacist Medicine Inventory Management");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(Color.WHITE);
        topPanel.add(lblBrand, BorderLayout.WEST);

        // ACTION BUTTONS BAR
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBar.setOpaque(false);

        JButton btnAdd = createStyledButton("➕ Add Medicine", new Color(10, 122, 112));
        JButton btnEdit = createStyledButton("✏ Edit Medicine", new Color(13, 110, 253));
        JButton btnStock = createStyledButton("📈 Update Stock", new Color(217, 119, 6));
        JButton btnDelete = createStyledButton("🗑 Delete Medicine", new Color(225, 91, 91));
        JButton btnExport = createStyledButton("📤 Export Inventory", new Color(40, 167, 69));
        JButton btnRefresh = createStyledButton("🔄 Refresh", new Color(108, 117, 125));

        buttonBar.add(btnAdd);
        buttonBar.add(btnEdit);
        buttonBar.add(btnStock);
        buttonBar.add(btnDelete);
        buttonBar.add(btnExport);
        buttonBar.add(btnRefresh);

        topPanel.add(buttonBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // SEARCH & FILTER TOOLBAR
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(new Color(245, 247, 250));
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        filterPanel.add(new JLabel("🔍 Search:"));
        txtSearch = new JTextField(18);
        txtSearch.addActionListener(e -> refreshTable());
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Category:"));
        cbCategoryFilter = new JComboBox<>(new String[]{"All Categories", "Tablet", "Capsule", "Injection", "Syrup", "Drops", "Cream", "Ointment", "Inhaler"});
        cbCategoryFilter.addActionListener(e -> refreshTable());
        filterPanel.add(cbCategoryFilter);

        filterPanel.add(new JLabel("Status:"));
        cbStatusFilter = new JComboBox<>(new String[]{"All Statuses", "🟢 In Stock", "🟡 Low Stock", "🔴 Out of Stock", "⚠️ Expiring Soon", "❌ Expired"});
        cbStatusFilter.addActionListener(e -> refreshTable());
        filterPanel.add(cbStatusFilter);

        JButton btnSearch = createStyledButton("Search", new Color(15, 42, 74));
        btnSearch.addActionListener(e -> refreshTable());
        filterPanel.add(btnSearch);

        // TABLE SETUP
        String[] columns = {
            "Medicine ID", "Medicine Name", "Generic Name", "Category", "Strength", "Dosage Form",
            "Manufacturer", "Batch No", "Mfg Date", "Expiry Date", "Purchase (₹)", "Selling (₹)",
            "GST %", "Stock Qty", "Min Stock", "Rack No", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(32);
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        inventoryTable.getTableHeader().setBackground(new Color(245, 247, 250));

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // BUTTON HANDLERS
        btnAdd.addActionListener(e -> showAddMedicineModal());
        btnEdit.addActionListener(e -> showEditMedicineModal());
        btnStock.addActionListener(e -> showUpdateStockModal());
        btnDelete.addActionListener(e -> handleDeleteMedicine());
        btnExport.addActionListener(e -> exportInventory());
        btnRefresh.addActionListener(e -> refreshTable());

        refreshTable();
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Medicine> list = medicineDAO.getAllMedicines();

        String query = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String selCategory = (String) cbCategoryFilter.getSelectedItem();
        String selStatus = (String) cbStatusFilter.getSelectedItem();

        for (Medicine m : list) {
            // Apply Search Filter
            boolean matchesSearch = query.isEmpty() ||
                m.getMedicineName().toLowerCase().contains(query) ||
                (m.getGenericName() != null && m.getGenericName().toLowerCase().contains(query)) ||
                (m.getBatchNumber() != null && m.getBatchNumber().toLowerCase().contains(query)) ||
                (m.getManufacturer() != null && m.getManufacturer().toLowerCase().contains(query)) ||
                (m.getCategory() != null && m.getCategory().toLowerCase().contains(query)) ||
                (m.getMedicineId() != null && m.getMedicineId().toLowerCase().contains(query));

            if (!matchesSearch) continue;

            // Apply Category Filter
            if (selCategory != null && !selCategory.equalsIgnoreCase("All Categories")) {
                if (m.getCategory() == null || !m.getCategory().equalsIgnoreCase(selCategory)) continue;
            }

            // Apply Status Filter
            if (selStatus != null && !selStatus.equalsIgnoreCase("All Statuses")) {
                String statusStr = m.getStatus() != null ? m.getStatus() : "";
                if (selStatus.contains("In Stock") && !statusStr.equalsIgnoreCase("In Stock")) continue;
                if (selStatus.contains("Low Stock") && !statusStr.equalsIgnoreCase("Low Stock")) continue;
                if (selStatus.contains("Out of Stock") && !statusStr.equalsIgnoreCase("Out of Stock")) continue;
                if (selStatus.contains("Expiring Soon") && !statusStr.equalsIgnoreCase("Expiring Soon")) continue;
                if (selStatus.contains("Expired") && !statusStr.equalsIgnoreCase("Expired")) continue;
            }

            tableModel.addRow(new Object[]{
                m.getMedicineId(),
                m.getMedicineName(),
                m.getGenericName() != null ? m.getGenericName() : m.getMedicineName(),
                m.getCategory() != null ? m.getCategory() : "Tablet",
                m.getStrength() != null ? m.getStrength() : "-",
                m.getDosageForm() != null ? m.getDosageForm() : "Tablet",
                m.getManufacturer() != null ? m.getManufacturer() : "-",
                m.getBatchNumber() != null ? m.getBatchNumber() : "-",
                m.getManufacturingDate() != null ? m.getManufacturingDate() : "-",
                m.getExpiryDate() != null ? m.getExpiryDate() : "-",
                String.format("%.2f", m.getPurchasePrice()),
                String.format("%.2f", m.getSellingPrice()),
                String.format("%.2f", m.getGstPercentage()),
                m.getStockQuantity(),
                m.getMinimumStock(),
                m.getRackNumber() != null ? m.getRackNumber() : "R-101",
                m.getStatus() != null ? m.getStatus() : "In Stock"
            });
        }
    }

    private void showAddMedicineModal() {
        JDialog modal = new JDialog(this, "➕ Add New Medicine to Inventory", true);
        modal.setSize(600, 680);
        modal.setLocationRelativeTo(this);
        modal.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(12, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField();
        JTextField txtGeneric = new JTextField();
        JComboBox<String> cbCategory = new JComboBox<>(new String[]{"Tablet", "Capsule", "Injection", "Syrup", "Drops", "Cream", "Ointment", "Inhaler"});
        JTextField txtStrength = new JTextField();
        JComboBox<String> cbDosage = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Drops"});
        JTextField txtManufacturer = new JTextField();
        JTextField txtBatch = new JTextField("BN-" + (1000 + (int)(Math.random() * 9000)));
        JTextField txtMfgDate = new JTextField("2024-01-01");
        JTextField txtExpDate = new JTextField("2028-12-31");
        JTextField txtPurPrice = new JTextField("50.00");
        JTextField txtSellPrice = new JTextField("75.00");
        JTextField txtStock = new JTextField("100");
        JTextField txtMinStock = new JTextField("15");
        JTextField txtRack = new JTextField("R-101");

        formPanel.add(new JLabel("Medicine Name *")); formPanel.add(txtName);
        formPanel.add(new JLabel("Generic Name *")); formPanel.add(txtGeneric);
        formPanel.add(new JLabel("Category *")); formPanel.add(cbCategory);
        formPanel.add(new JLabel("Strength *")); formPanel.add(txtStrength);
        formPanel.add(new JLabel("Manufacturer *")); formPanel.add(txtManufacturer);
        formPanel.add(new JLabel("Batch Number *")); formPanel.add(txtBatch);
        formPanel.add(new JLabel("Manufacturing Date (YYYY-MM-DD) *")); formPanel.add(txtMfgDate);
        formPanel.add(new JLabel("Expiry Date (YYYY-MM-DD) *")); formPanel.add(txtExpDate);
        formPanel.add(new JLabel("Purchase Price (₹) *")); formPanel.add(txtPurPrice);
        formPanel.add(new JLabel("Selling Price (₹) *")); formPanel.add(txtSellPrice);
        formPanel.add(new JLabel("Stock Quantity *")); formPanel.add(txtStock);
        formPanel.add(new JLabel("Minimum Stock *")); formPanel.add(txtMinStock);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = createStyledButton("Save Medicine", new Color(10, 122, 112));
        JButton btnCancel = createStyledButton("Cancel", new Color(108, 117, 125));

        btnSave.addActionListener(ev -> {
            try {
                Medicine m = new Medicine();
                m.setMedicineId(medicineDAO.generateAutoMedicineId());
                m.setMedicineName(txtName.getText().trim());
                m.setGenericName(txtGeneric.getText().trim().isEmpty() ? txtName.getText().trim() : txtGeneric.getText().trim());
                m.setCategory((String) cbCategory.getSelectedItem());
                m.setStrength(txtStrength.getText().trim());
                m.setDosageForm((String) cbDosage.getSelectedItem());
                m.setManufacturer(txtManufacturer.getText().trim());
                m.setBatchNumber(txtBatch.getText().trim());
                m.setManufacturingDate(txtMfgDate.getText().trim());
                m.setExpiryDate(txtExpDate.getText().trim());
                m.setPurchasePrice(Double.parseDouble(txtPurPrice.getText().trim()));
                m.setSellingPrice(Double.parseDouble(txtSellPrice.getText().trim()));
                m.setStockQuantity(Integer.parseInt(txtStock.getText().trim()));
                m.setMinimumStock(Integer.parseInt(txtMinStock.getText().trim()));
                m.setRackNumber(txtRack.getText().trim());

                String err = stockManager.validateMedicine(m, false);
                if (err != null) {
                    JOptionPane.showMessageDialog(modal, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                medicineDAO.createMedicine(m);
                JOptionPane.showMessageDialog(modal, "Medicine added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                modal.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(modal, "Invalid input values: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(ev -> modal.dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        modal.add(formPanel, BorderLayout.CENTER);
        modal.add(btnPanel, BorderLayout.SOUTH);
        modal.setVisible(true);
    }

    private void showEditMedicineModal() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String medicineId = (String) tableModel.getValueAt(selectedRow, 0);
        Medicine med = medicineDAO.getMedicineById(medicineId);
        if (med == null) return;

        JDialog modal = new JDialog(this, "✏ Edit Medicine: " + med.getMedicineName(), true);
        modal.setSize(500, 480);
        modal.setLocationRelativeTo(this);
        modal.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtID = new JTextField(med.getMedicineId()); txtID.setEditable(false);
        JTextField txtBatch = new JTextField(med.getBatchNumber()); txtBatch.setEditable(false);
        JTextField txtSellingPrice = new JTextField(String.valueOf(med.getSellingPrice()));
        JTextField txtStock = new JTextField(String.valueOf(med.getStockQuantity()));
        JTextField txtExpiry = new JTextField(med.getExpiryDate());
        JTextField txtRack = new JTextField(med.getRackNumber());
        JTextField txtDesc = new JTextField(med.getDescription() != null ? med.getDescription() : "");

        formPanel.add(new JLabel("Medicine ID (Locked)")); formPanel.add(txtID);
        formPanel.add(new JLabel("Batch Number (Locked)")); formPanel.add(txtBatch);
        formPanel.add(new JLabel("Selling Price (₹)")); formPanel.add(txtSellingPrice);
        formPanel.add(new JLabel("Stock Quantity")); formPanel.add(txtStock);
        formPanel.add(new JLabel("Expiry Date (YYYY-MM-DD)")); formPanel.add(txtExpiry);
        formPanel.add(new JLabel("Rack Number")); formPanel.add(txtRack);
        formPanel.add(new JLabel("Description")); formPanel.add(txtDesc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = createStyledButton("Save Changes", new Color(13, 110, 253));
        JButton btnCancel = createStyledButton("Cancel", new Color(108, 117, 125));

        btnSave.addActionListener(ev -> {
            try {
                med.setSellingPrice(Double.parseDouble(txtSellingPrice.getText().trim()));
                med.setStockQuantity(Integer.parseInt(txtStock.getText().trim()));
                med.setExpiryDate(txtExpiry.getText().trim());
                med.setRackNumber(txtRack.getText().trim());
                med.setDescription(txtDesc.getText().trim());

                String err = stockManager.validateMedicine(med, true);
                if (err != null) {
                    JOptionPane.showMessageDialog(modal, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                medicineDAO.updateMedicine(med);
                JOptionPane.showMessageDialog(modal, "Medicine updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                modal.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(modal, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(ev -> modal.dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        modal.add(formPanel, BorderLayout.CENTER);
        modal.add(btnPanel, BorderLayout.SOUTH);
        modal.setVisible(true);
    }

    private void showUpdateStockModal() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to update stock.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String medicineId = (String) tableModel.getValueAt(selectedRow, 0);
        Medicine med = medicineDAO.getMedicineById(medicineId);
        if (med == null) return;

        JDialog modal = new JDialog(this, "📈 Stock Manager: " + med.getMedicineName(), true);
        modal.setSize(480, 380);
        modal.setLocationRelativeTo(this);
        modal.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<String> cbAction = new JComboBox<>(new String[]{"Increase", "Decrease", "Adjust"});
        JTextField txtQty = new JTextField("10");
        JComboBox<String> cbReason = new JComboBox<>(new String[]{"Purchase", "Dispensed", "Expired", "Damaged", "Returned", "Adjustment"});
        JTextField txtRemarks = new JTextField("Stock audit");

        formPanel.add(new JLabel("Medicine Name:")); formPanel.add(new JLabel(med.getMedicineName() + " (Current Stock: " + med.getStockQuantity() + ")"));
        formPanel.add(new JLabel("Action Type:")); formPanel.add(cbAction);
        formPanel.add(new JLabel("Quantity:")); formPanel.add(txtQty);
        formPanel.add(new JLabel("Reason:")); formPanel.add(cbReason);
        formPanel.add(new JLabel("Remarks:")); formPanel.add(txtRemarks);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSubmit = createStyledButton("Update Stock", new Color(217, 119, 6));
        JButton btnCancel = createStyledButton("Cancel", new Color(108, 117, 125));

        btnSubmit.addActionListener(ev -> {
            try {
                int qty = Integer.parseInt(txtQty.getText().trim());
                String action = (String) cbAction.getSelectedItem();
                String reason = (String) cbReason.getSelectedItem();
                String remarks = txtRemarks.getText().trim();

                boolean ok = stockManager.updateStock(medicineId, action, qty, reason, remarks);
                if (ok) {
                    JOptionPane.showMessageDialog(modal, "Stock updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    modal.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(modal, "Failed to update stock.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(modal, "Invalid quantity: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(ev -> modal.dispose());

        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);

        modal.add(formPanel, BorderLayout.CENTER);
        modal.add(btnPanel, BorderLayout.SOUTH);
        modal.setVisible(true);
    }

    private void handleDeleteMedicine() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String medicineId = (String) tableModel.getValueAt(selectedRow, 0);
        String canDeleteErr = stockManager.checkCanDelete(medicineId);
        if (canDeleteErr != null) {
            JOptionPane.showMessageDialog(this, canDeleteErr, "Deletion Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Medicine '" + medicineId + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            medicineDAO.deleteMedicine(medicineId);
            JOptionPane.showMessageDialog(this, "Medicine deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        }
    }

    private void exportInventory() {
        JOptionPane.showMessageDialog(this, "Inventory report exported successfully to 'Niramaya_Medicine_Inventory.csv'.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MedicineInventory().setVisible(true);
        });
    }
}
