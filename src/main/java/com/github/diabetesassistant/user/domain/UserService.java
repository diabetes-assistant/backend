package com.github.diabetesassistant.user.domain;

import com.github.diabetesassistant.core.domain.PasswordCrypt;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
  UserRepository repository;
  PasswordCrypt passwordCrypt;

  public Mono<User> register(User user) {
    log.info("Registering user");
    UserEntity toBeCreated = new UserEntity(null, user.email(), user.password(), user.role().value);
    Mono<UserEntity> toBeCreatedUser = Mono.just(toBeCreated);
    Mono<UserEntity> userWithEncryptedPassword =
        toBeCreatedUser.map(
            userEntity -> {
              String encryptedPassword = passwordCrypt.encode(userEntity.password());
              return new UserEntity(null, userEntity.email(), encryptedPassword, userEntity.role());
            });
    Mono<UserEntity> createdUser = userWithEncryptedPassword.flatMap(this.repository::save);
    return createdUser.flatMap(this::toUser);
  }

  private Mono<User> toUser(UserEntity userEntity) {
    Optional<Role> role = Role.fromString(userEntity.role());
    if (role.isEmpty()) {
      return Mono.empty();
    }
    Optional<UUID> userId = Optional.of(userEntity.id());
    User user = new User(userId, userEntity.email(), userEntity.password(), role.get());
    log.info("Registering user was successful");
    return Mono.just(user);
  }
}
