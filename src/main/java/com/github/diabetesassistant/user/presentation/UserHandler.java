package com.github.diabetesassistant.user.presentation;

import com.github.diabetesassistant.core.presentation.ErrorDTO;
import com.github.diabetesassistant.user.domain.User;
import com.github.diabetesassistant.user.domain.UserService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(
    value = "/user",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class UserHandler {
  UserService service;

  @PostMapping
  public Mono<ResponseEntity<?>> createUser(@RequestBody UserCreationRequestDTO dto) {
    log.info("handling incoming user creation request");
    User user = new User(Optional.empty(), dto.email(), dto.password(), null);
    Mono<User> createdUserMono = this.service.register(user);
    return createdUserMono
        .mapNotNull(
            createdUser -> {
              log.info("created user");
              return createdUser;
            })
        .flatMap(this::toDTO)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .defaultIfEmpty(
            ResponseEntity.badRequest().body(new ErrorDTO("Invalid email or password given")));
  }

  private Mono<UserDTO> toDTO(User user) {
    if (user.id().isEmpty()) {
      log.error("Created user with empty user id for email: {}", user.email());
      return Mono.empty();
    }
    return Mono.just(new UserDTO(user.id().get().toString(), user.email(), user.role().value));
  }
}
