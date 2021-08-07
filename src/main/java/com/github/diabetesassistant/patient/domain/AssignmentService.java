package com.github.diabetesassistant.patient.domain;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class AssignmentService {
  public Mono<Assignment> findAssignment(String code) {
    return Mono.empty();
  }
}
