package com.github.diabetesassistant.user.domain;

import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class UserService {
  UserRepository repository;

  public Mono<User> register(User user) {
    UserEntity toBeCreated = new UserEntity(null, user.email(), user.password(), user.role().value);
    Mono<UserEntity> createdUser = this.repository.save(toBeCreated);
    return createdUser.flatMap(this::toUser);
  }

  private Mono<User> toUser(UserEntity userEntity) {
    Optional<Role> role = Role.fromString(userEntity.role());
    if (role.isEmpty()) {
      return Mono.empty();
    }
    Optional<UUID> userId = Optional.of(userEntity.id());
    User user = new User(userId, userEntity.email(), userEntity.password(), role.get());
    return Mono.just(user);
  }
}
