package com.github.diabetesassistant.data;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, UUID> {
  @Query("SELECT * FROM users where email = :email AND password = :password ")
  Mono<UserEntity> findByEmailAndPassword(String email, String password);
}
