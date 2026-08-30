package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;

import java.util.*;

/**
 * Service component for Patient Management operations in Niramaya Hospitals Admin.
 */
public class PatientManagement {

    private PatientDAO patientDAO = new PatientDAO();

    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }

    public List<Patient> searchPatients(String query) {
        List<Patient> all = getAllPatients();
        if (query == null || query.trim().isEmpty()) return all;

        List<Patient> result = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Patient p : all) {
            if (p.getPatientId().toLowerCase().contains(q) ||
                p.getName().toLowerCase().contains(q) ||
                p.getEmail().toLowerCase().contains(q) ||
                p.getPhone().toLowerCase().contains(q)) {
                result.add(p);
            }
        }
        return result;
    }

    public boolean addPatient(Patient p) {
        return patientDAO.savePatient(p);
    }

    public boolean deletePatient(String patientId) {
        return patientDAO.deletePatient(patientId);
    }
}
