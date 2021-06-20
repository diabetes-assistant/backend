package com.github.diabetesassistant.data;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

@SpringBootTest
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private Connection connection;

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

  @Test
  void shouldFindUser() throws SQLException {
    Statement statement = connection.createStatement();
    UUID id = UUID.randomUUID();
    statement.execute("INSERT INTO users (id,email,password,role) VALUES ('" + id + "', 'foo@bar.com', 'secret', 'patient')");
    statement.close();

    Mono<UserEntity> actual = this.userRepository.findByEmailAndPassword("foo@bar.com", "password");
    UserEntity expected = new UserEntity(id, "foo@bar.com", "secret", RoleEntity.PATIENT);

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }
}