package com.hospital.model;

/**
 * Hospital Settings Model for Niramaya Hospitals.
 */
public class Settings {
    private String hospitalName;
    private String hospitalLogo;
    private String address;
    private String phone;
    private String email;
    private String gstNumber;
    private String workingHours;
    private String emergencyContact;

    public Settings() {
        this.hospitalName = "Niramaya Hospitals";
        this.hospitalLogo = "assets/logo.png";
        this.address = "No. 25, Anna Salai, Chennai - 600002";
        this.phone = "+91 98765 43210";
        this.email = "contact@niramaya.health";
        this.gstNumber = "33AAAAA0000A1Z5";
        this.workingHours = "24 Hours / 7 Days";
        this.emergencyContact = "108 / 1800-425-0000";
    }

    public Settings(String hospitalName, String hospitalLogo, String address, String phone, String email, String gstNumber, String workingHours, String emergencyContact) {
        this.hospitalName = hospitalName;
        this.hospitalLogo = hospitalLogo;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.gstNumber = gstNumber;
        this.workingHours = workingHours;
        this.emergencyContact = emergencyContact;
    }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalLogo() { return hospitalLogo; }
    public void setHospitalLogo(String hospitalLogo) { this.hospitalLogo = hospitalLogo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}
