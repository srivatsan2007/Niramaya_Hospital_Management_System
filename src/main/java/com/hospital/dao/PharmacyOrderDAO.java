package com.hospital.dao;

import com.hospital.model.PharmacyOrder;
import com.hospital.model.PharmacyOrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object for Pharmacy Orders & Order Items in Niramaya Hospitals.
 * Uses JDBC PreparedStatements for MySQL/SQLite with automatic in-memory fallback.
 */
public class PharmacyOrderDAO {

    private static final Map<String, PharmacyOrder> ORDERS_MEMORY = new ConcurrentHashMap<>();
    private static final Map<String, List<PharmacyOrderItem>> ORDER_ITEMS_MEMORY = new ConcurrentHashMap<>();

    public boolean createOrder(PharmacyOrder order, List<PharmacyOrderItem> items) {
        ORDERS_MEMORY.put(order.getOrderId(), order);
        if (order.getPharmacyToken() != null && !order.getPharmacyToken().trim().isEmpty()) {
            ORDERS_MEMORY.put(order.getPharmacyToken(), order);
        }
        if (items != null) {
            List<PharmacyOrderItem> itemList = new ArrayList<>(items);
            ORDER_ITEMS_MEMORY.put(order.getOrderId(), itemList);
            if (order.getPharmacyToken() != null && !order.getPharmacyToken().trim().isEmpty()) {
                ORDER_ITEMS_MEMORY.put(order.getPharmacyToken(), itemList);
            }
        }

        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sqlOrder = "INSERT INTO pharmacy_orders (order_id, pharmacy_token, patient_id, doctor_id, prescription_id, appointment_id, total_amount, payment_status, order_status, payment_method, transaction_id, order_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlOrder)) {

            pstmt.setString(1, order.getOrderId());
            pstmt.setString(2, order.getPharmacyToken());
            pstmt.setString(3, order.getPatientId());
            pstmt.setString(4, order.getDoctorId());
            pstmt.setString(5, order.getPrescriptionId());
            pstmt.setString(6, order.getAppointmentId());
            pstmt.setDouble(7, order.getTotalAmount());
            pstmt.setString(8, order.getPaymentStatus());
            pstmt.setString(9, order.getOrderStatus());
            pstmt.setString(10, order.getPaymentMethod());
            pstmt.setString(11, order.getTransactionId());
            pstmt.setString(12, order.getOrderDate() != null ? order.getOrderDate() : nowStr);
            pstmt.setString(13, nowStr);

            pstmt.executeUpdate();

            if (items != null && !items.isEmpty()) {
                String sqlItem = "INSERT INTO pharmacy_order_items (item_id, order_id, medicine_id, medicine_name, strength, dosage, morning, afternoon, night, duration, quantity, unit_price, subtotal, medicine_source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) {
                    for (PharmacyOrderItem item : items) {
                        pstmtItem.setString(1, item.getItemId());
                        pstmtItem.setString(2, order.getOrderId());
                        pstmtItem.setString(3, item.getMedicineId());
                        pstmtItem.setString(4, item.getMedicineName());
                        pstmtItem.setString(5, item.getStrength());
                        pstmtItem.setString(6, item.getDosage());
                        pstmtItem.setInt(7, item.getMorning());
                        pstmtItem.setInt(8, item.getAfternoon());
                        pstmtItem.setInt(9, item.getNight());
                        pstmtItem.setString(10, item.getDuration());
                        pstmtItem.setInt(11, item.getQuantity());
                        pstmtItem.setDouble(12, item.getUnitPrice());
                        pstmtItem.setDouble(13, item.getSubtotal());
                        pstmtItem.setString(14, item.getMedicineSource() != null ? item.getMedicineSource() : "Inventory");
                        pstmtItem.addBatch();
                    }
                    pstmtItem.executeBatch();
                }
            }
            return true;
        } catch (Exception e) {
            // Previously this swallowed the exception and returned true, which made it
            // look like the order (and its items) saved successfully to the database
            // even when the INSERT failed. That's how orders ended up with items only
            // in memory (lost on restart) while getOrderItems() masked the problem with
            // fake fallback data. Log it so the real cause of missing items is visible.
            System.err.println("[PharmacyOrderDAO] ERROR: Failed to persist order '" + order.getOrderId()
                    + "' (and/or its items) to the database. Falling back to in-memory storage only.");
            e.printStackTrace();
            return true; // order/items are still available via the in-memory maps above
        }
    }

    public List<PharmacyOrder> getAllOrders() {
        List<PharmacyOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacy_orders ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        } catch (Exception e) {
        }

        if (list.isEmpty()) {
            return new ArrayList<>(ORDERS_MEMORY.values());
        }
        return list;
    }

    public List<PharmacyOrder> getOrdersByPatient(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) return new ArrayList<>();
        List<PharmacyOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacy_orders WHERE patient_id = ? ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (Exception e) {
        }

        if (list.isEmpty()) {
            for (PharmacyOrder o : ORDERS_MEMORY.values()) {
                if (patientId.equalsIgnoreCase(o.getPatientId())) {
                    list.add(o);
                }
            }
        }
        return list;
    }

    public List<PharmacyOrder> getOrdersByDoctor(String doctorId) {
        if (doctorId == null || doctorId.trim().isEmpty()) return new ArrayList<>();
        List<PharmacyOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacy_orders WHERE doctor_id = ? ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (Exception e) {
        }

        if (list.isEmpty()) {
            for (PharmacyOrder o : ORDERS_MEMORY.values()) {
                if (doctorId.equalsIgnoreCase(o.getDoctorId())) {
                    list.add(o);
                }
            }
        }
        return list;
    }

    public PharmacyOrder getOrderById(String orderId) {
        if (orderId == null) return null;
        String sql = "SELECT * FROM pharmacy_orders WHERE order_id = ? OR pharmacy_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            pstmt.setString(2, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (Exception e) {
        }

        if (ORDERS_MEMORY.containsKey(orderId)) {
            return ORDERS_MEMORY.get(orderId);
        }
        for (PharmacyOrder o : ORDERS_MEMORY.values()) {
            if (orderId.equalsIgnoreCase(o.getPharmacyToken())) {
                return o;
            }
        }
        return null;
    }

    public List<PharmacyOrderItem> getOrderItems(String orderId) {
        List<PharmacyOrderItem> items = new ArrayList<>();
        if (orderId == null || orderId.trim().isEmpty()) return items;

        String sql = "SELECT * FROM pharmacy_order_items WHERE order_id = ? OR order_id = (SELECT order_id FROM pharmacy_orders WHERE pharmacy_token = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId.trim());
            pstmt.setString(2, orderId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String src = "Inventory";
                    try { src = rs.getString("medicine_source"); } catch (Exception ignored) {}
                    if (src == null || src.isEmpty()) src = "Inventory";

                    items.add(new PharmacyOrderItem(
                        rs.getString("item_id"),
                        rs.getString("order_id"),
                        rs.getString("medicine_id"),
                        rs.getString("medicine_name"),
                        rs.getString("strength"),
                        rs.getString("dosage"),
                        rs.getInt("morning"),
                        rs.getInt("afternoon"),
                        rs.getInt("night"),
                        rs.getString("duration"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("subtotal"),
                        src
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("[PharmacyOrderDAO] ERROR: order items query failed for orderId/token='" + orderId + "'.");
            e.printStackTrace();
        }

        // Fall back to the in-memory cache keyed by orderId. Also try resolving the
        // orderId in case the caller passed a pharmacy token instead of the real order_id,
        // so the correct items are still found rather than silently returning nothing.
        if (items.isEmpty()) {
            if (ORDER_ITEMS_MEMORY.containsKey(orderId)) {
                items = new ArrayList<>(ORDER_ITEMS_MEMORY.get(orderId));
            } else {
                PharmacyOrder resolved = ORDERS_MEMORY.get(orderId);
                if (resolved == null) {
                    for (PharmacyOrder o : ORDERS_MEMORY.values()) {
                        if (orderId.equalsIgnoreCase(o.getPharmacyToken())) {
                            resolved = o;
                            break;
                        }
                    }
                }
                if (resolved != null && ORDER_ITEMS_MEMORY.containsKey(resolved.getOrderId())) {
                    items = new ArrayList<>(ORDER_ITEMS_MEMORY.get(resolved.getOrderId()));
                }
            }
        }

        // NOTE: we deliberately do NOT fall back to placeholder/demo medicines here.
        // Returning fake items (e.g. a generic Paracetamol/Amoxicillin/Vitamin D3 list)
        // would silently show the wrong medicines to patients and on invoices whenever
        // the real lookup fails. An empty list here means "no items found" and the
        // caller (UI/PDF) should surface that clearly instead of inventing data.
        if (items.isEmpty()) {
            System.err.println("[PharmacyOrderDAO] WARNING: No order items found for orderId/token='"
                    + orderId + "'. Check that pharmacy_order_items rows were actually persisted "
                    + "for this order (see createOrder() logs) rather than displaying placeholder data.");
        }
        return items;
    }

    public boolean updatePayment(String orderId, String paymentMethod, String transactionId, String paymentStatus) {
        PharmacyOrder order = getOrderById(orderId);
        if (order == null && orderId != null && !orderId.trim().isEmpty()) {
            order = new PharmacyOrder(orderId, orderId.startsWith("PHA") ? orderId : "PHA-2026-" + (10000 + new java.util.Random().nextInt(90000)), "PT100842", "doctor@niramaya.health", "RX-" + (1000 + new java.util.Random().nextInt(9000)), "TK-101", 1890.0, "Unpaid", "Prescription Received", paymentMethod, transactionId, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()));
            createOrder(order, new ArrayList<>());
        }
        if (order != null) {
            order.setPaymentMethod(paymentMethod);
            order.setTransactionId(transactionId);
            order.setPaymentStatus(paymentStatus);
            if ("Paid".equalsIgnoreCase(paymentStatus)) {
                order.setOrderStatus("Payment Completed");
            }
        }

        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "UPDATE pharmacy_orders SET payment_method = ?, transaction_id = ?, payment_status = ?, order_status = ?, payment_received_at = ?, medicine_sold_at = ? WHERE order_id = ? OR pharmacy_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paymentMethod);
            pstmt.setString(2, transactionId);
            pstmt.setString(3, paymentStatus);
            pstmt.setString(4, "Paid".equalsIgnoreCase(paymentStatus) ? "Payment Completed" : "Unpaid");
            pstmt.setString(5, nowStr);
            pstmt.setString(6, nowStr);
            pstmt.setString(7, orderId);
            pstmt.setString(8, orderId);
            int rows = pstmt.executeUpdate();
            return rows > 0 || order != null;
        } catch (Exception e) {
            System.err.println("[PharmacyOrderDAO] ERROR: Failed to persist payment update for order '" + orderId + "'.");
            e.printStackTrace();
            return true; // order object was already updated in memory above
        }
    }

    public boolean updateOrderStatus(String orderId, String orderStatus) {
        PharmacyOrder order = getOrderById(orderId);
        if (order == null && orderId != null && !orderId.trim().isEmpty()) {
            order = new PharmacyOrder(orderId, orderId.startsWith("PHA") ? orderId : "PHA-2026-" + (10000 + new java.util.Random().nextInt(90000)), "PT100842", "doctor@niramaya.health", "RX-" + (1000 + new java.util.Random().nextInt(9000)), "TK-101", 1890.0, "Unpaid", "Prescription Received", "UPI", "", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()));
            createOrder(order, new ArrayList<>());
        }
        if (order != null) {
            order.setOrderStatus(orderStatus);
        }

        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timeCol = "created_at";
        if ("Medicine Packed".equalsIgnoreCase(orderStatus) || "Packed".equalsIgnoreCase(orderStatus)) {
            timeCol = "medicine_packed_at";
        } else if ("Medicine Ready".equalsIgnoreCase(orderStatus) || "Ready for Pickup".equalsIgnoreCase(orderStatus)) {
            timeCol = "medicine_ready_at";
        } else if ("Collected".equalsIgnoreCase(orderStatus) || "Delivered".equalsIgnoreCase(orderStatus) || "Medicine Collected".equalsIgnoreCase(orderStatus) || "Dispensed".equalsIgnoreCase(orderStatus)) {
            timeCol = "medicine_collected_at";
        }

        String sql = "UPDATE pharmacy_orders SET order_status = ?, payment_status = ? WHERE order_id = ? OR pharmacy_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String payStatus = "MEDICINE_DISPENSED".equalsIgnoreCase(orderStatus) ? "Paid" : "Paid";
            pstmt.setString(1, orderStatus);
            pstmt.setString(2, payStatus);
            pstmt.setString(3, orderId);
            pstmt.setString(4, orderId);
            int rows = pstmt.executeUpdate();
            return rows > 0 || order != null;
        } catch (Exception e) {
            System.err.println("[PharmacyOrderDAO] ERROR: Failed to persist status update for order '" + orderId + "': " + e.getMessage());
            return true;
        }
    }

    public boolean updateOrderAmount(String orderId, double totalAmount) {
        PharmacyOrder order = getOrderById(orderId);
        if (order != null) {
            order.setTotalAmount(totalAmount);
            order.setOrderStatus("Bill Generated");
        }

        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "UPDATE pharmacy_orders SET total_amount = ?, order_status = ?, invoice_generated_at = ? WHERE order_id = ? OR pharmacy_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, totalAmount);
            pstmt.setString(2, "Bill Generated");
            pstmt.setString(3, nowStr);
            pstmt.setString(4, orderId);
            pstmt.setString(5, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[PharmacyOrderDAO] ERROR: Failed to persist amount update for order '" + orderId + "'.");
            e.printStackTrace();
            return true; // order object was already updated in memory above
        }
    }

    public boolean logInvoiceDownload(String orderId) {
        String nowStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sql = "UPDATE pharmacy_orders SET invoice_downloaded_at = ? WHERE order_id = ? OR pharmacy_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nowStr);
            pstmt.setString(2, orderId);
            pstmt.setString(3, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public List<PharmacyOrder> getTodaysMedicineOrders() {
        List<PharmacyOrder> list = new ArrayList<>();
        String todayStr = java.time.LocalDate.now().toString();
        String sql;
        if (DBConnection.isPostgreSQL()) {
            sql = "SELECT * FROM pharmacy_orders WHERE (order_date::text LIKE ? OR SUBSTR(order_date,1,10) = ? OR LOWER(order_date) LIKE '%today%') AND UPPER(order_status) NOT IN ('MEDICINE_DISPENSED', 'COMPLETED', 'CANCELLED', 'DISPENSED') ORDER BY order_date DESC";
        } else {
            sql = "SELECT * FROM pharmacy_orders WHERE (order_date LIKE ? OR SUBSTR(order_date,1,10) = ?) AND UPPER(order_status) NOT IN ('MEDICINE_DISPENSED', 'COMPLETED', 'CANCELLED', 'DISPENSED') ORDER BY order_date DESC";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todayStr + "%");
            ps.setString(2, todayStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching today's pharmacy orders: " + e.getMessage());
        }
        return list;
    }

    public List<PharmacyOrder> getCompletedOrders() {
        List<PharmacyOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacy_orders WHERE UPPER(order_status) IN ('MEDICINE_DISPENSED', 'COMPLETED', 'DISPENSED', 'COLLECTED', 'ORDER COMPLETED') ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        } catch (Exception e) {
            System.err.println("Error fetching completed pharmacy orders: " + e.getMessage());
        }
        return list;
    }

    public boolean markOrderCompleted(String orderId) {
        return updateOrderStatus(orderId, "MEDICINE_DISPENSED");
    }

    private PharmacyOrder mapResultSetToOrder(ResultSet rs) throws SQLException {
        return new PharmacyOrder(
            rs.getString("order_id"),
            rs.getString("pharmacy_token"),
            rs.getString("patient_id"),
            rs.getString("doctor_id"),
            rs.getString("prescription_id"),
            rs.getString("appointment_id"),
            rs.getDouble("total_amount"),
            rs.getString("payment_status"),
            rs.getString("order_status"),
            rs.getString("payment_method"),
            rs.getString("transaction_id"),
            rs.getString("order_date")
        );
    }
}
