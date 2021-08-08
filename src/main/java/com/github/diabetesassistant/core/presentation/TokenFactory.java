package com.github.diabetesassistant.core.presentation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenFactory {
  private static final ZoneId BERLIN_ZONE = ZoneId.of("Europe/Berlin");
  private static final long ACCESS_TOKEN_DURATION_IN_HOURS = 10L;
  private static final long ID_TOKEN_DURATION_IN_HOURS = 10L;
  private static final String ISSUER = "diabetes-assistant-backend";
  private static final String CLIENT = "diabetes-assistant-client";
  private static final String BEARER_PREFIX = "Bearer ";
  public static final String ROLES_CLAIM = "diabetesAssistant:roles";
  private final Algorithm accessTokenAlgorithm;
  private final Algorithm idTokenAlgorithm;

  @Autowired
  public TokenFactory(
      @Value("${auth.accessTokenSecret}") String accessTokenSecret,
      @Value("${auth.idTokenSecret}") String idTokenSecret) {
    this.accessTokenAlgorithm = Algorithm.HMAC512(accessTokenSecret);
    this.idTokenAlgorithm = Algorithm.HMAC512(idTokenSecret);
  }

  public String createAccessToken(String userId, List<String> roles) {
    LocalDateTime now = LocalDateTime.now();
    ZoneOffset zoneOffset = BERLIN_ZONE.getRules().getOffset(now);
    LocalDateTime expiry = now.plusHours(ACCESS_TOKEN_DURATION_IN_HOURS);

    return JWT.create()
        .withIssuer(ISSUER)
        .withAudience(CLIENT)
        .withSubject(userId)
        .withClaim(ROLES_CLAIM, String.join(",", roles))
        .withExpiresAt(Date.from(expiry.toInstant(zoneOffset)))
        .sign(this.accessTokenAlgorithm);
  }

  public DecodedJWT verifyAccessToken(String authorizationHeader) throws JWTVerificationException {
    if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
      throw new JWTVerificationException("Missing 'Bearer ' prefix");
    }
    String accessToken = authorizationHeader.replace(BEARER_PREFIX, "");
    JWTVerifier verifier =
        JWT.require(this.accessTokenAlgorithm)
            .withIssuer(ISSUER)
            .withAudience(CLIENT)
            .acceptLeeway(1337L)
            .build();
    return verifier.verify(accessToken);
  }

  public String createIdToken(String userId, String email) {
    LocalDateTime now = LocalDateTime.now();
    ZoneOffset zoneOffset = BERLIN_ZONE.getRules().getOffset(now);
    LocalDateTime expiry = now.plusHours(ID_TOKEN_DURATION_IN_HOURS);
    return JWT.create()
        .withIssuer(ISSUER)
        .withAudience(CLIENT)
        .withSubject(userId)
        .withClaim("email", email)
        .withExpiresAt(Date.from(expiry.toInstant(zoneOffset)))
        .sign(this.idTokenAlgorithm);
  }
}
