package com.github.diabetesassistant.core.presentation;

import static com.github.diabetesassistant.core.presentation.TokenFactory.ROLES_CLAIM;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
  private static final String[] NOT_PROTECTED_RESOURCES = new String[] {"/user/**", "/auth/**"};
  private static final String FRONTEND_LOCALHOST = "localhost";
  private static final String FRONTEND_STAGING =
      "https://staging-diabetes-assitant-fe.herokuapp.com";
  private static final String FRONTEND_LIVE = "https://live-diabetes-assitant-fe.herokuapp.com";

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(FRONTEND_LOCALHOST, FRONTEND_STAGING, FRONTEND_LIVE));
    configuration.setAllowedMethods(List.of("PUT", "POST", "GET", "OPTION", "DELETE"));
    configuration.addAllowedHeader("Authorization");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  ServerAuthenticationConverter authenticationConverter() {
    return (ServerWebExchange exchange) -> {
      Mono<ServerHttpRequest> requestMono = Mono.justOrEmpty(exchange.getRequest());
      Mono<HttpHeaders> requestHeaders = requestMono.map(HttpMessage::getHeaders);
      Mono<String> authorizationHeader =
          requestHeaders.mapNotNull(headers -> headers.getFirst(HttpHeaders.AUTHORIZATION));
      authorizationHeader.map(
          ah -> {
            log.info("got token-------> " + ah);
            return ah;
          });
      return authorizationHeader.map(
          token -> new UsernamePasswordAuthenticationToken(token, token));
    };
  }

  @Bean
  ReactiveAuthenticationManager authenticationManager(TokenFactory tokenFactory) {
    return (Authentication authentication) -> {
      Mono<String> jwt = Mono.just(authentication.getCredentials().toString());
      Mono<DecodedJWT> decodedToken =
          jwt.map(tokenFactory::verifyAccessToken)
              .onErrorResume(
                  (exception) -> {
                    log.warn("was not able to verify token", exception);
                    return Mono.empty();
                  });
      return decodedToken.map(
          token -> {
            String[] rawRoles = token.getClaim(ROLES_CLAIM).asString().split(",");
            List<SimpleGrantedAuthority> roles =
                Arrays.stream(rawRoles).map(SimpleGrantedAuthority::new).toList();
            return new UsernamePasswordAuthenticationToken(
                token.getSubject(), authentication.getCredentials(), roles);
          });
    };
  }

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.cors()
        .configurationSource(corsConfigurationSource())
        .and()
        .csrf()
        .disable()
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers(NOT_PROTECTED_RESOURCES)
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .httpBasic()
        .disable()
        .formLogin()
        .disable()
        .logout()
        .disable();
    return http.build();
  }
}
