package com.github.diabetesassistant.patient.presentation;

import com.github.diabetesassistant.patient.domain.Patient;
import com.github.diabetesassistant.patient.domain.PatientService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/patient", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class PatientHandler {

  private final PatientService patientService;

  private PatientDTO toDTO(Patient patient) {
    return new PatientDTO(patient.id().toString(), patient.email());
  }

  @GetMapping
  public Flux<PatientDTO> getPatients(@RequestParam("doctorId") String doctorId) {
    log.info("got get patients request");
    Flux<UUID> doctorIdFlux =
        Flux.just(doctorId)
            .map(UUID::fromString)
            .onErrorMap(
                (exception) -> {
                  log.warn("invalid doctorId given", exception);
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Invalid doctorId given", exception);
                });
    Flux<Patient> patients = doctorIdFlux.flatMap(patientService::findPatients);
    return patients.map(this::toDTO);
  }
}
