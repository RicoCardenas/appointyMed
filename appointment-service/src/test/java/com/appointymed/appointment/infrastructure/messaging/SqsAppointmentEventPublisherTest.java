package com.appointymed.appointment.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.AppointmentStatus;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainInstant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@ExtendWith(MockitoExtension.class)
class SqsAppointmentEventPublisherTest {

    @Mock private SqsClient sqsClient;

    @Test
    void publishesAppointmentUsingVersionOneEnvelope() throws Exception {
        SqsAppointmentEventPublisher publisher =
                new SqsAppointmentEventPublisher(
                        sqsClient,
                        new ObjectMapper().findAndRegisterModules(),
                        "http://localhost/notification-queue");
        Appointment appointment = appointment();

        publisher.publishAppointmentBooked(
                appointment, "patient@example.com", "Patient One", "Doctor One", "Cardiology");

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        JsonNode envelope =
                new ObjectMapper().findAndRegisterModules().readTree(captor.getValue().messageBody());

        assertThat(envelope.get("eventType").asText()).isEqualTo("APPOINTMENT_BOOKED");
        assertThat(envelope.get("version").asInt()).isOne();
        assertThat(envelope.get("producer").asText()).isEqualTo("appointment-service");
        assertThat(envelope.get("payload").get("patientEmail").asText())
                .isEqualTo("patient@example.com");
    }

    private Appointment appointment() {
        DomainDateTime start = DomainDateTime.ofEpochMillis(1_800_000_000_000L);
        return new Appointment(
                DomainId.of("11111111-1111-1111-1111-111111111111"),
                DomainId.of("22222222-2222-2222-2222-222222222222"),
                DomainId.of("33333333-3333-3333-3333-333333333333"),
                DomainId.of("44444444-4444-4444-4444-444444444444"),
                start,
                start.plusMinutes(30),
                AppointmentStatus.SCHEDULED,
                "Routine",
                null,
                null,
                DomainInstant.ofEpochMillis(1_799_000_000_000L),
                DomainInstant.ofEpochMillis(1_799_000_000_000L));
    }
}
