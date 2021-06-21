package com.github.diabetesassistant.data;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Value
@Table("tokens")
public class TokenEntity {
  @Id UUID id;
  TokenTypeEntity type;
  UUID userId;
  LocalDateTime createdAt;

  @PersistenceConstructor
  public TokenEntity(UUID id, TokenTypeEntity type, UUID userId, LocalDateTime createdAt) {
    this.id = id;
    this.type = type;
    this.userId = userId;
    this.createdAt = createdAt;
  }

  public TokenEntity(TokenTypeEntity type, UUID userId, LocalDateTime createdAt) {
    this.type = type;
    this.userId = userId;
    this.createdAt = createdAt;
    this.id = null;
  }
}
