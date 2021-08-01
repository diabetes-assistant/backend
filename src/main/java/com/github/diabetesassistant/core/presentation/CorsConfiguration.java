package com.github.diabetesassistant.core.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@ConditionalOnProperty(name = "cors", havingValue = "true")
@Configuration
@EnableWebFlux
@Slf4j
public class CorsConfiguration implements WebFluxConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {
    log.info("Configuring CORS");
    corsRegistry
        .addMapping("/**")
        .allowedOrigins(
            "https://staging-diabetes-assitant-fe.herokuapp.com",
            "https://live-diabetes-assitant-fe.herokuapp.com",
            "localhost")
        .allowedMethods("PUT", "POST", "GET", "OPTION", "DELETE")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
