package com.github.diabetesassistant.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.diabetesassistant.data.*;
import java.time.LocalDateTime;
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
class AuthServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private TokenRepository tokenRepository;
  private AuthService testee;

  @BeforeEach
  void setUp() {
    this.testee = new AuthService(this.userRepository, this.tokenRepository);
  }

  @Test
  void shouldReturnCreatedToken() {
    UserEntity existingUser =
        new UserEntity(UUID.randomUUID(), "foo@bar.com", "secret", RoleEntity.DOCTOR);
    when(this.userRepository.findByEmailAndPassword(anyString(), anyString()))
        .thenReturn(Mono.just(existingUser));
    TokenEntity createdToken =
        new TokenEntity(TokenTypeEntity.ID_TOKEN, existingUser.getId(), LocalDateTime.now());
    when(this.tokenRepository.create(any(TokenEntity.class))).thenReturn(Mono.just(createdToken));
    User user = new User("foo@bar.com", "secret");

    Mono<Tokens> actual = this.testee.authenticate(user);
    AccessToken expectedAccessToken = new AccessToken(existingUser.getId(), List.of(Role.DOCTOR));
    IDToken expectedIDToken = new IDToken(existingUser.getId(), existingUser.getEmail());
    Tokens expected = new Tokens(expectedAccessToken, expectedIDToken);

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldReturnEmptyWhenUserNotFound() {
    when(this.userRepository.findByEmailAndPassword(anyString(), anyString()))
        .thenReturn(Mono.empty());
    User user = new User("foo@bar.com", "secret");

    Mono<Tokens> actual = this.testee.authenticate(user);

    StepVerifier.create(actual.log()).verifyComplete();
  }
}