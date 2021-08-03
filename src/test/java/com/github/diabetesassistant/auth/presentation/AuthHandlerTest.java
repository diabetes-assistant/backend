package com.github.diabetesassistant.auth.presentation;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.diabetesassistant.auth.domain.*;
import com.github.diabetesassistant.core.presentation.SecurityConfig;
import com.github.diabetesassistant.core.presentation.TokenFactory;
import com.github.diabetesassistant.user.domain.Role;
import java.util.List;
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
@WebFluxTest(controllers = AuthHandler.class)
@Import(SecurityConfig.class)
public class AuthHandlerTest {
  @MockBean private AuthService serviceMock;
  @MockBean private TokenFactory tokenFactory;
  @Autowired private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    when(tokenFactory.verifyAccessToken(anyString())).thenReturn(mock(DecodedJWT.class));
  }

  @Test
  void shouldReturnLoggedInTokens() {
    UUID userId = UUID.randomUUID();
    AccessToken accessToken = new AccessToken(userId, List.of(Role.PATIENT));
    IDToken idToken = new IDToken(userId, "foo@bar.com");
    Tokens loggedInToken = new Tokens(accessToken, idToken);
    when(this.serviceMock.authenticate(any())).thenReturn(Mono.just(loggedInToken));
    TokenCreationRequestDTO tokenCreationRequestDTO =
        new TokenCreationRequestDTO("foo@bar.com", "secret");

    this.webTestClient
        .post()
        .uri("/auth/token")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(tokenCreationRequestDTO), TokenCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(TokenDTO.class)
        .value(
            response -> {
              assertNotEquals("", response.accessToken());
              assertNotEquals("", response.idToken());
            });
  }

  @Test
  void shouldReturn400WhenNoTokenCreated() {
    when(this.serviceMock.authenticate(any())).thenReturn(Mono.empty());
    TokenCreationRequestDTO tokenCreationRequestDTO =
        new TokenCreationRequestDTO("foo@bar.com", "secret");

    this.webTestClient
        .post()
        .uri("/auth/token")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(tokenCreationRequestDTO), TokenCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void shouldReturn500OnError() {
    when(this.serviceMock.authenticate(any())).thenThrow(new IllegalArgumentException("foo"));
    TokenCreationRequestDTO tokenCreationRequestDTO =
        new TokenCreationRequestDTO("foo@bar.com", "secret");

    this.webTestClient
        .post()
        .uri("/auth/token")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(tokenCreationRequestDTO), TokenCreationRequestDTO.class)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }
}
