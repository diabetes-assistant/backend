package com.github.diabetesassistant.patient.domain;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@AllArgsConstructor
@Slf4j
public class PatientService {
  public Flux<Patient> findPatients(UUID userId) {
    log.info("Looking for patients");
    return Flux.empty();
  }
}
