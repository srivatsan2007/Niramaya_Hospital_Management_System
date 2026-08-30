package com.hospital.model;

public class Nurse {
    private String nurseId;
    private String employeeCode;
    private String name;
    private String fullName;
    private String gender;
    private String dob;
    private String phone;
    private String phoneNumber;
    private String email;
    private String department;
    private String qualification;
    private int experienceYears;
    private String shift;
    private String joiningDate;
    private String address;
    private String username;
    private String password;
    private String profilePhoto;
    private String status;
    private String createdAt;
    private String updatedAt;

    public Nurse() {}

    public Nurse(String nurseId, String employeeCode, String name, String gender, String dob, String phone,
                 String email, String department, String qualification, int experienceYears, String shift,
                 String joiningDate, String address, String password) {
        this.nurseId = nurseId;
        this.employeeCode = employeeCode;
        this.name = name;
        this.fullName = name;
        this.gender = gender;
        this.dob = dob;
        this.phone = phone;
        this.phoneNumber = phone;
        this.email = email;
        this.department = department;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.shift = shift;
        this.joiningDate = joiningDate;
        this.address = address;
        this.username = email;
        this.password = password;
        this.status = "Active";
    }

    // Getters and Setters
    public String getNurseId() { return nurseId; }
    public void setNurseId(String nurseId) { this.nurseId = nurseId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getName() { return name != null ? name : fullName; }
    public void setName(String name) { this.name = name; this.fullName = name; }

    public String getFullName() { return fullName != null ? fullName : name; }
    public void setFullName(String fullName) { this.fullName = fullName; this.name = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getPhone() { return phone != null ? phone : phoneNumber; }
    public void setPhone(String phone) { this.phone = phone; this.phoneNumber = phone; }

    public String getPhoneNumber() { return phoneNumber != null ? phoneNumber : phone; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; this.phone = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getJoiningDate() { return joiningDate; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUsername() { return username != null ? username : email; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
