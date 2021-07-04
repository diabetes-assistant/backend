package com.github.diabetesassistant.auth.domain;

import com.github.diabetesassistant.auth.data.*;
import com.github.diabetesassistant.core.domain.PasswordCrypt;
import com.github.diabetesassistant.user.data.UserEntity;
import com.github.diabetesassistant.user.data.UserRepository;
import com.github.diabetesassistant.user.domain.Role;
import com.github.diabetesassistant.user.domain.User;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final PasswordCrypt passwordCrypt;

  public Mono<Tokens> authenticate(User user) {
    log.info("authenticating user");
    Mono<UserEntity> existingUser = this.userRepository.findByEmail(user.email());
    Mono<UserEntity> onlyValidUser =
        existingUser.filter(
            userEntity -> this.passwordCrypt.isEqual(user.password(), userEntity.password()));
    Mono<TokenEntity> accessTokenEntity =
        onlyValidUser.map(toTokenEntity(TokenTypeEntity.ACCESS_TOKEN));
    Mono<TokenEntity> createdAccessToken = accessTokenEntity.flatMap(tokenRepository::save);
    Mono<AccessToken> accessToken =
        Mono.zip(onlyValidUser, createdAccessToken).map(this::toAccessToken);
    Mono<TokenEntity> idTokenEntity = onlyValidUser.map(toTokenEntity(TokenTypeEntity.ID_TOKEN));
    Mono<TokenEntity> createdIdToken = idTokenEntity.flatMap(tokenRepository::save);
    Mono<IDToken> idToken =
        createdIdToken.map(token -> new IDToken(token.getUserId(), user.email()));
    return Mono.zip(accessToken, idToken).map(a -> new Tokens(a.getT1(), a.getT2()));
  }

  private Function<UserEntity, TokenEntity> toTokenEntity(TokenTypeEntity tokenType) {
    return (UserEntity userEntity) ->
        new TokenEntity(tokenType, userEntity.id(), LocalDateTime.now());
  }

  private AccessToken toAccessToken(Tuple2<UserEntity, TokenEntity> userAndToken) {
    UserEntity user = userAndToken.getT1();
    Optional<Role> maybeRole =
        Arrays.stream(Role.values()).filter(role -> role.value.equals(user.role())).findFirst();
    return new AccessToken(user.id(), maybeRole.map(List::of).orElse(null));
  }
}
