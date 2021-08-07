package com.github.diabetesassistant.patient.domain;

import static org.mockito.Mockito.*;

import com.github.diabetesassistant.patient.data.AssignmentEntity;
import com.github.diabetesassistant.patient.data.AssignmentRepository;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {
  @Mock private AssignmentRepository assignmentRepository;
  @Mock private UserRepository userRepository;
  private AssignmentService testee;

  @BeforeEach
  void setUp() {
    this.testee = new AssignmentService(this.assignmentRepository, this.userRepository);
  }

  @Test
  void shouldReturnExistingAssignment() {
    String code = "foobar";
    UUID doctorId = UUID.randomUUID();
    UserEntity doctorEntity = new UserEntity(doctorId, "doctor@email.com", "secret", "doctor");
    when(this.userRepository.findById(doctorId)).thenReturn(Mono.just(doctorEntity));
    UUID patientId = UUID.randomUUID();
    UserEntity patientEntity = new UserEntity(patientId, "patient@email.com", "secret", "patient");
    when(this.userRepository.findById(patientId)).thenReturn(Mono.just(patientEntity));
    AssignmentEntity assignment = new AssignmentEntity(code, doctorId, patientId, "initial");
    when(this.assignmentRepository.findById(code)).thenReturn(Mono.just(assignment));

    Mono<Assignment> actual = this.testee.findAssignment(code);
    Doctor doctor = new Doctor(doctorId, doctorEntity.email());
    Patient patient = new Patient(patientId, patientEntity.email());
    Assignment expected =
        new Assignment(code, Optional.of(doctor), Optional.of(patient), assignment.state());

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldReturnExistingAssignmentWithoutAnyDoctorOrPatient() {
    String code = "foobar";
    AssignmentEntity assignment = new AssignmentEntity(code, null, null, "initial");
    when(this.assignmentRepository.findById(code)).thenReturn(Mono.just(assignment));

    Mono<Assignment> actual = this.testee.findAssignment(code);
    Assignment expected =
        new Assignment(code, Optional.empty(), Optional.empty(), assignment.state());

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldReturnCreatedAssignment() {
    UUID doctorId = UUID.randomUUID();
    AssignmentEntity assignment = new AssignmentEntity("foobar", doctorId, null, "initial");
    when(this.assignmentRepository.save(any())).thenReturn(Mono.just(assignment));
    UserEntity doctorEntity = new UserEntity(doctorId, "foo@bar.com", "secret", "doctor");
    when(this.userRepository.findById(doctorId)).thenReturn(Mono.just(doctorEntity));

    Mono<Assignment> actual = this.testee.createAssignment(doctorId);
    Doctor doctor = new Doctor(doctorId, doctorEntity.email());
    Assignment expected =
        new Assignment(
            assignment.code(), Optional.of(doctor), Optional.empty(), assignment.state());

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldReturnExceptionWhenCreatingForNonExistantDoctor() {
    UUID doctorId = UUID.randomUUID();
    AssignmentEntity assignment = new AssignmentEntity("foobar", doctorId, null, "initial");
    when(this.assignmentRepository.save(any())).thenReturn(Mono.just(assignment));
    when(this.userRepository.findById(doctorId)).thenReturn(Mono.empty());

    Mono<Assignment> actual = this.testee.createAssignment(doctorId);

    StepVerifier.create(actual.log()).expectError(ResponseStatusException.class).verify();
  }

  @Test
  void shouldReturnExistingAssignments() {
    String code = "foobar";
    UUID doctorId = UUID.randomUUID();
    UserEntity doctorEntity = new UserEntity(doctorId, "doctor@email.com", "secret", "doctor");
    when(this.userRepository.findById(doctorId)).thenReturn(Mono.just(doctorEntity));
    AssignmentEntity assignment = new AssignmentEntity(code, doctorId, null, "initial");
    when(this.assignmentRepository.findByDoctorId(doctorId, "initial"))
        .thenReturn(Flux.just(assignment));
    when(this.assignmentRepository.findById(assignment.code())).thenReturn(Mono.just(assignment));

    Flux<Assignment> actual = this.testee.findAssignments(doctorId, "initial");
    Doctor doctor = new Doctor(doctorId, doctorEntity.email());
    Assignment expected =
        new Assignment(code, Optional.of(doctor), Optional.empty(), assignment.state());

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldReturnEmptyForNotExistingState() {
    UUID doctorId = UUID.randomUUID();
    when(this.assignmentRepository.findByDoctorId(doctorId, "initial")).thenReturn(Flux.empty());

    Flux<Assignment> actual = this.testee.findAssignments(doctorId, "initial");

    StepVerifier.create(actual.log()).verifyComplete();
  }
}
