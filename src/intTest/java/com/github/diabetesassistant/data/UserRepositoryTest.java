package com.github.diabetesassistant.data;

import com.github.diabetesassistant.domain.Role;
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

    Mono<UserEntity> actual = this.userRepository.findByEmailAndPassword("foo@bar.com", "secret");
    UserEntity expected = new UserEntity(id, "foo@bar.com", "secret", Role.PATIENT.value);

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }
}
