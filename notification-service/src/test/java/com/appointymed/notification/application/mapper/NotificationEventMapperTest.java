package com.appointymed.notification.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.appointymed.notification.domain.model.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class NotificationEventMapperTest {

    private final NotificationEventMapper mapper =
            new NotificationEventMapper(new ObjectMapper().findAndRegisterModules());

    @Test
    void mapsAppointmentEnvelopeToNotificationEvent() {
        String body =
                """
                {
                    "eventId": "event-1",
                    "eventType": "APPOINTMENT_BOOKED",
                    "version": 1,
                    "occurredAt": "2026-08-30T18:00:00Z",
                    "producer": "appointment-service",
                    "correlationId": "correlation-1",
                    "payload": {
                        "patientEmail": "patient@example.com",
                        "patientName": "Patient One",
                        "message": "Appointment booked"
                    }
                }
                """;

        NotificationEvent event = mapper.toDomain(body);

        assertEquals("event-1", event.getEventId());
        assertEquals("APPOINTMENT_BOOKED", event.getEventType());
        assertEquals("appointment-service", event.getProducer());
        assertEquals("patient@example.com", event.getRecipientEmail());
        assertEquals("Patient One", event.getRecipientName());
        assertEquals("Appointment booked", event.getMessage());
    }
}
