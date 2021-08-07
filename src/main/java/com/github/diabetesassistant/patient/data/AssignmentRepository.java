package com.github.diabetesassistant.patient.data;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AssignmentRepository extends ReactiveCrudRepository<AssignmentEntity, String> {
  @Query("SELECT * FROM assignments where doctorId = :doctorId and state = :state")
  Flux<AssignmentEntity> findByDoctorId(UUID doctorId, String state);
}
