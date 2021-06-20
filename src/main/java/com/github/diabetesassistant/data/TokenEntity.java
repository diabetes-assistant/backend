package com.github.diabetesassistant.data;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class TokenEntity {
  TokenTypeEntity tokenType;
  UUID user;
  LocalDateTime createdAt;
}
