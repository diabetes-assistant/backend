package com.github.diabetesassistant.data;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.client.SSLMode;
import io.r2dbc.spi.ConnectionFactory;
import java.net.URI;
import java.util.Optional;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
public class DatabaseConfiguration extends AbstractR2dbcConfiguration {
  @SneakyThrows
  @Override
  @Bean
  @ConditionalOnProperty(name = "db.load_from_environment", havingValue = "true")
  public ConnectionFactory connectionFactory() {
    IllegalStateException missingUrlException =
        new IllegalStateException("DATABASE_URL environment variable not set");
    String databaseUrl =
        Optional.ofNullable(System.getenv("DATABASE_URL")).orElseThrow(() -> missingUrlException);
    URI dbUri = new URI(databaseUrl);
    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    return new PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
            .host(dbUri.getHost())
            .port(dbUri.getPort())
            .database(dbUri.getPath().substring(1))
            .username(username)
            .password(password)
            .sslMode(SSLMode.REQUIRE)
            .build());
  }
}
