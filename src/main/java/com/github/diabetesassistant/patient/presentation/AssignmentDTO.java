package com.github.diabetesassistant.patient.presentation;

import java.util.Optional;

public record AssignmentDTO(
    String code, Optional<DoctorDTO> doctor, Optional<PatientDTO> patient, String state) {}
