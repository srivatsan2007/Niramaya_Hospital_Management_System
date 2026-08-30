package com.hospital.model;

/**
 * Model class representing Laboratory Technicians in Niramaya Hospital.
 */
public class LabTechnician {
    private String technicianId;
    private String name;
    private String phone;
    private int age;
    private String gender;
    private String email;
    private String password;
    private String qualification;
    private String createdAt;

    public LabTechnician() {}

    public LabTechnician(String technicianId, String name, String phone, int age, String gender,
                         String email, String password, String qualification, String createdAt) {
        this.technicianId = technicianId;
        this.name = name;
        this.phone = phone;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.qualification = qualification;
        this.createdAt = createdAt;
    }

    public String getTechnicianId() { return technicianId; }
    public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
