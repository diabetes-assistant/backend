package com.github.diabetesassistant.user.presentation;

import com.github.diabetesassistant.core.presentation.ErrorDTO;
import com.github.diabetesassistant.user.domain.Role;
import com.github.diabetesassistant.user.domain.User;
import com.github.diabetesassistant.user.domain.UserService;
import java.util.Optional;
import java.util.UUID;
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
    log.info("Start handling incoming user creation request");
    Mono<User> userMono = this.toUser(dto);
    Mono<User> createdUserMono = userMono.flatMap(this.service::register);
    return createdUserMono
        .mapNotNull(
            createdUser -> {
              log.info("Done handling incoming user creation request");
              return createdUser;
            })
        .flatMap(this::toDTO)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .defaultIfEmpty(
            ResponseEntity.badRequest()
                .body(new ErrorDTO("Invalid email, password or role given")));
  }

  private Mono<User> toUser(UserCreationRequestDTO dto) {
    Optional<Role> role = Role.fromString(dto.role());
    if (role.isEmpty()) {
      return Mono.empty();
    }
    User user = new User(Optional.empty(), dto.email(), dto.password(), role.get());
    return Mono.just(user);
  }

  private Mono<UserDTO> toDTO(User user) {
    UUID userId = user.id().orElse(UUID.randomUUID());
    return Mono.just(new UserDTO(userId.toString(), user.email(), user.role().value));
  }
}
