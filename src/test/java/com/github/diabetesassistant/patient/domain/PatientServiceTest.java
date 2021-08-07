package com.github.diabetesassistant.patient.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.github.diabetesassistant.patient.data.AssignmentEntity;
import com.github.diabetesassistant.patient.data.AssignmentRepository;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private AssignmentRepository assignmentRepository;
  private PatientService testee;

  @BeforeEach
  void setUp() {
    this.testee = new PatientService(this.userRepository, this.assignmentRepository);
  }

  @Test
  void shouldReturnPatientsForDoctor() {
    UUID doctorId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();
    AssignmentEntity assignment = new AssignmentEntity("code", doctorId, patientId, "initial");
    when(this.assignmentRepository.findByDoctorId(doctorId)).thenReturn(Flux.just(assignment));
    UserEntity patient = new UserEntity(patientId, "patient@email.com", "secret", "patient");
    when(this.userRepository.findById(patientId)).thenReturn(Mono.just(patient));
    UserEntity doctor = new UserEntity(doctorId, "doctor@email.com", "secret", "doctor");
    when(this.userRepository.findById(doctorId)).thenReturn(Mono.just(doctor));

    Flux<Patient> actual = this.testee.findPatients(doctorId);
    Patient expected = new Patient(patientId, patient.email());

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldReturnEmptyFluxForNotExistingDoctor() {
    UUID doctorId = UUID.randomUUID();
    when(this.userRepository.findById(doctorId)).thenReturn(Mono.empty());

    Flux<Patient> actual = this.testee.findPatients(doctorId);

    StepVerifier.create(actual.log()).expectError(IllegalArgumentException.class).verify();
    verify(this.assignmentRepository, times(0)).findByDoctorId(any());
  }
}
