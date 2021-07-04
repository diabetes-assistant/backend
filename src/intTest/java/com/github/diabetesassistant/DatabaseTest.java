package com.github.diabetesassistant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class DatabaseTest {

  protected Connection connection;

  @BeforeEach
  void setUp() throws SQLException {
    String user = "postgres";
    String password = "mysecretpassword";
    String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
    Flyway flyway = Flyway.configure().dataSource(jdbcUrl, user, password).load();
    flyway.clean();
    flyway.migrate();
    Properties connectionProps = new Properties();
    connectionProps.put("user", user);
    connectionProps.put("password", password);
    this.connection = DriverManager.getConnection(jdbcUrl, connectionProps);
  }

  @AfterEach
  public void cleanUpEach() throws SQLException {
    this.connection.close();
  }
}
