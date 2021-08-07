package com.github.diabetesassistant.patient.presentation;

import com.github.diabetesassistant.patient.domain.Assignment;
import com.github.diabetesassistant.patient.domain.AssignmentService;
import com.github.diabetesassistant.patient.domain.Doctor;
import com.github.diabetesassistant.patient.domain.Patient;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/assignment", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class AssignmentHandler {
  private final AssignmentService assignmentService;

  private DoctorDTO toDTO(Doctor doctor) {
    return new DoctorDTO(doctor.id().toString(), doctor.email());
  }

  private PatientDTO toDTO(Patient patient) {
    return new PatientDTO(patient.id().toString(), patient.email());
  }

  private AssignmentDTO toDTO(Assignment assignment) {
    Optional<DoctorDTO> doctor = assignment.doctor().map(this::toDTO);
    Optional<PatientDTO> patient = assignment.patient().map(this::toDTO);
    return new AssignmentDTO(assignment.code(), doctor, patient, assignment.state());
  }

  @GetMapping("/{code}")
  public Mono<AssignmentDTO> getAssignmentWithCode(@PathVariable("code") String code) {
    log.info("got get assignments with code request");
    Mono<Assignment> maybeAssignment =
        this.assignmentService
            .findAssignment(code)
            .switchIfEmpty(
                Mono.error(
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "assignment not found")));
    return maybeAssignment.map(this::toDTO);
  }

  private ResponseStatusException badRequest(Throwable exception) {
    log.warn("invalid doctorid given", exception);
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid doctorid given", exception);
  }

  @PostMapping
  public Mono<AssignmentDTO> createAssignmentWithCode(
      @RequestBody AssignmentCreationRequestDTO dto) {
    Mono<UUID> doctorId =
        Mono.just(dto.doctorId()).map(UUID::fromString).onErrorMap(this::badRequest);
    return doctorId.flatMap(this.assignmentService::createAssignment).map(this::toDTO);
  }
}
