package com.github.diabetesassistant.user.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.diabetesassistant.core.domain.PasswordCrypt;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock private UserRepository repository;
  @Mock private PasswordCrypt passwordCrypt;
  private UserService testee;

  @BeforeEach
  void setUp() {
    this.testee = new UserService(this.repository, this.passwordCrypt);
  }

  @Test
  void shouldReturnCreatedDoctor() {
    UUID userId = UUID.randomUUID();
    UserEntity createdUser = new UserEntity(userId, "foo@bar.com", "secret", "doctor");
    when(this.repository.save(any())).thenReturn(Mono.just(createdUser));
    User toBeCreated = new User(Optional.empty(), "foo@bar.com", "secret", Role.DOCTOR);

    Mono<User> actual = this.testee.register(toBeCreated);
    User expected = new User(Optional.of(userId), "foo@bar.com", "secret", Role.DOCTOR);

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
    verify(this.passwordCrypt, times(1)).encode("secret");
  }

  @Test
  void shouldReturnEmptyWhenRepositoryEmpty() {
    when(this.repository.save(any())).thenReturn(Mono.empty());
    User toBeCreated = new User(Optional.empty(), "foo@bar.com", "secret", Role.DOCTOR);

    Mono<User> actual = this.testee.register(toBeCreated);

    StepVerifier.create(actual.log()).verifyComplete();
    verify(this.passwordCrypt, times(1)).encode("secret");
  }
}
