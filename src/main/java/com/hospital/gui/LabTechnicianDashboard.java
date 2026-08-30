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
 * Professional Laboratory Technician Dashboard (Java Swing).
 * Features Sidebar: Dashboard, Pending Tests, Generate Report, Completed Reports, Patient Search, My Profile, Logout.
 */
public class LabTechnicianDashboard extends JFrame {

    private final JPanel contentArea;
    private final CardLayout cardLayout;
    private final LabBookingDAO bookingDAO = new LabBookingDAO();
    private final LabReportDAO reportDAO = new LabReportDAO();

    private DefaultTableModel pendingTableModel;
    private DefaultTableModel completedTableModel;
    private JTable pendingTable;
    private JTable completedTable;

    public LabTechnicianDashboard() {
        setTitle("Niramaya Hospitals — Laboratory Technician Portal");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 700));
        sidebar.setBackground(new Color(6, 48, 107));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Brand
        JLabel brand = new JLabel("🧪 NIRAMAYA LABS");
        brand.setFont(new Font("Sora", Font.BOLD, 18));
        brand.setForeground(Color.WHITE);
        brand.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));
        sidebar.add(brand);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);

        // Add Navigation Buttons
        sidebar.add(createNavButton("📊 Dashboard", "DASHBOARD"));
        sidebar.add(createNavButton("⏳ Pending Tests", "PENDING"));
        sidebar.add(createNavButton("📝 Generate Report", "GENERATE"));
        sidebar.add(createNavButton("✅ Completed Reports", "COMPLETED"));
        sidebar.add(createNavButton("🔍 Patient Search", "SEARCH"));
        sidebar.add(createNavButton("👤 My Profile", "PROFILE"));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createNavButton("↩ Logout", "LOGOUT"));

        add(sidebar, BorderLayout.WEST);

        // Build Cards
        contentArea.add(buildDashboardPanel(), "DASHBOARD");
        contentArea.add(buildPendingPanel(), "PENDING");
        contentArea.add(buildCompletedPanel(), "COMPLETED");
        contentArea.add(buildSearchPanel(), "SEARCH");
        contentArea.add(buildProfilePanel(), "PROFILE");

        add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, "DASHBOARD");
    }

    private JButton createNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(6, 48, 107));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addActionListener(e -> {
            if ("LOGOUT".equals(cardName)) {
                dispose();
                JOptionPane.showMessageDialog(null, "Logged out successfully from Lab Tech Console.");
            } else if ("GENERATE".equals(cardName)) {
                openGenerateWindowForFirstPending();
            } else {
                cardLayout.show(contentArea, cardName);
                if ("PENDING".equals(cardName)) loadPendingData();
                if ("COMPLETED".equals(cardName)) loadCompletedData();
            }
        });
        return btn;
    }

    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(new Color(245, 248, 251));
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Laboratory Technician Diagnostic Dashboard");
        title.setFont(new Font("Sora", Font.BOLD, 22));
        title.setForeground(new Color(6, 48, 107));
        p.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setBackground(new Color(245, 248, 251));

        grid.add(createCard("Total Test Queue", "12", new Color(11, 95, 203)));
        grid.add(createCard("Pending Diagnostics", "5", new Color(232, 169, 76)));
        grid.add(createCard("Completed Reports", "7", new Color(10, 178, 167)));
        grid.add(createCard("Verified Pathologists", "3", new Color(0, 200, 83)));

        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private JPanel createCard(String title, String val, Color c) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(225, 233, 240), 1, true));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(), BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(new Color(74, 91, 110));

        JLabel v = new JLabel(val);
        v.setFont(new Font("Sora", Font.BOLD, 32));
        v.setForeground(c);

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPendingPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Pending Diagnostics Queue (Payment Status = Paid)");
        title.setFont(new Font("Sora", Font.BOLD, 18));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Patient ID", "Patient Name", "Age/Gender", "Doctor Name", "Department", "Test Name", "Booking Date", "Payment", "Action"};
        pendingTableModel = new DefaultTableModel(cols, 0);
        pendingTable = new JTable(pendingTableModel);
        p.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        JButton btnAction = new JButton("🧪 Generate Report for Selected Booking");
        btnAction.setBackground(new Color(0, 200, 83));
        btnAction.setForeground(Color.WHITE);
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAction.addActionListener(e -> openGenerateWindowForSelected());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnAction);
        p.add(bottom, BorderLayout.SOUTH);

        loadPendingData();
        return p;
    }

    private JPanel buildCompletedPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Completed Laboratory Reports");
        title.setFont(new Font("Sora", Font.BOLD, 18));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Report ID", "Booking ID", "Patient ID", "Test Name", "Result Summary", "Technician", "Report Date", "Status"};
        completedTableModel = new DefaultTableModel(cols, 0);
        completedTable = new JTable(completedTableModel);
        p.add(new JScrollPane(completedTable), BorderLayout.CENTER);

        loadCompletedData();
        return p;
    }

    private JPanel buildSearchPanel() {
        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchFld = new JTextField(25);
        JButton searchBtn = new JButton("🔍 Search Patient / Booking / Report");
        top.add(new JLabel("Enter ID or Name:"));
        top.add(searchFld);
        top.add(searchBtn);
        p.add(top, BorderLayout.NORTH);

        DefaultTableModel searchModel = new DefaultTableModel(new String[]{"Report ID", "Booking ID", "Patient ID", "Test Name", "Report Date", "Status"}, 0);
        JTable searchTable = new JTable(searchModel);
        p.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> {
            searchModel.setRowCount(0);
            List<LabReport> list = reportDAO.searchReports(searchFld.getText());
            for (LabReport r : list) {
                searchModel.addRow(new Object[]{r.getReportId(), r.getBookingId(), r.getPatientId(), r.getTestName(), r.getReportDate(), r.getStatus()});
            }
        });
        return p;
    }

    private JPanel buildProfilePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("👤 Laboratory Technician Specialist — Niramaya Diagnostics Console");
        lbl.setFont(new Font("Sora", Font.BOLD, 18));
        p.add(lbl);
        return p;
    }

    private void loadPendingData() {
        pendingTableModel.setRowCount(0);
        List<LabBooking> bookings = bookingDAO.getAllBookings();
        if (bookings.isEmpty()) {
            // Add default fallback row if database empty
            pendingTableModel.addRow(new Object[]{"LAB-100842", "PT100842", "Rekha Prasad", "28 / Female", "Dr. Ananya Rao", "Cardiology", "Complete Blood Count (CBC)", "2026-07-30", "Paid", "Generate Report"});
        } else {
            for (LabBooking b : bookings) {
                if ("Paid".equalsIgnoreCase(b.getPaymentStatus()) && !"Completed".equalsIgnoreCase(b.getStatus())) {
                    pendingTableModel.addRow(new Object[]{b.getBookingId(), b.getPatientId(), "Rekha Prasad", "28 / Female", "Dr. Ananya Rao", "Cardiology", b.getTestName(), b.getBookingDate(), b.getPaymentStatus(), "Generate Report"});
                }
            }
        }
    }

    private void loadCompletedData() {
        completedTableModel.setRowCount(0);
        List<LabReport> reports = reportDAO.getAllReports();
        for (LabReport r : reports) {
            completedTableModel.addRow(new Object[]{r.getReportId(), r.getBookingId(), r.getPatientId(), r.getTestName(), r.getResult(), r.getUploadedBy(), r.getReportDate(), r.getStatus()});
        }
    }

    private void openGenerateWindowForSelected() {
        int row = pendingTable.getSelectedRow();
        LabBooking b;
        if (row >= 0) {
            String bId = (String) pendingTable.getValueAt(row, 0);
            String pId = (String) pendingTable.getValueAt(row, 1);
            String test = (String) pendingTable.getValueAt(row, 6);
            String date = (String) pendingTable.getValueAt(row, 7);
            b = new LabBooking(bId, pId, "DOC1001", "RX-100842", test, date, "Morning", "Pending", "Paid", date);
        } else {
            b = new LabBooking("LAB-100842", "PT100842", "DOC1001", "RX-100842", "Complete Blood Count (CBC)", "2026-07-30", "Morning", "Pending", "Paid", "2026-07-29");
        }
        new GenerateLabReport(b).setVisible(true);
    }

    private void openGenerateWindowForFirstPending() {
        openGenerateWindowForSelected();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LabTechnicianDashboard().setVisible(true));
    }
}
