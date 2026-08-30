package com.hospital.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Swing component for rendering dynamic report fields for all 10 laboratory test types:
 * CBC, Blood Sugar, Lipid Profile, LFT, KFT, Urine Test, ECG, X-Ray, MRI, CT Scan.
 */
public class DynamicReportForm extends JPanel {

    private String testName;
    private final Map<String, JTextField> fieldsMap = new HashMap<>();
    private final Map<String, JTextArea> textAreasMap = new HashMap<>();

    public DynamicReportForm(String testName) {
        this.testName = testName != null ? testName : "Complete Blood Count (CBC)";
        setLayout(new BorderLayout());
        buildForm();
    }

    public void setTestName(String testName) {
        this.testName = testName != null ? testName : "Complete Blood Count (CBC)";
        buildForm();
    }

    private void buildForm() {
        removeAll();
        fieldsMap.clear();
        textAreasMap.clear();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Dynamic Clinical Fields — " + testName));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String testKey = testName.toLowerCase();

        if (testKey.contains("cbc") || testKey.contains("blood count")) {
            addLabelAndField(formPanel, gbc, 0, "Hemoglobin (g/dL):", "14.2");
            addLabelAndField(formPanel, gbc, 1, "WBC Count (cells/mcL):", "7,500");
            addLabelAndField(formPanel, gbc, 2, "RBC Count (million/mcL):", "4.8");
            addLabelAndField(formPanel, gbc, 3, "Platelet Count (lakhs/mcL):", "2.8");
            addLabelAndField(formPanel, gbc, 4, "Hematocrit (%):", "42.5");
            addLabelAndField(formPanel, gbc, 5, "MCV (fL):", "88");
            addLabelAndField(formPanel, gbc, 6, "MCH (pg):", "29.5");
            addLabelAndField(formPanel, gbc, 7, "MCHC (g/dL):", "33.2");
            addLabelAndField(formPanel, gbc, 8, "ESR (mm/hr):", "12");
            addLabelAndArea(formPanel, gbc, 9, "Reference Range:", "Hb: 12-16 g/dL, WBC: 4500-11000, Platelets: 1.5-4.5 Lakhs");
            addLabelAndArea(formPanel, gbc, 10, "Interpretation:", "Normal CBC parameters. No evidence of anemia or acute leukocytosis.");
            addLabelAndField(formPanel, gbc, 11, "Remarks:", "Platelet count and morphology normal.");
        } else if (testKey.contains("sugar") || testKey.contains("glucose") || testKey.contains("hba1c")) {
            addLabelAndField(formPanel, gbc, 0, "Fasting Blood Sugar (mg/dL):", "98");
            addLabelAndField(formPanel, gbc, 1, "Post-Prandial Blood Sugar (mg/dL):", "135");
            addLabelAndField(formPanel, gbc, 2, "HbA1c (%):", "5.6");
            addLabelAndArea(formPanel, gbc, 3, "Interpretation:", "Euglycemic status. Glycemic control is optimal.");
            addLabelAndField(formPanel, gbc, 4, "Remarks:", "Non-diabetic glycemic range.");
        } else if (testKey.contains("lipid") || testKey.contains("cholesterol")) {
            addLabelAndField(formPanel, gbc, 0, "Total Cholesterol (mg/dL):", "175");
            addLabelAndField(formPanel, gbc, 1, "HDL (Good Cholesterol) (mg/dL):", "52");
            addLabelAndField(formPanel, gbc, 2, "LDL (Bad Cholesterol) (mg/dL):", "98");
            addLabelAndField(formPanel, gbc, 3, "Triglycerides (mg/dL):", "120");
            addLabelAndField(formPanel, gbc, 4, "VLDL (mg/dL):", "24");
            addLabelAndArea(formPanel, gbc, 5, "Interpretation:", "Desirable lipid panel with good cardioprotective HDL ratios.");
        } else if (testKey.contains("liver") || testKey.contains("lft") || testKey.contains("bilirubin")) {
            addLabelAndField(formPanel, gbc, 0, "ALT (SGPT) (U/L):", "28");
            addLabelAndField(formPanel, gbc, 1, "AST (SGOT) (U/L):", "24");
            addLabelAndField(formPanel, gbc, 2, "ALP (U/L):", "78");
            addLabelAndField(formPanel, gbc, 3, "Albumin (g/dL):", "4.2");
            addLabelAndField(formPanel, gbc, 4, "Bilirubin Total (mg/dL):", "0.8");
            addLabelAndArea(formPanel, gbc, 5, "Interpretation:", "Normal hepatic enzyme levels and synthetic function.");
        } else if (testKey.contains("kidney") || testKey.contains("kft") || testKey.contains("creatinine")) {
            addLabelAndField(formPanel, gbc, 0, "Creatinine (mg/dL):", "0.9");
            addLabelAndField(formPanel, gbc, 1, "Urea (mg/dL):", "22");
            addLabelAndField(formPanel, gbc, 2, "Uric Acid (mg/dL):", "5.1");
            addLabelAndField(formPanel, gbc, 3, "Sodium (mEq/L):", "138");
            addLabelAndField(formPanel, gbc, 4, "Potassium (mEq/L):", "4.2");
            addLabelAndArea(formPanel, gbc, 5, "Interpretation:", "Renal filtration and serum electrolyte balance normal.");
        } else if (testKey.contains("urine")) {
            addLabelAndField(formPanel, gbc, 0, "Color:", "Pale Yellow");
            addLabelAndField(formPanel, gbc, 1, "Appearance:", "Clear");
            addLabelAndField(formPanel, gbc, 2, "pH:", "6.5");
            addLabelAndField(formPanel, gbc, 3, "Protein:", "Nil");
            addLabelAndField(formPanel, gbc, 4, "Glucose:", "Nil");
            addLabelAndField(formPanel, gbc, 5, "Ketones:", "Negative");
            addLabelAndField(formPanel, gbc, 6, "Blood:", "Absent");
            addLabelAndField(formPanel, gbc, 7, "Remarks:", "Normal routine urine dipstick analysis.");
        } else if (testKey.contains("ecg") || testKey.contains("electrocardiogram")) {
            addLabelAndField(formPanel, gbc, 0, "Heart Rate (bpm):", "72");
            addLabelAndField(formPanel, gbc, 1, "Rhythm:", "Normal Sinus Rhythm");
            addLabelAndField(formPanel, gbc, 2, "PR Interval (ms):", "154");
            addLabelAndField(formPanel, gbc, 3, "QRS Duration (ms):", "86");
            addLabelAndField(formPanel, gbc, 4, "QT Interval (ms):", "390");
            addLabelAndArea(formPanel, gbc, 5, "Interpretation:", "Normal axis, no ST-T changes or acute ischemic signs.");
            addLabelAndField(formPanel, gbc, 6, "Doctor Recommendation:", "Routine annual cardiac wellness checkup.");
        } else if (testKey.contains("x-ray") || testKey.contains("xray")) {
            addLabelAndField(formPanel, gbc, 0, "Body Part:", "Chest PA View");
            addLabelAndArea(formPanel, gbc, 1, "Findings:", "Lung fields are clear bilaterally. CTR is within normal limits. Cardiac shadow and costophrenic angles normal.");
            addLabelAndArea(formPanel, gbc, 2, "Impression:", "Normal Chest Radiogram.");
            addLabelAndField(formPanel, gbc, 3, "Recommendation:", "No further imaging required at present.");
        } else if (testKey.contains("mri")) {
            addLabelAndField(formPanel, gbc, 0, "Examined Region:", "Brain / Spine");
            addLabelAndArea(formPanel, gbc, 1, "Findings:", "Gray-white matter differentiation preserved. Ventricles and sulci normal for age. No focal mass effect or midline shift.");
            addLabelAndArea(formPanel, gbc, 2, "Impression:", "Unremarkable MRI study.");
            addLabelAndField(formPanel, gbc, 3, "Recommendation:", "Clinical correlation suggested.");
        } else if (testKey.contains("ct scan") || testKey.contains("ct")) {
            addLabelAndField(formPanel, gbc, 0, "Examined Region:", "Abdomen / Thorax");
            addLabelAndField(formPanel, gbc, 1, "Technique:", "Contrast Enhanced Multi-slice CT");
            addLabelAndArea(formPanel, gbc, 2, "Findings:", "Abdominal organs present normal size, attenuation, and enhancement. No lymphadenopathy.");
            addLabelAndArea(formPanel, gbc, 3, "Impression:", "Normal CT Tomography scan.");
            addLabelAndField(formPanel, gbc, 4, "Recommendation:", "Follow up with physician.");
        } else {
            // Generic Fallback Form
            addLabelAndField(formPanel, gbc, 0, "Primary Diagnostic Result:", "Normal Parameter Value");
            addLabelAndArea(formPanel, gbc, 1, "Clinical Observations:", "Patient test parameters within expected physiological limits.");
            addLabelAndArea(formPanel, gbc, 2, "Interpretation:", "No pathology detected.");
            addLabelAndField(formPanel, gbc, 3, "Remarks:", "Routine checkup verified.");
        }

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, int row, String label, String defaultVal) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField tf = new JTextField(defaultVal);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fieldsMap.put(label, tf);
        panel.add(tf, gbc);
    }

    private void addLabelAndArea(JPanel panel, GridBagConstraints gbc, int row, String label, String defaultVal) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextArea ta = new JTextArea(defaultVal, 2, 25);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textAreasMap.put(label, ta);
        panel.add(new JScrollPane(ta), gbc);
    }

    public Map<String, String> getFormData() {
        Map<String, String> data = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : fieldsMap.entrySet()) {
            data.put(entry.getKey(), entry.getValue().getText());
        }
        for (Map.Entry<String, JTextArea> entry : textAreasMap.entrySet()) {
            data.put(entry.getKey(), entry.getValue().getText());
        }
        return data;
    }

    public String buildHtmlTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"results-table\">\n")
          .append("  <thead>\n")
          .append("    <tr>\n")
          .append("      <th>Parameter / Investigation</th>\n")
          .append("      <th>Observed Value</th>\n")
          .append("      <th>Status / Remarks</th>\n")
          .append("    </tr>\n")
          .append("  </thead>\n")
          .append("  <tbody>\n");

        for (Map.Entry<String, JTextField> entry : fieldsMap.entrySet()) {
            sb.append("    <tr>\n")
              .append("      <td><b>").append(entry.getKey().replace(":", "")).append("</b></td>\n")
              .append("      <td><b style=\"color:#0A4DA6;\">").append(entry.getValue().getText()).append("</b></td>\n")
              .append("      <td>Normal Reference Range</td>\n")
              .append("    </tr>\n");
        }

        for (Map.Entry<String, JTextArea> entry : textAreasMap.entrySet()) {
            sb.append("    <tr>\n")
              .append("      <td><b>").append(entry.getKey().replace(":", "")).append("</b></td>\n")
              .append("      <td colspan=\"2\">").append(entry.getValue().getText()).append("</td>\n")
              .append("    </tr>\n");
        }

        sb.append("  </tbody>\n</table>");
        return sb.toString();
    }
}
