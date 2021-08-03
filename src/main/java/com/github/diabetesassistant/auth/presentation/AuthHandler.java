package com.github.diabetesassistant.auth.presentation;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.github.diabetesassistant.auth.domain.*;
import com.github.diabetesassistant.core.presentation.TokenFactory;
import com.github.diabetesassistant.user.domain.User;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(
    value = "/auth",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class AuthHandler {
  private final AuthService service;
  private final TokenFactory tokenFactory;

  @PostMapping("/token")
  public Mono<TokenDTO> createToken(@RequestBody TokenCreationRequestDTO dto) {
    log.info("handling incoming token creation request");
    User user = new User(Optional.empty(), dto.email(), dto.password(), null);
    Mono<Tokens> tokens = this.service.authenticate(user);
    return tokens
        .mapNotNull(
            createdTokens -> {
              log.info("created tokens");
              return createdTokens;
            })
        .map(this::toDTO)
        .switchIfEmpty(
            Mono.error(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid doctorId given")));
  }

  private TokenDTO toDTO(Tokens tokens) throws JWTCreationException {
    AccessToken accessToken = tokens.accessToken();
    IDToken idToken = tokens.idToken();
    List<String> roles =
        accessToken.roles().stream().map(Objects::toString).collect(Collectors.toList());
    String userId = accessToken.userId().toString();
    String accessJWT = this.tokenFactory.createAccessToken(userId, roles);
    String idJWT = this.tokenFactory.createIdToken(userId, idToken.email());
    return new TokenDTO(accessJWT, idJWT);
  }
}
