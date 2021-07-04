package com.github.diabetesassistant.auth.presentation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.github.diabetesassistant.auth.domain.*;
import com.github.diabetesassistant.core.presentation.ErrorDTO;
import com.github.diabetesassistant.user.domain.User;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(
    value = "/auth",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AuthHandler {
  private final AuthService service;
  private final Algorithm accessTokenAlgorithm;
  private final Algorithm idTokenAlgorithm;
  static final ZoneId BERLIN_ZONE = ZoneId.of("Europe/Berlin");

  @Autowired
  public AuthHandler(
      AuthService service,
      @Value("${auth.accessTokenSecret}") String accessTokenSecret,
      @Value("${auth.idTokenSecret}") String idTokenSecret) {
    this.service = service;
    this.accessTokenAlgorithm = Algorithm.HMAC512(accessTokenSecret);
    this.idTokenAlgorithm = Algorithm.HMAC512(idTokenSecret);
  }

  @PostMapping("/token")
  public Mono<ResponseEntity<?>> createToken(
      @RequestBody TokenCreationRequestDTO tokenCreationRequestDTO) {
    log.info("handling incoming token creation request");
    User user =
        new User(
            Optional.empty(),
            tokenCreationRequestDTO.email(),
            tokenCreationRequestDTO.password(),
            null);
    Mono<Tokens> tokens = this.service.authenticate(user);
    return tokens
        .mapNotNull(
            createdTokens -> {
              log.info("created tokens");
              return createdTokens;
            })
        .map(this::toDTO)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.badRequest().body(new ErrorDTO("Invalid user given")));
  }

  private TokenDTO toDTO(Tokens tokens) throws JWTCreationException {
    AccessToken accessToken = tokens.accessToken();
    IDToken idToken = tokens.idToken();
    List<String> roles =
        accessToken.roles().stream().map(Objects::toString).collect(Collectors.toList());
    String userId = accessToken.userId().toString();
    LocalDateTime now = LocalDateTime.now();
    ZoneOffset zoneOffset = BERLIN_ZONE.getRules().getOffset(now);
    String accessJWT =
        JWT.create()
            .withIssuer("diabetes-assistant-backend")
            .withAudience("diabetes-assistant-client")
            .withSubject(userId)
            .withClaim("diabetesAssistant:roles", String.join(",", roles))
            .withExpiresAt(Date.from(now.plusHours(10L).toInstant(zoneOffset)))
            .sign(this.accessTokenAlgorithm);
    String idJWT =
        JWT.create()
            .withIssuer("diabetes-assistant-backend")
            .withAudience("diabetes-assistant-client")
            .withSubject(userId)
            .withClaim("email", idToken.email())
            .withExpiresAt(Date.from(now.plusDays(1L).toInstant(zoneOffset)))
            .sign(this.idTokenAlgorithm);
    return new TokenDTO(accessJWT, idJWT);
  }
}
