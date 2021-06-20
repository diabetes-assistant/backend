package com.github.diabetesassistant.data;

import java.util.UUID;
import lombok.Value;

@Value
public class UserEntity {
  UUID id;
  String email;
  String password;
  String role;
}
