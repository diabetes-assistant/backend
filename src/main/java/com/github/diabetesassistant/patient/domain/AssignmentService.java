package com.github.diabetesassistant.patient.domain;

import com.github.diabetesassistant.patient.data.AssignmentEntity;
import com.github.diabetesassistant.patient.data.AssignmentRepository;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@AllArgsConstructor
@Slf4j
public class AssignmentService {
  private final AssignmentRepository assignmentRepository;
  private final UserRepository userRepository;
  private static final char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
  private static final int CODE_LENGTH = 6;
  private static final Random RANDOM_GENERATOR = new SecureRandom();

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

  @SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
  public static String randomString(Random randomGenerator, int length) {
    char[] result = new char[length];
    for (int i = 0; i < result.length; i++) {
      int randomCharIndex = randomGenerator.nextInt(CHARSET_AZ_09.length);
      result[i] = CHARSET_AZ_09[randomCharIndex];
    }
    return new String(result);
  }

  public Mono<Assignment> createAssignment(UUID doctorId) {
    ResponseStatusException notFoundException =
        new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctor not found");
    Mono<Optional<UserEntity>> doctorEntity =
        this.userRepository
            .findById(doctorId)
            .switchIfEmpty(Mono.error(notFoundException))
            .map(Optional::of);
    String code = randomString(RANDOM_GENERATOR, CODE_LENGTH);
    AssignmentEntity assignmentEntity = new AssignmentEntity(code, doctorId, null, "initial");
    Mono<AssignmentEntity> createdAssignmentEntity =
        this.assignmentRepository.save(assignmentEntity);
    return createdAssignmentEntity.zipWith(doctorEntity).map(this::toAssignmentWithDoctor);
  }

  public Flux<Assignment> findAssignments(UUID doctorId, String state) {
    Flux<String> codes =
        this.assignmentRepository.findByDoctorId(doctorId, state).map(AssignmentEntity::code);
    return codes.flatMap(this::findAssignment);
  }

  public Mono<Assignment> confirm(Assignment assignment) {
    UUID doctorId = assignment.doctor().map(Doctor::id).orElse(null);
    UUID patientId = assignment.patient().map(Patient::id).orElse(null);
    AssignmentEntity entity =
        new AssignmentEntity(assignment.code(), doctorId, patientId, assignment.state());
    log.info("got entity");
    log.info(entity.toString());
    return this.assignmentRepository.save(entity).map(_1 -> assignment);
  }
}
