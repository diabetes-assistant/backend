package com.github.diabetesassistant.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.diabetesassistant.domain.AuthService;
import com.github.diabetesassistant.domain.Tokens;
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
    this.testee = new AuthHandler(serviceMock);
  }

  @Test
  void shouldReturnLoggedInTokens() {
    UserDTO userDTO = new UserDTO("foo@bar.com", "secret");
    Tokens loggedInToken = new Tokens("foo", "bar");
    when(this.serviceMock.authenticate(any())).thenReturn(Mono.just(loggedInToken));

    Mono<TokenDTO> actual = testee.createToken(userDTO);
    TokenDTO expected = new TokenDTO(loggedInToken.getAccessToken(), loggedInToken.getIdToken());

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }
}
