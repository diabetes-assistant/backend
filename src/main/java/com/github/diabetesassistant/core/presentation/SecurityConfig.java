package com.github.diabetesassistant.core.presentation;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
  private static final String[] NOT_PROTECTED_RESOURCES = new String[] {"/user/**", "/auth/**"};
  private static final String FRONTEND_LOCALHOST = "localhost";
  private static final String FRONTEND_STAGING = "staging-diabetes-assitant-fe.herokuapp.com";
  private static final String FRONTEND_LIVE = "live-diabetes-assitant-fe.herokuapp.com";
  private static final List<String> ALLOWED_ORIGINS =
      List.of(FRONTEND_LOCALHOST, FRONTEND_STAGING, FRONTEND_LIVE);
  private static final List<String> ALLOWED_METHODS =
      List.of("PUT", "POST", "GET", "OPTION", "DELETE");
  private static final String MAX_AGE = "3600";

  @Bean
  public WebFilter corsFilter() {
    return (ServerWebExchange ctx, WebFilterChain chain) -> {
      ServerHttpRequest request = ctx.getRequest();
      if (CorsUtils.isCorsRequest(request)) {
        ServerHttpResponse response = ctx.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", String.join(",", ALLOWED_METHODS));
        headers.add("Access-Control-Allow-Headers", "*");
        headers.add("Access-Control-Max-Age", MAX_AGE);

        if (request.getMethod() == HttpMethod.OPTIONS) {
          response.setStatusCode(HttpStatus.OK);
          return Mono.empty();
        }
      }
      return chain.filter(ctx);
    };
  }

  @Bean
  ReactiveAuthorizationManager<AuthorizationContext> authorizationManager(
      TokenFactory tokenFactory) {
    return (Mono<Authentication> authenticationMono, AuthorizationContext context) -> {
      Mono<HttpHeaders> headers = Mono.just(context.getExchange().getRequest().getHeaders());
      Mono<String> authorizationHeader =
          headers.mapNotNull(httpHeaders -> httpHeaders.getFirst(HttpHeaders.AUTHORIZATION));
      Mono<DecodedJWT> decodedJWTMono = authorizationHeader.map(tokenFactory::verifyAccessToken);
      return decodedJWTMono
          .map(decodedJWT -> new AuthorizationDecision(true))
          .onErrorResume(
              exception -> {
                log.warn("was not able to validate jwt", exception);
                return Mono.just(new AuthorizationDecision(false));
              });
    };
  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      ReactiveAuthorizationManager<AuthorizationContext> authorizationManager) {
    http.httpBasic().disable();
    http.formLogin().disable();
    http.csrf().disable();
    http.logout().disable();
    http.cors().disable();
    http.exceptionHandling();
    http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());
    http.authorizeExchange(
        exchanges -> exchanges.pathMatchers(NOT_PROTECTED_RESOURCES).permitAll());
    http.authorizeExchange(exchanges -> exchanges.anyExchange().access(authorizationManager));

    return http.build();
  }
}
