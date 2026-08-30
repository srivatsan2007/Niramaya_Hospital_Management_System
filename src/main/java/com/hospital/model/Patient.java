package com.hospital.model;

/**
 * Patient Entity Model.
 */
public class Patient {
    private String patientId;
    private String name;
    private String email;
    private String phone;
    private int age;
    private String gender;
    private String bloodGroup;

    public Patient() {}

    public Patient(String patientId, String name, String email, String phone, int age, String gender, String bloodGroup) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
}
