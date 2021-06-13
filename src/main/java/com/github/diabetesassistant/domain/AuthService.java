package com.github.diabetesassistant.domain;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class AuthService {
  public Mono<Tokens> authenticate(User user) {
    return null;
  }
}
