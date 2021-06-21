package com.github.diabetesassistant.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordCrypt {
  private final Argon2PasswordEncoder encoder;

  @Autowired
  public PasswordCrypt() {
    this.encoder = new Argon2PasswordEncoder(16, 32, 5, 1024, 10);
  }

  public String encode(String plain) {
    return this.encoder.encode(plain);
  }

  public boolean isEqual(String plain, String cipher) {
    return this.encoder.matches(plain, cipher);
  }
}
