package com.hospital.model;

/**
 * Model representing a Maternity & Newborn Birth Report.
 */
public class BirthReport {
    private String reportId;
    private String certificateNo;
    private String motherId;
    private String motherName;
    private String fatherName;
    private String babyGender;
    private String birthWeight;
    private String birthDate;
    private String birthTime;
    private String deliveryType;
    private String deliveringDoctor;
    private String pediatricianName;
    private String bloodGroup;
    private String complicationsNotes;
    private String createdAt;

    public BirthReport() {}

    public BirthReport(String reportId, String certificateNo, String motherId, String motherName,
                       String fatherName, String babyGender, String birthWeight, String birthDate,
                       String birthTime, String deliveryType, String deliveringDoctor,
                       String pediatricianName, String bloodGroup, String complicationsNotes,
                       String createdAt) {
        this.reportId = reportId;
        this.certificateNo = certificateNo;
        this.motherId = motherId;
        this.motherName = motherName;
        this.fatherName = fatherName;
        this.babyGender = babyGender;
        this.birthWeight = birthWeight;
        this.birthDate = birthDate;
        this.birthTime = birthTime;
        this.deliveryType = deliveryType;
        this.deliveringDoctor = deliveringDoctor;
        this.pediatricianName = pediatricianName;
        this.bloodGroup = bloodGroup;
        this.complicationsNotes = complicationsNotes;
        this.createdAt = createdAt;
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }

    public String getMotherId() { return motherId; }
    public void setMotherId(String motherId) { this.motherId = motherId; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getBabyGender() { return babyGender; }
    public void setBabyGender(String babyGender) { this.babyGender = babyGender; }

    public String getBirthWeight() { return birthWeight; }
    public void setBirthWeight(String birthWeight) { this.birthWeight = birthWeight; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getBirthTime() { return birthTime; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getDeliveringDoctor() { return deliveringDoctor; }
    public void setDeliveringDoctor(String deliveringDoctor) { this.deliveringDoctor = deliveringDoctor; }

    public String getPediatricianName() { return pediatricianName; }
    public void setPediatricianName(String pediatricianName) { this.pediatricianName = pediatricianName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getComplicationsNotes() { return complicationsNotes; }
    public void setComplicationsNotes(String complicationsNotes) { this.complicationsNotes = complicationsNotes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
