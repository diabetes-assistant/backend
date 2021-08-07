package com.github.diabetesassistant.patient.presentation;

public record AssignmentDTO(String code, DoctorDTO doctor, PatientDTO patient, String state) {}
