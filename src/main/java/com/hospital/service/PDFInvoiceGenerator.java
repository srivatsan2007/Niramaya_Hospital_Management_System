package com.hospital.service;

import com.hospital.model.PharmacyOrder;
import com.hospital.model.PharmacyOrderItem;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Generates PDF Invoices for Pharmacy Orders.
 */
public class PDFInvoiceGenerator {

    public static File generatePharmacyInvoice(PharmacyOrder order, List<PharmacyOrderItem> items, String patientName) {
        try {
            // We simulate a PDF generator (e.g. using iText or writing a mock HTML/Text invoice file)
            File pdfDir = new File("public/Reports");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            String fileName = "Invoice_" + order.getOrderId() + ".pdf";
            File pdfFile = new File(pdfDir, fileName);

            try (FileWriter writer = new FileWriter(pdfFile)) {
                writer.write("================================================================================\n");
                writer.write("                         NIRAMAYA HOSPITALS\n");
                writer.write("================================================================================\n\n");
                writer.write("                      PHARMACY TAX INVOICE\n\n");
                
                writer.write("Invoice Number: INV-" + order.getOrderId() + "\n");
                writer.write("Pharmacy Token: " + order.getPharmacyToken() + "\n");
                writer.write("Invoice Date:   " + order.getOrderDate() + "\n");
                writer.write("Patient ID:     " + order.getPatientId() + " | Name: " + patientName + "\n");
                writer.write("Doctor Name:    " + order.getDoctorId() + "\n");
                writer.write("Prescription ID:" + order.getPrescriptionId() + "\n\n");
                
                writer.write("--------------------------------------------------------------------------------\n");
                writer.write("MEDICINE DETAILS\n");
                writer.write("--------------------------------------------------------------------------------\n");
                
                if (items != null) {
                    for (PharmacyOrderItem item : items) {
                        writer.write(String.format("%-25s | %-10s | Qty: %-4d | ₹%.2f\n", 
                            item.getMedicineName(), item.getStrength(), item.getQuantity(), item.getSubtotal()));
                    }
                }
                
                writer.write("--------------------------------------------------------------------------------\n");
                writer.write(String.format("Grand Total: ₹%.2f\n", order.getTotalAmount()));
                writer.write("Payment Method: " + order.getPaymentMethod() + " (" + order.getTransactionId() + ")\n\n");
                
                writer.write("Hospital Seal\n");
                writer.write("Thank you for choosing Niramaya Hospitals.\n");
            }
            return pdfFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
