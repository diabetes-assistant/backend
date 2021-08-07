package com.github.diabetesassistant.patient.domain;

public record Assignment(String code, Doctor doctor, Patient patient, String state) {}
