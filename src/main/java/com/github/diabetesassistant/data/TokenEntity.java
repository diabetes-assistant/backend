package com.github.diabetesassistant.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Value
@Table("tokens")
@SuppressFBWarnings(value = "EI_EXPOSE_REP")
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
