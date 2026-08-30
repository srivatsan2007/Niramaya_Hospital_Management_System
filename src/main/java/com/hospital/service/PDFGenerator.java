package com.hospital.service;

import com.hospital.model.LabReport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Service to generate professional hospital PDF & HTML laboratory reports for Niramaya Hospitals.
 * Automatically saves files to `Reports/` and `public/Reports/`.
 */
public class PDFGenerator {

    public static File generateReportPDF(LabReport report, String parametersHtmlTable) {
        try {
            // Ensure target directories exist
            File reportsDir = new File("Reports");
            if (!reportsDir.exists()) reportsDir.mkdirs();

            File publicReportsDir = new File("public/Reports");
            if (!publicReportsDir.exists()) publicReportsDir.mkdirs();

            String filename = "LabReport_" + report.getPatientId() + "_" + report.getReportId() + ".pdf";
            File targetFile = new File(reportsDir, filename);
            File publicTargetFile = new File(publicReportsDir, filename);

            String htmlContent = buildReportHTML(report, parametersHtmlTable);

            // Save PDF / Printable HTML document
            saveHtmlFile(targetFile, htmlContent);
            saveHtmlFile(publicTargetFile, htmlContent);

            System.out.println("✓ Generated Lab Report PDF at: " + targetFile.getAbsolutePath());
            return targetFile;
        } catch (Exception e) {
            System.err.println("Error generating PDF report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void saveHtmlFile(File file, String content) throws Exception {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }

    public static String buildReportHTML(LabReport report, String parametersTable) {
        String pId = report.getPatientId();
        String rId = report.getReportId();
        String bId = report.getBookingId();
        String qrData = "NIRAMAYA-LAB-REPORT|ReportID:" + rId + "|PatientID:" + pId + "|BookingID:" + bId;

        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "  <meta charset=\"UTF-8\">\n" +
               "  <title>Lab Report — " + rId + "</title>\n" +
               "  <style>\n" +
               "    @media print { body { padding: 0; background: #fff; } .no-print { display: none !important; } .report-box { border: none !important; box-shadow: none !important; padding: 0 !important; } }\n" +
               "    body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #0F2A4A; background: #F8FAFC; margin: 0; padding: 20px; }\n" +
               "    .report-box { max-width: 800px; margin: 0 auto; background: #ffffff; border: 1.5px solid #CBD5E1; border-radius: 16px; padding: 36px; box-shadow: 0 10px 30px rgba(0,0,0,0.06); }\n" +
               "    .header-table { width: 100%; border-bottom: 2px solid #0A4DA6; padding-bottom: 16px; margin-bottom: 24px; }\n" +
               "    .brand-title { font-size: 26px; font-weight: 800; color: #06306B; margin: 0; letter-spacing: -0.5px; }\n" +
               "    .brand-sub { font-size: 13px; color: #0AB2A7; font-weight: 700; letter-spacing: 2px; text-transform: uppercase; margin-top: 4px; }\n" +
               "    .patient-card { background: #F1F5F9; border-radius: 12px; padding: 18px; margin-bottom: 24px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; font-size: 14px; }\n" +
               "    .test-badge { background: #0A4DA6; color: #ffffff; display: inline-block; padding: 6px 16px; border-radius: 20px; font-weight: 700; font-size: 14px; margin-bottom: 18px; }\n" +
               "    table.results-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; font-size: 14px; }\n" +
               "    table.results-table th { background: #06306B; color: #ffffff; text-align: left; padding: 10px 14px; font-size: 13px; font-weight: 700; text-transform: uppercase; }\n" +
               "    table.results-table td { padding: 12px 14px; border-bottom: 1px solid #E2E8F0; }\n" +
               "    table.results-table tr:nth-child(even) td { background: #F8FAFC; }\n" +
               "    .section-title { font-size: 15px; font-weight: 700; color: #06306B; margin-top: 18px; margin-bottom: 8px; border-left: 4px solid #0AB2A7; padding-left: 10px; }\n" +
               "    .notes-box { background: #FAF5FF; border: 1px solid #E9D5FF; border-radius: 10px; padding: 14px; font-size: 13px; color: #581C87; margin-bottom: 24px; }\n" +
               "    .signatures-row { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 36px; pt-4; border-top: 1px solid #E2E8F0; padding-top: 20px; }\n" +
               "    .sig-block { text-align: center; width: 200px; }\n" +
               "    .sig-line { border-bottom: 1.5px solid #0F2A4A; margin-bottom: 6px; height: 40px; display: flex; align-items: flex-end; justify-content: center; font-style: italic; font-weight: 700; color: #0A4DA6; }\n" +
               "    .footer-note { text-align: center; font-size: 11px; color: #64748B; margin-top: 28px; border-top: 1px dashed #CBD5E1; padding-top: 12px; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"no-print\" style=\"max-width:800px; margin:0 auto 16px auto; display:flex; justify-content:space-between; align-items:center; background:#ffffff; padding:12px 20px; border-radius:12px; border:1px solid #CBD5E1;\">\n" +
               "    <div style=\"font-weight:700; color:#06306B;\">📄 Niramaya Hospitals — Laboratory Test Report (" + rId + ")</div>\n" +
               "    <button onclick=\"window.print()\" style=\"background:#0D6EFD; color:#ffffff; border:none; padding:8px 18px; border-radius:8px; font-weight:700; cursor:pointer; font-size:13px;\">🖨️ Save as PDF / Print</button>\n" +
               "  </div>\n" +
               "  <div class=\"report-box\">\n" +
               "    <table class=\"header-table\">\n" +
               "      <tr>\n" +
               "        <td>\n" +
               "          <div class=\"brand-title\">🏥 NIRAMAYA HOSPITALS</div>\n" +
               "          <div class=\"brand-sub\">Compassion. Care. Cure.</div>\n" +
               "          <div style=\"font-size: 12px; color: #64748B; margin-top: 6px;\">Department of Diagnostic Pathology & Clinical Laboratories</div>\n" +
               "        </td>\n" +
               "        <td style=\"text-align: right;\">\n" +
               "          <div style=\"font-size: 14px; font-weight: 800; color: #06306B;\">LABORATORY TEST REPORT</div>\n" +
               "          <div style=\"font-size: 12px; color: #475569;\">Report ID: <b>" + rId + "</b></div>\n" +
               "          <div style=\"font-size: 12px; color: #475569;\">Booking ID: <b>" + bId + "</b></div>\n" +
               "          <div style=\"font-size: 12px; color: #475569;\">Report Date: <b>" + report.getReportDate() + "</b></div>\n" +
               "        </td>\n" +
               "      </tr>\n" +
               "    </table>\n" +
               "\n" +
               "    <div class=\"patient-card\">\n" +
               "      <div><b>Patient Unique ID:</b> " + report.getPatientId() + "</div>\n" +
               "      <div><b>Recommending Doctor:</b> " + report.getDoctorName() + "</div>\n" +
               "      <div><b>Patient Name:</b> " + report.getPatientName() + "</div>\n" +
               "      <div><b>Department:</b> " + report.getDepartment() + "</div>\n" +
               "      <div><b>Age / Gender:</b> " + report.getPatientAge() + " / " + report.getPatientGender() + "</div>\n" +
               "      <div><b>Sample Status:</b> <span style=\"color: #00C853; font-weight: 700;\">Verified & Processed</span></div>\n" +
               "    </div>\n" +
               "\n" +
               "    <div class=\"test-badge\">🧪 Diagnostic Test: " + report.getTestName() + "</div>\n" +
               "\n" +
               "    " + (parametersTable != null && !parametersTable.isEmpty() ? parametersTable : buildDefaultTable(report)) + "\n" +
               "\n" +
               "    <div class=\"section-title\">CLINICAL INTERPRETATION</div>\n" +
               "    <div class=\"notes-box\">" + (report.getObservation() != null ? report.getObservation() : "All observed physiological values lie within normal diagnostic reference intervals.") + "</div>\n" +
               "\n" +
               "    <div class=\"section-title\">PATHOLOGIST REMARKS</div>\n" +
               "    <div style=\"font-size: 13px; color: #334155; margin-bottom: 24px;\">" + (report.getRemarks() != null ? report.getRemarks() : "No pathological abnormalities identified. Follow up as advised by attending physician.") + "</div>\n" +
               "\n" +
               "    <div class=\"signatures-row\">\n" +
               "      <div class=\"sig-block\">\n" +
               "        <div class=\"sig-line\">" + report.getUploadedBy() + "</div>\n" +
               "        <div style=\"font-size: 11px; font-weight: 700; color: #475569;\">Laboratory Technician</div>\n" +
               "        <div style=\"font-size: 10px; color: #94A3B8;\">Niramaya Clinical Labs</div>\n" +
               "      </div>\n" +
               "\n" +
               "      <div style=\"text-align: center;\">\n" +
               "        <div style=\"width: 70px; height: 70px; border: 2px dashed #0AB2A7; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: 800; color: #0AB2A7; margin: 0 auto 4px auto;\">SEAL<br>VERIFIED</div>\n" +
               "        <div style=\"font-size: 9px; color: #64748B;\">QR: " + rId + "</div>\n" +
               "      </div>\n" +
               "\n" +
               "      <div class=\"sig-block\">\n" +
               "        <div class=\"sig-line\">" + report.getVerifiedBy() + "</div>\n" +
               "        <div style=\"font-size: 11px; font-weight: 700; color: #475569;\">Chief Pathologist</div>\n" +
               "        <div style=\"font-size: 10px; color: #94A3B8;\">MD Pathology (Niramaya)</div>\n" +
               "      </div>\n" +
               "    </div>\n" +
               "\n" +
               "    <div class=\"footer-note\">\n" +
               "      This report is generated electronically by Niramaya Hospitals Smart Management System.<br>\n" +
               "      Verification Code: <b>" + qrData.hashCode() + "</b> &nbsp;|&nbsp; 24/7 Diagnostics Desk: +91 44 2834 9000\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }

    private static String buildDefaultTable(LabReport report) {
        return "<table class=\"results-table\">\n" +
               "  <thead>\n" +
               "    <tr>\n" +
               "      <th>Parameter / Investigation</th>\n" +
               "      <th>Observed Result</th>\n" +
               "      <th>Reference Range</th>\n" +
               "      <th>Unit</th>\n" +
               "    </tr>\n" +
               "  </thead>\n" +
               "  <tbody>\n" +
               "    <tr>\n" +
               "      <td><b>" + report.getTestName() + " Result</b></td>\n" +
               "      <td><b style=\"color:#0A4DA6;\">" + report.getResult() + "</b></td>\n" +
               "      <td>Standard Reference</td>\n" +
               "      <td>Clinical Unit</td>\n" +
               "    </tr>\n" +
               "  </tbody>\n" +
               "</table>";
    }

    public static File generatePharmacyInvoicePDF(com.hospital.model.PharmacyOrder order, java.util.List<com.hospital.model.PharmacyOrderItem> items, String patientName, String doctorName) {
        try {
            File reportsDir = new File("Reports");
            if (!reportsDir.exists()) reportsDir.mkdirs();

            File publicReportsDir = new File("public/Reports");
            if (!publicReportsDir.exists()) publicReportsDir.mkdirs();

            String filename = "PharmacyInvoice_" + order.getPharmacyToken() + "_" + order.getOrderId() + ".pdf";
            File targetFile = new File(reportsDir, filename);
            File publicTargetFile = new File(publicReportsDir, filename);

            String htmlContent = buildPharmacyInvoiceHTML(order, items, patientName, doctorName);

            saveHtmlFile(targetFile, htmlContent);
            saveHtmlFile(publicTargetFile, htmlContent);

            System.out.println("✓ Generated Pharmacy Invoice PDF at: " + targetFile.getAbsolutePath());
            return targetFile;
        } catch (Exception e) {
            System.err.println("Error generating Pharmacy Invoice PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String buildPharmacyInvoiceHTML(com.hospital.model.PharmacyOrder order, java.util.List<com.hospital.model.PharmacyOrderItem> items, String patientName, String doctorName) {
        String token = order.getPharmacyToken();
        String invNo = "INV-" + (order.getOrderId() != null ? order.getOrderId().replace("PHA-", "") : "10084");
        if (patientName == null || patientName.isEmpty()) patientName = "Rekha Prasad";
        if (doctorName == null || doctorName.isEmpty()) doctorName = "Dr. Ananya Rao";

        if (items == null || items.isEmpty()) {
            try {
                com.hospital.dao.PharmacyOrderDAO pDao = new com.hospital.dao.PharmacyOrderDAO();
                if (order != null && order.getOrderId() != null) {
                    items = pDao.getOrderItems(order.getOrderId());
                }
                if ((items == null || items.isEmpty()) && order != null && order.getPharmacyToken() != null) {
                    items = pDao.getOrderItems(order.getPharmacyToken());
                }
            } catch (Exception ignored) {}
        }

        if ((items == null || items.isEmpty()) && order != null && order.getPrescriptionId() != null) {
            try {
                com.hospital.dao.PrescriptionDAO rxDao = new com.hospital.dao.PrescriptionDAO();
                com.hospital.model.Prescription rx = rxDao.getPrescriptionById(order.getPrescriptionId());
                if (rx != null && rx.getMedicines() != null && !rx.getMedicines().isEmpty()) {
                    String medsStr = rx.getMedicines();
                    items = parsePrescriptionMedicinesToItems(medsStr, order != null ? order.getOrderId() : "ORD-1");
                }
            } catch (Exception ignored) {}
        }

        double subtotalSum = 0;
        StringBuilder rowsHtml = new StringBuilder();
        if (items != null && !items.isEmpty()) {
            for (com.hospital.model.PharmacyOrderItem item : items) {
                double sub = item.getSubtotal() > 0 ? item.getSubtotal() : (item.getUnitPrice() * item.getQuantity());
                subtotalSum += sub;
                rowsHtml.append("<tr>")
                        .append("<td><b>").append(item.getMedicineName()).append("</b></td>")
                        .append("<td>").append(item.getStrength() != null ? item.getStrength() : "Standard").append("</td>")
                        .append("<td style=\"text-align:center;\">").append(item.getQuantity()).append("</td>")
                        .append("<td style=\"text-align:right;\">₹").append(String.format("%.2f", item.getUnitPrice())).append("</td>")
                        .append("<td style=\"text-align:right;\">₹").append(String.format("%.2f", sub)).append("</td>")
                        .append("</tr>");
            }
        } else {
            subtotalSum = order.getTotalAmount() > 0 ? (order.getTotalAmount() / 1.05) : 0.0;
            rowsHtml.append("<tr><td colspan=\"5\" style=\"text-align:center; padding:16px; color:#64748B;\">No prescribed medicines listed for this invoice.</td></tr>");
        }

        double gst = order.getTotalAmount() > 0 ? (order.getTotalAmount() - subtotalSum) : Math.round(subtotalSum * 0.05);
        double grandTotal = order.getTotalAmount() > 0 ? order.getTotalAmount() : (subtotalSum + gst);

        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "  <meta charset=\"UTF-8\">\n" +
               "  <title>Pharmacy Tax Invoice — " + token + "</title>\n" +
               "  <style>\n" +
               "    @media print { body { padding: 0; background: #fff; } .no-print { display: none !important; } .invoice-box { border: none !important; box-shadow: none !important; padding: 0 !important; } }\n" +
               "    body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #0F2A4A; background: #F8FAFC; margin: 0; padding: 20px; }\n" +
               "    .invoice-box { max-width: 800px; margin: 0 auto; background: #ffffff; border: 1.5px solid #CBD5E1; border-radius: 16px; padding: 36px; box-shadow: 0 10px 30px rgba(0,0,0,0.06); }\n" +
               "    .header-table { width: 100%; border-bottom: 2px solid #0D6EFD; padding-bottom: 16px; margin-bottom: 24px; }\n" +
               "    .brand-title { font-size: 26px; font-weight: 800; color: #06306B; margin: 0; letter-spacing: -0.5px; }\n" +
               "    .brand-sub { font-size: 13px; color: #00C853; font-weight: 700; letter-spacing: 2px; text-transform: uppercase; margin-top: 4px; }\n" +
               "    .patient-card { background: #F1F5F9; border-radius: 12px; padding: 18px; margin-bottom: 24px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; font-size: 14px; }\n" +
               "    .token-badge { background: #0D6EFD; color: #ffffff; display: inline-block; padding: 6px 16px; border-radius: 20px; font-weight: 700; font-size: 14px; margin-bottom: 18px; }\n" +
               "    table.inv-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; font-size: 14px; }\n" +
               "    table.inv-table th { background: #06306B; color: #ffffff; text-align: left; padding: 10px 14px; font-size: 13px; font-weight: 700; text-transform: uppercase; }\n" +
               "    table.inv-table td { padding: 12px 14px; border-bottom: 1px solid #E2E8F0; }\n" +
               "    table.inv-table tr:nth-child(even) td { background: #F8FAFC; }\n" +
               "    .totals-table { width: 320px; margin-left: auto; font-size: 14px; border-collapse: collapse; margin-bottom: 24px; }\n" +
               "    .totals-table td { padding: 8px 12px; }\n" +
               "    .grand-total-row { font-size: 18px; font-weight: 800; color: #0D6EFD; border-top: 2px solid #0D6EFD; border-bottom: 2px solid #0D6EFD; }\n" +
               "    .pay-card { background: #EFF6FF; border: 1px solid #BFDBFE; border-radius: 10px; padding: 14px; font-size: 13px; color: #1E40AF; margin-bottom: 24px; display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 10px; }\n" +
               "    .footer-note { text-align: center; font-size: 13px; font-weight: 700; color: #06306B; margin-top: 28px; border-top: 1px dashed #CBD5E1; padding-top: 16px; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"no-print\" style=\"max-width:800px; margin:0 auto 16px auto; display:flex; justify-content:space-between; align-items:center; background:#ffffff; padding:12px 20px; border-radius:12px; border:1px solid #CBD5E1;\">\n" +
               "    <div style=\"font-weight:700; color:#06306B;\">💊 Niramaya Hospitals — Official Pharmacy Tax Invoice</div>\n" +
               "    <div style=\"display:flex; gap:10px;\">\n" +
               "      <button onclick=\"window.print()\" style=\"background:#0D6EFD; color:#ffffff; border:none; padding:8px 18px; border-radius:8px; font-weight:700; cursor:pointer; font-size:13px;\">🖨️ Print Invoice / Save PDF</button>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "  <div class=\"invoice-box\">\n" +
               "    <table class=\"header-table\">\n" +
               "      <tr>\n" +
               "        <td>\n" +
               "          <div class=\"brand-title\">🏥 NIRAMAYA HOSPITALS</div>\n" +
               "          <div class=\"brand-sub\">Compassion. Care. Cure.</div>\n" +
               "          <div style=\"font-size: 12px; color: #64748B; margin-top: 6px;\">Department of Pharmacy & Pharmaceutical Services</div>\n" +
               "        </td>\n" +
               "        <td style=\"text-align: right;\">\n" +
               "          <div style=\"font-size: 16px; font-weight: 800; color: #06306B;\">PHARMACY TAX INVOICE</div>\n" +
               "          <div style=\"font-size: 12px; color: #475569;\">Invoice No: <b>" + invNo + "</b></div>\n" +
               "          <div style=\"font-size: 12px; color: #475569;\">Pharmacy Token: <b>" + token + "</b></div>\n" +
               "          <div style=\"font-size: 12px; color: #475569;\">Invoice Date: <b>" + (order.getOrderDate() != null ? order.getOrderDate() : new java.util.Date().toString()) + "</b></div>\n" +
               "        </td>\n" +
               "      </tr>\n" +
               "    </table>\n" +
               "\n" +
               "    <div class=\"patient-card\">\n" +
               "      <div><b>Patient Unique ID:</b> " + order.getPatientId() + "</div>\n" +
               "      <div><b>Attending Doctor:</b> " + doctorName + "</div>\n" +
               "      <div><b>Patient Name:</b> " + patientName + "</div>\n" +
               "      <div><b>Prescription ID:</b> " + (order.getPrescriptionId() != null ? order.getPrescriptionId() : "RX-100842") + "</div>\n" +
               "    </div>\n" +
               "\n" +
               "    <div class=\"token-badge\">💊 Pharmacy Token: " + token + "</div>\n" +
               "\n" +
               "    <table class=\"inv-table\">\n" +
               "      <thead>\n" +
               "        <tr>\n" +
               "          <th>Medicine Name</th>\n" +
               "          <th>Strength</th>\n" +
               "          <th style=\"text-align:center;\">Quantity</th>\n" +
               "          <th style=\"text-align:right;\">Unit Price</th>\n" +
               "          <th style=\"text-align:right;\">Subtotal</th>\n" +
               "        </tr>\n" +
               "      </thead>\n" +
               "      <tbody>\n" +
               "        " + rowsHtml.toString() + "\n" +
               "      </tbody>\n" +
               "    </table>\n" +
               "\n" +
               "    <table class=\"totals-table\">\n" +
               "      <tr>\n" +
               "        <td>Subtotal:</td>\n" +
               "        <td style=\"text-align:right;\"><b>₹" + String.format("%.2f", subtotalSum) + "</b></td>\n" +
               "      </tr>\n" +
               "      <tr>\n" +
               "        <td>GST (5%):</td>\n" +
               "        <td style=\"text-align:right;\"><b>₹" + String.format("%.2f", gst) + "</b></td>\n" +
               "      </tr>\n" +
               "      <tr class=\"grand-total-row\">\n" +
               "        <td>Grand Total:</td>\n" +
               "        <td style=\"text-align:right;\">₹" + String.format("%.2f", grandTotal) + "</td>\n" +
               "      </tr>\n" +
               "    </table>\n" +
               "\n" +
               "    <div class=\"pay-card\">\n" +
               "      <div><b>Payment Method:</b> " + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "UPI / Online") + "</div>\n" +
               "      <div><b>Transaction ID:</b> " + (order.getTransactionId() != null ? order.getTransactionId() : "TXN" + System.currentTimeMillis()) + "</div>\n" +
               "      <div><b>Payment Status:</b> <span style=\"color:#00C853; font-weight:700;\">" + (order.getPaymentStatus() != null ? order.getPaymentStatus() : "Paid") + " ✓</span></div>\n" +
               "    </div>\n" +
               "\n" +
               "    <div class=\"footer-note\">\n" +
               "      Thank you for choosing Niramaya Hospitals.<br>\n" +
               "      <span style=\"font-size:11px; color:#64748B; font-weight:normal;\">For Pharmacy Inquiries: +91 44 2834 9005 &nbsp;|&nbsp; GSTIN: 33AAAAA0000A1Z5</span>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }

    private static java.util.List<com.hospital.model.PharmacyOrderItem> parsePrescriptionMedicinesToItems(String medsStr, String orderId) {
        java.util.List<com.hospital.model.PharmacyOrderItem> list = new java.util.ArrayList<>();
        if (medsStr == null || medsStr.trim().isEmpty()) return list;
        String trimmed = medsStr.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                String inner = trimmed.substring(1, trimmed.length() - 1);
                String[] jsonObjects = inner.split("(?<=\\}),\\s*(?=\\{)");
                int idx = 1;
                for (String objStr : jsonObjects) {
                    java.util.Map<String, String> itemMap = parseJsonFlatPdf(objStr);
                    String name = itemMap.getOrDefault("medicineName", itemMap.getOrDefault("name", "Prescribed Medicine"));
                    String str = itemMap.getOrDefault("strength", "Standard");
                    int m = tryParseIntPdf(itemMap.getOrDefault("morning", "1"), 1);
                    int a = tryParseIntPdf(itemMap.getOrDefault("afternoon", "0"), 0);
                    int n = tryParseIntPdf(itemMap.getOrDefault("night", "1"), 1);
                    String timing = itemMap.getOrDefault("instructions", itemMap.getOrDefault("dosage", "After Food"));
                    String duration = itemMap.getOrDefault("duration", "5 Days");
                    int qty = tryParseIntPdf(itemMap.getOrDefault("quantity", itemMap.getOrDefault("qty", "0")), 0);
                    if (qty <= 0) qty = (m + a + n) * 5;
                    if (qty <= 0) qty = 10;
                    double unitPrice = tryParseDoublePdf(itemMap.getOrDefault("unitPrice", "0"), 45.0);
                    if (unitPrice <= 0) unitPrice = 45.0;
                    double subtotal = unitPrice * qty;
                    list.add(new com.hospital.model.PharmacyOrderItem(
                        "ITM-" + orderId + "-" + idx++, orderId, "MED" + (100 + idx), name, str, timing, m, a, n, duration, qty, unitPrice, subtotal, "Inventory"
                    ));
                }
            } catch(Exception ignored){}
        } else {
            String[] lines = trimmed.contains(";") ? trimmed.split(";") : trimmed.split("\n");
            int idx = 1;
            for (String l : lines) {
                if (l.trim().isEmpty()) continue;
                String[] parts = l.split(",");
                String name = parts[0].trim();
                double unitPrice = 45.0;
                int qty = 10;
                double subtotal = unitPrice * qty;
                list.add(new com.hospital.model.PharmacyOrderItem(
                    "ITM-" + orderId + "-" + idx++, orderId, "MED" + (100 + idx), name, "Standard", "After Food", 1, 0, 1, "5 Days", qty, unitPrice, subtotal, "Inventory"
                ));
            }
        }
        return list;
    }

    private static java.util.Map<String, String> parseJsonFlatPdf(String json) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        if (json == null || json.isEmpty()) return map;
        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);
        String[] pairs = s.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replace("\"", "");
                String v = kv[1].trim().replace("\"", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private static int tryParseIntPdf(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static double tryParseDoublePdf(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }
}
