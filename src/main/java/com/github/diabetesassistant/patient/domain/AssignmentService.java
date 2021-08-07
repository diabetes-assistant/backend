package com.github.diabetesassistant.patient.domain;

import com.github.diabetesassistant.patient.data.AssignmentEntity;
import com.github.diabetesassistant.patient.data.AssignmentRepository;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@AllArgsConstructor
@Slf4j
public class AssignmentService {
  private final AssignmentRepository assignmentRepository;
  private final UserRepository userRepository;

  private Assignment toAssignmentWithDoctor(Tuple2<AssignmentEntity, Optional<UserEntity>> tuple2) {
    Optional<Doctor> doctor = tuple2.getT2().map(d -> new Doctor(d.id(), d.email()));
    return new Assignment(tuple2.getT1().code(), doctor, Optional.empty(), tuple2.getT1().state());
  }

  private Assignment toAssignmentWithPatient(Tuple2<Assignment, Optional<UserEntity>> tuple2) {
    Optional<Patient> patient = tuple2.getT2().map((p) -> new Patient(p.id(), p.email()));
    return new Assignment(
        tuple2.getT1().code(), tuple2.getT1().doctor(), patient, tuple2.getT1().state());
  }

  public Mono<Assignment> findAssignment(String code) {
    Mono<AssignmentEntity> assignment = this.assignmentRepository.findById(code);
    Mono<UUID> patientId = assignment.mapNotNull(AssignmentEntity::patientid);
    Mono<UUID> doctorId = assignment.mapNotNull(AssignmentEntity::doctorid);
    Mono<Optional<UserEntity>> doctorEntity =
        doctorId
            .flatMap(this.userRepository::findById)
            .map(Optional::of)
            .switchIfEmpty(Mono.just(Optional.empty()));
    Mono<Optional<UserEntity>> patientEntity =
        patientId
            .flatMap(this.userRepository::findById)
            .map(Optional::of)
            .switchIfEmpty(Mono.just(Optional.empty()));
    Mono<Assignment> assignmentWithDoctor =
        assignment.zipWith(doctorEntity).map(this::toAssignmentWithDoctor);
    return assignmentWithDoctor.zipWith(patientEntity).map(this::toAssignmentWithPatient);
  }
}
