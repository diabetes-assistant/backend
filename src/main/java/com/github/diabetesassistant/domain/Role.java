package com.github.diabetesassistant.domain;

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
}
