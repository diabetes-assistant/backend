package com.github.diabetesassistant.patient.data;

import com.github.diabetesassistant.DatabaseTest;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
public class AssignmentRepositoryTest extends DatabaseTest {
  @Autowired private AssignmentRepository repository;

  @Test
  void shouldFindAssignmentByDoctorId() throws SQLException {
    Statement statement = connection.createStatement();
    UUID doctorId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();
    String query =
        "INSERT INTO assignments (code, doctorId, patientId, state) VALUES ('foo', '"
            + doctorId
            + "', '"
            + patientId
            + "', 'confirmed')";
    statement.execute(query);
    statement.close();

    Flux<AssignmentEntity> actual = this.repository.findByDoctorId(doctorId);
    AssignmentEntity expected = new AssignmentEntity("foo", doctorId, patientId, "confirmed");

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }
}
