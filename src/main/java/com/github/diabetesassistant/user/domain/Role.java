package com.github.diabetesassistant.user.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Role {
  PATIENT("patient"),
  DOCTOR("doctor");

  public final String value;

  Role(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Optional<Role> fromString(String value) {
    return Arrays.stream(Role.values()).filter(role -> role.value.equals(value)).findFirst();
  }
}
