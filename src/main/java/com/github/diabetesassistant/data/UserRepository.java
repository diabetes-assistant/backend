package com.github.diabetesassistant.data;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface UserRepository extends ReactiveCrudRepository<UserEntity, UUID> {
  @Query("SELECT * FROM users where email = :email AND password = :password ")
  Mono<UserEntity> findByEmailAndPassword(String email, String password);
}
