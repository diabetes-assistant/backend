package com.github.diabetesassistant.patient.data;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("assignments")
public record AssignmentEntity(@Id String code, UUID doctorid, UUID patientid, String state) {}
