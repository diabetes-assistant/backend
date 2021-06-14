package com.github.diabetesassistant.presentation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.github.diabetesassistant.domain.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
public class AuthHandler {
  private final AuthService service;
  private final Algorithm accessTokenAlgorithm;
  private final Algorithm idTokenAlgorithm;

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
  public Mono<ResponseEntity<?>> createToken(@RequestBody UserDTO userDTO) {
    User user = new User(userDTO.getEmail(), userDTO.getPassword());
    Mono<Tokens> tokens = this.service.authenticate(user);
    return tokens
        .map(this::toDTO)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.badRequest().body(new ErrorDTO("Invalid user given")));
  }

  private TokenDTO toDTO(Tokens tokens) throws JWTCreationException {
    AccessToken accessToken = tokens.getAccessToken();
    IDToken idToken = tokens.getIdToken();
    List<String> roles =
        accessToken.getRoles().stream().map(Objects::toString).collect(Collectors.toList());
    String userId = accessToken.getUserId().toString();
    String accessJWT =
        JWT.create()
            .withIssuer("diabetes-assistant-backend")
            .withAudience("diabetes-assistant-client")
            .withSubject(userId)
            .withClaim("diabetesAssistant:roles", String.join(",", roles))
            .sign(this.accessTokenAlgorithm);
    String idJWT =
        JWT.create()
            .withIssuer("diabetes-assistant-backend")
            .withAudience("diabetes-assistant-client")
            .withSubject(userId)
            .withClaim("email", idToken.getEmail())
            .sign(this.idTokenAlgorithm);
    return new TokenDTO(accessJWT, idJWT);
  }
}
