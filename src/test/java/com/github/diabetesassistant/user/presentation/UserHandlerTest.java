package com.github.diabetesassistant.user.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.diabetesassistant.core.presentation.ErrorDTO;
import com.github.diabetesassistant.core.presentation.SecurityConfig;
import com.github.diabetesassistant.core.presentation.TokenFactory;
import com.github.diabetesassistant.user.domain.Role;
import com.github.diabetesassistant.user.domain.User;
import com.github.diabetesassistant.user.domain.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = UserHandler.class)
@Import(SecurityConfig.class)
public class UserHandlerTest {
  @MockBean private UserService serviceMock;
  @MockBean private TokenFactory tokenFactory;
  @Autowired private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    when(tokenFactory.verifyAccessToken(anyString())).thenReturn(mock(DecodedJWT.class));
  }

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
            Mono.just(new UserCreationRequestDTO("foo@bar.com", "secret", "patient")),
            UserCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserDTO.class)
        .value(response -> assertEquals(expected, response));
  }

  @Test
  void shouldReturnUserRequestWhenUserHasNoId() {
    User createdUser = new User(Optional.empty(), "foo@bar.com", "secret", Role.DOCTOR);
    when(this.serviceMock.register(any())).thenReturn(Mono.just(createdUser));
    UserCreationRequestDTO creationRequest =
        new UserCreationRequestDTO("foo@bar.com", "secret", "doctor");

    UserDTO expected = new UserDTO("", creationRequest.email(), creationRequest.role());
    this.webTestClient
        .post()
        .uri("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(creationRequest), UserCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserDTO.class)
        .value(
            response -> {
              assertNotNull(response.id());
              assertEquals(expected.email(), response.email());
              assertEquals(expected.role(), response.role());
            });
  }

  @Test
  void shouldReturn400WhenNoUserCreated() {
    when(this.serviceMock.register(any())).thenReturn(Mono.empty());

    this.webTestClient
        .post()
        .uri("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Mono.just(new UserCreationRequestDTO("foo@bar.com", "secret", "doctor")),
            UserCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(ErrorDTO.class)
        .value(response -> assertEquals(ErrorDTO.class, response.getClass()));
  }
}
