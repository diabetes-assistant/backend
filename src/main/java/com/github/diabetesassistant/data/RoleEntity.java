package com.github.diabetesassistant.data;

public enum RoleEntity {
  PATIENT("patient"),
  DOCTOR("doctor");

  public final String value;

  RoleEntity(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
