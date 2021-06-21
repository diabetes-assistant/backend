package com.github.diabetesassistant.data;

import java.util.UUID;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Value
@Table("users")
public class UserEntity {
  @Id UUID id;
  String email;
  String password;
  String role;
}
