package com.github.diabetesassistant;

import java.net.URI;
import java.net.URISyntaxException;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
  public static void main(String[] args) throws URISyntaxException {
    URI dbUri = new URI(System.getenv("DATABASE_URL"));
    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    String dbUrl =
        "jdbc:postgresql://"
            + dbUri.getHost()
            + ':'
            + dbUri.getPort()
            + dbUri.getPath()
            + "?sslmode=require";
    Flyway flyway = Flyway.configure().dataSource(dbUrl, username, password).load();
    flyway.migrate();
    SpringApplication.run(App.class, args);
  }
}
