package com.hospital.service;

import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;

import java.util.*;

/**
 * Service component for Doctor Management operations in Niramaya Hospitals Admin.
 */
public class DoctorManagement {

    private DoctorDAO doctorDAO = new DoctorDAO();

    public List<Doctor> getAllDoctors() {
        return doctorDAO.getAllDoctors();
    }

    public List<Doctor> searchDoctors(String query, String department) {
        List<Doctor> all = getAllDoctors();
        List<Doctor> result = new ArrayList<>();
        String q = (query == null) ? "" : query.toLowerCase().trim();
        String dept = (department == null) ? "all" : department.toLowerCase().trim();

        for (Doctor d : all) {
            boolean matchDept = dept.equals("all") || d.getDepartment().toLowerCase().contains(dept);
            boolean matchQuery = q.isEmpty() ||
                    d.getDoctorId().toLowerCase().contains(q) ||
                    d.getDoctorName().toLowerCase().contains(q) ||
                    d.getDepartment().toLowerCase().contains(q) ||
                    d.getSpecialization().toLowerCase().contains(q);

            if (matchDept && matchQuery) {
                result.add(d);
            }
        }
        return result;
    }

    public boolean addDoctor(Doctor doctor) {
        return doctorDAO.saveDoctor(doctor);
    }

    public boolean toggleDoctorStatus(String doctorId, String status, String acceptAppointments) {
        return doctorDAO.updateAvailability(doctorId, status, acceptAppointments);
    }

    public boolean deleteDoctor(String doctorId) {
        return doctorDAO.deleteDoctor(doctorId);
    }
}
