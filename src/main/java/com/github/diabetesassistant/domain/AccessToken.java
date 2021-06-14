package com.github.diabetesassistant.domain;

import java.util.List;
import java.util.UUID;
import lombok.Value;

@Value
public class AccessToken {
  UUID userId;
  List<Role> roles;
}
