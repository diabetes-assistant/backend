package com.github.diabetesassistant.patient.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.diabetesassistant.core.presentation.ErrorDTO;
import com.github.diabetesassistant.patient.domain.Patient;
import com.github.diabetesassistant.patient.domain.PatientService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PatientHandler.class)
class PatientHandlerTest {
  @MockBean private PatientService serviceMock;

  @Autowired private WebTestClient webTestClient;

  @Test
  void shouldReturnListOfPatients() {
    UUID patientId = UUID.randomUUID();
    Patient patient = new Patient(patientId, "foo@bar.com");
    when(this.serviceMock.findPatients(any())).thenReturn(Flux.just(patient));

    PatientDTO dto = new PatientDTO(patientId.toString(), "foo@bar.com");
    List<PatientDTO> expected = List.of(dto);
    String doctorId = UUID.randomUUID().toString();
    this.webTestClient
        .get()
        .uri("/patient?doctorId=" + doctorId)
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
        .header("Authorization", "Bearer ")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(ErrorDTO.class)
        .value(response -> assertEquals(ErrorDTO.class, response.getClass()));
  }

  @Test
  void shouldReturnEmptyListWhenNothingFound() {
    when(this.serviceMock.findPatients(any())).thenReturn(Flux.empty());

    List<PatientDTO> expected = Collections.emptyList();
    String doctorId = UUID.randomUUID().toString();
    this.webTestClient
        .get()
        .uri("/patient?doctorId=" + doctorId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(PatientDTO.class)
        .value(response -> assertEquals(expected, response));
  }
}
