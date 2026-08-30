package com.hospital.model;

/**
 * Doctor Model Entity representing records in the Doctors table.
 * Supports all user-entered registration fields and availability status flags.
 */
public class Doctor {
    private String doctorId;
    private String name;
    private String phone;
    private int age;
    private String gender;
    private String email;
    private String password;
    private String qualification;
    private String category;
    private double consultationFees;
    private String workingDays;
    private String workingHours;
    private String availableStatus; // 'Online' or 'Offline'
    private boolean appointmentAvailable; // true or false
    private String createdAt;

    public Doctor() {}

    public Doctor(String doctorId, String name, String phone, int age, String gender,
                  String email, String password, String qualification, String category,
                  double consultationFees, String workingDays, String workingHours,
                  String availableStatus, boolean appointmentAvailable, String createdAt) {
        this.doctorId = doctorId;
        this.name = name;
        this.phone = phone;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.qualification = qualification;
        this.category = category;
        this.consultationFees = consultationFees;
        this.workingDays = workingDays;
        this.workingHours = workingHours;
        this.availableStatus = availableStatus;
        this.appointmentAvailable = appointmentAvailable;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDoctorName() { return name; }
    public void setDoctorName(String doctorName) { this.name = doctorName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhoneNumber() { return phone; }
    public void setPhoneNumber(String phoneNumber) { this.phone = phoneNumber; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSpecialization() { return category; }
    public void setSpecialization(String specialization) { this.category = specialization; }
    public String getDepartment() { return category; }
    public void setDepartment(String department) { this.category = department; }

    public double getConsultationFees() { return consultationFees; }
    public void setConsultationFees(double consultationFees) { this.consultationFees = consultationFees; }
    public double getConsultationFee() { return consultationFees; }
    public void setConsultationFee(double consultationFee) { this.consultationFees = consultationFee; }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
    public String getAvailableDays() { return workingDays; }
    public void setAvailableDays(String availableDays) { this.workingDays = availableDays; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public String getAvailableTime() { return workingHours; }
    public void setAvailableTime(String availableTime) { this.workingHours = availableTime; }

    public String getAvailableStatus() { return availableStatus; }
    public void setAvailableStatus(String availableStatus) { this.availableStatus = availableStatus; }
    public String getStatus() { return availableStatus; }
    public void setStatus(String status) { this.availableStatus = status; }

    public boolean isAppointmentAvailable() { return appointmentAvailable; }
    public void setAppointmentAvailable(boolean appointmentAvailable) { this.appointmentAvailable = appointmentAvailable; }
    public String getAcceptAppointments() { return appointmentAvailable ? "Yes" : "No"; }
    public void setAcceptAppointments(String acceptAppointments) {
        this.appointmentAvailable = "Yes".equalsIgnoreCase(acceptAppointments) || "true".equalsIgnoreCase(acceptAppointments) || "1".equals(acceptAppointments);
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCreatedDate() { return createdAt; }
    public void setCreatedDate(String createdDate) { this.createdAt = createdDate; }

    public String getExperience() { return "5+ Yrs"; }
    public String getProfilePhoto() { return ""; }
}
