package com.github.diabetesassistant;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
  public static void main(String[] args) throws URISyntaxException {
    IllegalStateException missingUrlException =
        new IllegalStateException("DATABASE_URL environment variable not set");
    String databaseUrl =
        Optional.ofNullable(System.getenv("DATABASE_URL")).orElseThrow(() -> missingUrlException);
    URI dbUri = new URI(databaseUrl);
    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
    //            + "?sslmode=require";
    Flyway flyway = Flyway.configure().dataSource(dbUrl, username, password).load();
    flyway.migrate();
    SpringApplication.run(App.class, args);
  }
}
