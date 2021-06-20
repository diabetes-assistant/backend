package com.github.diabetesassistant.data;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserRepository {
  public Mono<UserEntity> findByEmailAndPassword(String email, String password) {
    return null;
  }
}
