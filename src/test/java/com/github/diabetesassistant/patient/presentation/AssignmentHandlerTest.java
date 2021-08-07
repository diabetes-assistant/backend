package com.github.diabetesassistant.patient.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.diabetesassistant.core.presentation.SecurityConfig;
import com.github.diabetesassistant.core.presentation.TokenFactory;
import com.github.diabetesassistant.patient.domain.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AssignmentHandler.class)
@Import(SecurityConfig.class)
public class AssignmentHandlerTest {
  @MockBean private AssignmentService serviceMock;
  @MockBean private TokenFactory tokenFactory;
  @Autowired private WebTestClient webTestClient;
  private DecodedJWT decodedJWTMock;

  @BeforeEach
  void setUp() {
    this.decodedJWTMock = mock(DecodedJWT.class);
    when(tokenFactory.verifyAccessToken(anyString())).thenReturn(decodedJWTMock);
  }

  @Test
  void shouldReturnSpecificAssignmentByCode() {
    String code = "foobar";
    Patient patient = new Patient(UUID.randomUUID(), "foo@bar.com");
    Doctor doctor = new Doctor(UUID.randomUUID(), "foo@bar.com");
    Assignment assignment = new Assignment(code, doctor, patient, "initial");
    when(this.serviceMock.findAssignment(code)).thenReturn(Mono.just(assignment));
    String loggedInUserId = UUID.randomUUID().toString();
    when(decodedJWTMock.getSubject()).thenReturn(loggedInUserId);

    PatientDTO patientDTO = new PatientDTO(patient.id().toString(), patient.email());
    DoctorDTO doctorDTO = new DoctorDTO(doctor.id().toString(), doctor.email());
    AssignmentDTO expected =
        new AssignmentDTO(assignment.code(), doctorDTO, patientDTO, assignment.state());

    this.webTestClient
        .get()
        .uri("/assignment/" + assignment.code())
        .header(HttpHeaders.AUTHORIZATION, "Bearer asdasd")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(AssignmentDTO.class)
        .value(response -> assertEquals(expected, response));
  }

  @Test
  void shouldReturn404ForNonExistingAssignment() {
    String code = "foobar";
    when(this.serviceMock.findAssignment(code)).thenReturn(Mono.empty());
    String loggedInUserId = UUID.randomUUID().toString();
    when(decodedJWTMock.getSubject()).thenReturn(loggedInUserId);

    this.webTestClient
        .get()
        .uri("/assignment/" + code)
        .header(HttpHeaders.AUTHORIZATION, "Bearer asdasd")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();
  }
}