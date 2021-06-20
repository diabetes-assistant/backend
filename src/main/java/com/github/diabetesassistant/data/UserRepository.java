package com.github.diabetesassistant.data;

import reactor.core.publisher.Mono;

public class UserRepository {
  public Mono<UserEntity> findByEmailAndPassword(String email, String password) {
    return null;
  }
}
