package com.github.diabetesassistant.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.diabetesassistant.domain.*;
import java.util.List;
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
@WebFluxTest(controllers = AuthHandler.class)
public class AuthHandlerTest {
  @MockBean private AuthService serviceMock;

  @Autowired private WebTestClient webTestClient;

  @Test
  void shouldReturnLoggedInTokens() {
    UUID userId = UUID.randomUUID();
    AccessToken accessToken = new AccessToken(userId, List.of(Role.PATIENT));
    IDToken idToken = new IDToken(userId, "foo@bar.com");
    Tokens loggedInToken = new Tokens(accessToken, idToken);
    when(this.serviceMock.authenticate(any())).thenReturn(Mono.just(loggedInToken));
    Algorithm accessTokenAlgorithm = Algorithm.HMAC512("access-secret");
    Algorithm idTokenAlgorithm = Algorithm.HMAC512("id-secret");
    UserDTO userDTO = new UserDTO("foo@bar.com", "secret");

    String expectedAccessToken =
        JWT.create()
            .withIssuer("diabetes-assistant-backend")
            .withAudience("diabetes-assistant-client")
            .withSubject(userId.toString())
            .withClaim("diabetesAssistant:roles", accessToken.getRoles().get(0).value)
            .sign(accessTokenAlgorithm);
    String expectedIdToken =
        JWT.create()
            .withIssuer("diabetes-assistant-backend")
            .withAudience("diabetes-assistant-client")
            .withSubject(userId.toString())
            .withClaim("email", idToken.getEmail())
            .sign(idTokenAlgorithm);

    this.webTestClient
        .post()
        .uri("/auth/token")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(userDTO), UserDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(TokenDTO.class)
        .value(
            response -> {
              assertEquals(expectedAccessToken, response.getAccessToken());
              assertEquals(expectedIdToken, response.getIdToken());
            });
  }

  @Test
  void shouldReturn400WhenNoTokenCreated() {
    when(this.serviceMock.authenticate(any())).thenReturn(Mono.empty());
    UserDTO userDTO = new UserDTO("foo@bar.com", "secret");

    ErrorDTO expected = new ErrorDTO("Invalid user given");
    this.webTestClient
        .post()
        .uri("/auth/token")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(userDTO), UserDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(ErrorDTO.class)
        .value(response -> assertEquals(expected, response));
  }

  @Test
  void shouldReturn500OnError() {
    when(this.serviceMock.authenticate(any())).thenThrow(new IllegalArgumentException("foo"));
    UserDTO userDTO = new UserDTO("foo@bar.com", "secret");

    this.webTestClient
        .post()
        .uri("/auth/token")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(userDTO), UserDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }
}
