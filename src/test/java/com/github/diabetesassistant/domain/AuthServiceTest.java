package com.github.diabetesassistant.domain;

import static com.github.diabetesassistant.domain.Role.DOCTOR;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.diabetesassistant.data.*;
import java.time.LocalDateTime;
import java.util.List;
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
  @Mock private PasswordCrypt passwordCrypt;
  private AuthService testee;

  @BeforeEach
  void setUp() {
    this.testee = new AuthService(this.userRepository, this.tokenRepository, this.passwordCrypt);
  }

  @Test
  void shouldReturnCreatedToken() {
    UserEntity existingUser = new UserEntity(randomUUID(), "foo@bar.com", "secret", DOCTOR.value);
    when(this.userRepository.findByEmail(anyString())).thenReturn(Mono.just(existingUser));
    TokenEntity createdToken =
        new TokenEntity(TokenTypeEntity.ID_TOKEN, existingUser.id(), LocalDateTime.now());
    when(this.tokenRepository.save(any(TokenEntity.class))).thenReturn(Mono.just(createdToken));
    when(this.passwordCrypt.isEqual(anyString(), anyString())).thenReturn(true);
    User user = new User("foo@bar.com", "secret");

    Mono<Tokens> actual = this.testee.authenticate(user);
    AccessToken expectedAccessToken = new AccessToken(existingUser.id(), List.of(DOCTOR));
    IDToken expectedIDToken = new IDToken(existingUser.id(), existingUser.email());
    Tokens expected = new Tokens(expectedAccessToken, expectedIDToken);

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldReturnEmptyWhenUserNotFound() {
    when(this.userRepository.findByEmail(anyString())).thenReturn(Mono.empty());
    User user = new User("foo@bar.com", "secret");

    Mono<Tokens> actual = this.testee.authenticate(user);

    StepVerifier.create(actual.log()).verifyComplete();
  }

  @Test
  void shouldReturnEmptyWhenPasswordIsInvalid() {
    UserEntity existingUser = new UserEntity(randomUUID(), "foo@bar.com", "secret", DOCTOR.value);
    when(this.userRepository.findByEmail(anyString())).thenReturn(Mono.just(existingUser));
    when(this.passwordCrypt.isEqual(anyString(), anyString())).thenReturn(false);
    User user = new User("foo@bar.com", "secret");

    Mono<Tokens> actual = this.testee.authenticate(user);

    StepVerifier.create(actual.log()).verifyComplete();
  }
}
