package com.hospital.model;

/**
 * Model representing a Hospital Staff Member with a unified Employee Code (EMP-XXXXXX).
 * Common for Doctor, Lab Technician, Pharmacist, Receptionist, Administrator, Nurse, Accountant, HR.
 */
public class Staff {

    private String employeeCode;    // Primary Key e.g. EMP-000001
    private String role;            // Doctor, Laboratory Technician, Pharmacist, Receptionist, Administrator, Nurse, Accountant, HR
    private String fullName;
    private String email;
    private String mobile;
    private String department;
    private String designation;
    private String qualification;
    private String experience;
    private String status;          // Active, Pending, Approved, Rejected, Inactive
    private String createdAt;
    private String updatedAt;

    // Role-specific & Profile / ID Card attributes
    private String medicalRegNo;
    private String specialization;
    private double consultationFee;
    private String licenseNo;
    private String officeExtension;
    private String bloodGroup;
    private String joiningDate;
    private String emergencyContact;
    private String validity;
    private String password;

    public Staff() {
        this.status = "Pending";
        this.bloodGroup = "O+";
        this.validity = "31-DEC-2028";
    }

    public Staff(String employeeCode, String role, String fullName, String email, String mobile, 
                 String department, String designation, String status) {
        this();
        this.employeeCode = employeeCode;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.department = department;
        this.designation = designation;
        this.status = status;
    }

    // Getters and Setters
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getMedicalRegNo() { return medicalRegNo; }
    public void setMedicalRegNo(String medicalRegNo) { this.medicalRegNo = medicalRegNo; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }

    public String getOfficeExtension() { return officeExtension; }
    public void setOfficeExtension(String officeExtension) { this.officeExtension = officeExtension; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getJoiningDate() { return joiningDate; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getValidity() { return validity; }
    public void setValidity(String validity) { this.validity = validity; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
