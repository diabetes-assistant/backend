package com.github.diabetesassistant.user.data;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record UserEntity(@Id UUID id, String email, String password, String role) {}
