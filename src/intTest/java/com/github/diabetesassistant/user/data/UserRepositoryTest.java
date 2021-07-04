package com.github.diabetesassistant.user.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.diabetesassistant.DatabaseTest;
import com.github.diabetesassistant.user.domain.Role;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class UserRepositoryTest extends DatabaseTest {

  @Autowired private UserRepository userRepository;

  @Test
  void shouldFindUser() throws SQLException {
    Statement statement = connection.createStatement();
    UUID id = UUID.randomUUID();
    statement.execute(
        "INSERT INTO users (id,email,password,role) VALUES ('"
            + id
            + "', 'foo@bar.com', 'secret', 'patient')");
    statement.close();

    Mono<UserEntity> actual = this.userRepository.findByEmail("foo@bar.com");
    UserEntity expected = new UserEntity(id, "foo@bar.com", "secret", Role.PATIENT.value);

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldCreateUser() throws SQLException {
    UserEntity userEntity = new UserEntity(null, "foo@bar.com", "secret", Role.PATIENT.value);

    UserEntity actual = this.userRepository.save(userEntity).block();
    UUID createdUserId = this.findUserId();
    UserEntity expected =
        new UserEntity(createdUserId, userEntity.email(), userEntity.password(), userEntity.role());

    assertEquals(expected, actual);
  }

  private UUID findUserId() throws SQLException {
    Statement statement = this.connection.createStatement();
    String sql = "SELECT id FROM users";
    ResultSet resultSet = statement.executeQuery(sql);
    if (resultSet.next()) {
      return UUID.fromString(resultSet.getString("id"));
    }
    return null;
  }
}
