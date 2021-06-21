package com.github.diabetesassistant.domain;

import com.github.diabetesassistant.data.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@AllArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;

  public Mono<Tokens> authenticate(User user) {
    Mono<UserEntity> existingUser =
        this.userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());
    Mono<TokenEntity> accessTokenEntity =
        existingUser.map(toTokenEntity(TokenTypeEntity.ACCESS_TOKEN));
    Mono<TokenEntity> createdAccessToken = accessTokenEntity.flatMap(tokenRepository::save);
    Mono<AccessToken> accessToken =
        Mono.zip(existingUser, createdAccessToken).map(this::toAccessToken);
    Mono<TokenEntity> idTokenEntity = existingUser.map(toTokenEntity(TokenTypeEntity.ID_TOKEN));
    Mono<TokenEntity> createdIdToken = idTokenEntity.flatMap(tokenRepository::save);
    Mono<IDToken> idToken =
        createdIdToken.map(token -> new IDToken(token.getUserId(), user.getEmail()));
    return Mono.zip(accessToken, idToken).map(a -> new Tokens(a.getT1(), a.getT2()));
  }

  private Function<UserEntity, TokenEntity> toTokenEntity(TokenTypeEntity tokenType) {
    return (UserEntity userEntity) ->
        new TokenEntity(tokenType, userEntity.getId(), LocalDateTime.now());
  }

  private AccessToken toAccessToken(Tuple2<UserEntity, TokenEntity> userAndToken) {
    UserEntity user = userAndToken.getT1();
    Optional<Role> maybeRole =
        Arrays.stream(Role.values()).filter(role -> role.value.equals(user.getRole())).findFirst();
    return new AccessToken(user.getId(), maybeRole.map(List::of).orElse(null));
  }
}
