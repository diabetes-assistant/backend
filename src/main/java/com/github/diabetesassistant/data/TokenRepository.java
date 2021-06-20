package com.github.diabetesassistant.data;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TokenRepository {
  public Mono<TokenEntity> create(TokenEntity entity) {
    return null;
  }
}
