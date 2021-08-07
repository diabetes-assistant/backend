package com.github.diabetesassistant.patient.domain;

import java.util.Optional;

public record Assignment(
    String code, Optional<Doctor> doctor, Optional<Patient> patient, String state) {}
