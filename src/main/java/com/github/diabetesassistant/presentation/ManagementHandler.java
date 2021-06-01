package com.github.diabetesassistant.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ManagementHandler {

    @GetMapping("/health")
    public Mono<HealthStateDTO> getHealthState() {
        return Mono.just(new HealthStateDTO("\uD83D\uDE3B"));
    }
}
