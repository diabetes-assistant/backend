package com.github.diabetesassistant.presentation;

import com.github.diabetesassistant.domain.AuthService;
import com.github.diabetesassistant.domain.Tokens;
import com.github.diabetesassistant.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
public class AuthHandler {
  private final AuthService service;

  @PostMapping("/auth/token")
  public Mono<TokenDTO> createToken(UserDTO userDTO) {
    User user = new User(userDTO.getEmail(), userDTO.getPassword());
    Mono<Tokens> tokens = this.service.authenticate(user);
    return tokens.map(token -> new TokenDTO(token.getAccessToken(), token.getIdToken()));
  }
}
