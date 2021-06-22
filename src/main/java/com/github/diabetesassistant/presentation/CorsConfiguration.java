package com.github.diabetesassistant.presentation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@ConditionalOnProperty(name = "cors", havingValue = "true")
@Configuration
@EnableWebFlux
public class CorsConfiguration implements WebFluxConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {
    corsRegistry
        .addMapping("/**")
        .allowedOrigins(
            "https://staging-diabetes-assitant-fe.herokuapp.com",
            "https://live-diabetes-assitant-fe.herokuapp.com",
            "localhost")
        .allowedMethods("PUT", "POST", "GET", "OPTION", "DELETE")
        .maxAge(3600);
  }
}
