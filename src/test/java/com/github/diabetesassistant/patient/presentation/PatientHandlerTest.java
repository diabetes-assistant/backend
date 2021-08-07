package com.github.diabetesassistant.patient.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.diabetesassistant.core.presentation.ErrorDTO;
import com.github.diabetesassistant.core.presentation.SecurityConfig;
import com.github.diabetesassistant.core.presentation.TokenFactory;
import com.github.diabetesassistant.patient.domain.Patient;
import com.github.diabetesassistant.patient.domain.PatientService;
import java.util.Collections;
import java.util.List;
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
import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PatientHandler.class)
@Import(SecurityConfig.class)
class PatientHandlerTest {
  @MockBean private PatientService serviceMock;
  @MockBean private TokenFactory tokenFactory;
  @Autowired private WebTestClient webTestClient;
  private DecodedJWT decodedJWTMock;

  @BeforeEach
  void setUp() {
    this.decodedJWTMock = mock(DecodedJWT.class);
    when(tokenFactory.verifyAccessToken(anyString())).thenReturn(decodedJWTMock);
  }

  @Test
  void shouldReturnListOfPatients() {
    UUID patientId = UUID.randomUUID();
    Patient patient = new Patient(patientId, "foo@bar.com");
    when(this.serviceMock.findPatients(any())).thenReturn(Flux.just(patient));
    String loggedInUserId = UUID.randomUUID().toString();
    when(decodedJWTMock.getSubject()).thenReturn(loggedInUserId);

    PatientDTO dto = new PatientDTO(patientId.toString(), "foo@bar.com");
    List<PatientDTO> expected = List.of(dto);
    String doctorId = UUID.randomUUID().toString();
    this.webTestClient
        .get()
        .uri("/patient?doctorId=" + doctorId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer asdasd")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(PatientDTO.class)
        .value(response -> assertEquals(expected, response));
  }

  @Test
  void shouldReturn400ForInvalidDoctorId() {
    String doctorId = "foo";
    this.webTestClient
        .get()
        .uri("/patient?doctorId=" + doctorId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer asdasd")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ErrorDTO.class)
        .value(response -> assertEquals(ErrorDTO.class, response.getClass()));
  }

  @Test
  void shouldReturn400ForNonExistingDoctorId() {
    String doctorId = "foo";
    when(serviceMock.findPatients(any()))
        .thenReturn(Flux.error(new IllegalArgumentException("Not found")));
    this.webTestClient
        .get()
        .uri("/patient?doctorId=" + doctorId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer asdasd")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ErrorDTO.class)
        .value(response -> assertEquals(ErrorDTO.class, response.getClass()));
  }

  @Test
  void shouldReturn401ForInvalidToken() {
    when(this.serviceMock.findPatients(any())).thenReturn(Flux.empty());

    List<PatientDTO> expected = Collections.emptyList();
    String doctorId = UUID.randomUUID().toString();
    this.webTestClient
        .get()
        .uri("/patient?doctorId=" + doctorId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer asdasd")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(PatientDTO.class)
        .value(response -> assertEquals(expected, response));
  }

  @Test
  void shouldReturnEmptyListWhenNothingFound() {
    when(this.serviceMock.findPatients(any())).thenReturn(Flux.empty());

    List<PatientDTO> expected = Collections.emptyList();
    String doctorId = UUID.randomUUID().toString();
    this.webTestClient
        .get()
        .uri("/patient?doctorId=" + doctorId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer asdasd")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(PatientDTO.class)
        .value(response -> assertEquals(expected, response));
  }
}
