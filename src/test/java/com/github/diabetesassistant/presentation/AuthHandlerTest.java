package com.github.diabetesassistant.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.diabetesassistant.domain.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AuthHandlerTest {
  @Mock private AuthService serviceMock;

  private AuthHandler testee;

  @BeforeEach
  void setUp() {
    this.testee = new AuthHandler(serviceMock, "access-secret", "id-secret");
  }

  @Test
  void shouldReturnLoggedInTokens() {
    UserDTO userDTO = new UserDTO("foo@bar.com", "secret");
    UUID userId = UUID.randomUUID();
    AccessToken accessToken = new AccessToken(userId, List.of(Role.PATIENT));
    IDToken idToken = new IDToken(userId, "foo@bar.com");
    Tokens loggedInToken = new Tokens(accessToken, idToken);
    when(this.serviceMock.authenticate(any())).thenReturn(Mono.just(loggedInToken));
    Algorithm accessTokenAlgorithm = Algorithm.HMAC512("access-secret");
    Algorithm idTokenAlgorithm = Algorithm.HMAC512("id-secret");

    Mono<TokenDTO> actual = testee.createToken(userDTO);
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
    TokenDTO expected = new TokenDTO(expectedAccessToken, expectedIdToken);

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }
}
