package com.github.diabetesassistant.user.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.diabetesassistant.core.presentation.ErrorDTO;
import com.github.diabetesassistant.user.domain.Role;
import com.github.diabetesassistant.user.domain.User;
import com.github.diabetesassistant.user.domain.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserHandler.class)
public class UserHandlerTest {
  @MockBean private UserService serviceMock;

  @Autowired private WebTestClient webTestClient;

  @Test
  void shouldReturnCreatedUser() {
    UUID userId = UUID.randomUUID();
    User createdUser = new User(Optional.of(userId), "foo@bar.com", "secret", Role.PATIENT);
    when(this.serviceMock.register(any())).thenReturn(Mono.just(createdUser));

    UserDTO expected = new UserDTO(userId.toString(), "foo@bar.com", "patient");
    this.webTestClient
        .post()
        .uri("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Mono.just(new UserCreationRequestDTO("foo@bar.com", "secret")),
            UserCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserDTO.class)
        .value(response -> assertEquals(expected, response));
  }

  @Test
  void shouldReturn400WhenUserHasNoId() {
    User createdUser = new User(Optional.empty(), "foo@bar.com", "secret", null);
    when(this.serviceMock.register(any())).thenReturn(Mono.just(createdUser));

    this.webTestClient
        .post()
        .uri("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Mono.just(new UserCreationRequestDTO("foo@bar.com", "secret")),
            UserCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(ErrorDTO.class)
        .value(response -> assertEquals(ErrorDTO.class, response.getClass()));
  }

  @Test
  void shouldReturn400WhenNoUserCreated() {
    when(this.serviceMock.register(any())).thenReturn(Mono.empty());

    this.webTestClient
        .post()
        .uri("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Mono.just(new UserCreationRequestDTO("foo@bar.com", "secret")),
            UserCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(ErrorDTO.class)
        .value(response -> assertEquals(ErrorDTO.class, response.getClass()));
  }
}
