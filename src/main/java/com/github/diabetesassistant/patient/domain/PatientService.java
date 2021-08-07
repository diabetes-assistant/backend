package com.github.diabetesassistant.patient.domain;

import com.github.diabetesassistant.patient.data.AssignmentEntity;
import com.github.diabetesassistant.patient.data.AssignmentRepository;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class PatientService {
  private final UserRepository userRepository;
  private final AssignmentRepository assignmentRepository;

  public Flux<Patient> findPatients(UUID doctorId) {
    log.info("Looking for patients");
    Mono<UserEntity> maybeDoctor =
        this.userRepository
            .findById(doctorId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
    Flux<AssignmentEntity> assignments =
        maybeDoctor.flatMapMany(
            doctor -> this.assignmentRepository.findByDoctorId(doctor.id(), "confirmed"));
    Flux<UUID> patientIds = assignments.map(AssignmentEntity::patientid);
    Flux<UserEntity> users = patientIds.flatMap(userRepository::findById);
    return users.map(user -> new Patient(user.id(), user.email()));
  }
}
