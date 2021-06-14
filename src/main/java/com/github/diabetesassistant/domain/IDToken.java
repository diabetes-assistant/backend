package com.github.diabetesassistant.domain;

import java.util.UUID;
import lombok.Value;

@Value
public class IDToken {
  UUID userId;
  String email;
}
