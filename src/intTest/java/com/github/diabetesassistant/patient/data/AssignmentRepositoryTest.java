package com.github.diabetesassistant.patient.data;

import com.github.diabetesassistant.DatabaseTest;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        "INSERT INTO assignments (code, doctorId, patientId, state) VALUES ('foobar', '"
            + doctorId
            + "', '"
            + patientId
            + "', 'confirmed')";
    statement.execute(query);
    statement.close();

    Flux<AssignmentEntity> actual = this.repository.findByDoctorId(doctorId, "confirmed");
    AssignmentEntity expected = new AssignmentEntity("foobar", doctorId, patientId, "confirmed");

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldFindAssignmentByDoctorIdWithEmptyValues() throws SQLException {
    Statement statement = connection.createStatement();
    UUID doctorId = UUID.randomUUID();
    String query =
        "INSERT INTO assignments (code, doctorId, patientId, state) VALUES ('foo', '"
            + doctorId
            + "', NULL, 'confirmed')";
    statement.execute(query);
    statement.close();

    Flux<AssignmentEntity> actual = this.repository.findByDoctorId(doctorId, "confirmed");
    AssignmentEntity expected = new AssignmentEntity("foo", doctorId, null, "confirmed");

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }

  @Test
  void shouldFindCreatedAssignment() {
    UUID doctorId = UUID.randomUUID();
    AssignmentEntity assignmentEntity = new AssignmentEntity("foobar", doctorId, null, "initial");
    this.repository.save(assignmentEntity).block();

    Mono<AssignmentEntity> actual = this.repository.findById("foobar");
    AssignmentEntity expected = assignmentEntity;

    StepVerifier.create(actual.log()).expectNext(expected).verifyComplete();
  }
}
