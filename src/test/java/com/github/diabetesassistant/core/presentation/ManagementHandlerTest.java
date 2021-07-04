package com.github.diabetesassistant.core.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ManagementHandlerTest {
  private ManagementHandler testee;

  @BeforeEach
  void setUp() {
    this.testee = new ManagementHandler();
  }

  @Test
  void shouldReturnHealthDTOMono() {
    Mono<HealthStateDTO> actual = this.testee.getHealthState();
    HealthStateDTO expected = new HealthStateDTO("\uD83D\uDE3B");

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }
}
