package com.appointymed.notification.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointymed.notification.domain.exception.InvalidNotificationEventException;
import com.appointymed.notification.domain.model.NotificationDelivery;
import com.appointymed.notification.domain.model.NotificationEvent;
import com.appointymed.notification.domain.repository.NotificationEventRepository;
import com.appointymed.notification.domain.repository.NotificationSenderPort;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProcessNotificationEventUseCaseTest {

    @Test
    void sendsEventOnlyOnceWhenMessageIsDuplicated() {
        InMemoryEventRepository repository = new InMemoryEventRepository();
        CapturingSender sender = new CapturingSender();
        ProcessNotificationEventUseCase useCase =
                new ProcessNotificationEventUseCase(repository, sender);
        NotificationEvent event = validEvent();

        useCase.execute(event);
        useCase.execute(event);

        assertEquals(1, sender.sent.size());
        assertEquals("patient@example.com", sender.sent.get(0).getRecipientEmail());
    }

    @Test
    void rejectsUnsupportedVersion() {
        ProcessNotificationEventUseCase useCase =
                new ProcessNotificationEventUseCase(new InMemoryEventRepository(), new CapturingSender());
        NotificationEvent event =
                new NotificationEvent(
                        "event-1",
                        "APPOINTMENT_BOOKED",
                        2,
                        "appointment-service",
                        "correlation-1",
                        "patient@example.com",
                        "Patient One",
                        "Appointment confirmed",
                        "Appointment booked");

        assertThrows(InvalidNotificationEventException.class, () -> useCase.execute(event));
    }

    private NotificationEvent validEvent() {
        return new NotificationEvent(
                "event-1",
                "APPOINTMENT_BOOKED",
                1,
                "appointment-service",
                "correlation-1",
                "patient@example.com",
                "Patient One",
                "Appointment confirmed",
                "Appointment booked");
    }

    private static final class InMemoryEventRepository implements NotificationEventRepository {
        private final Set<String> processed = new HashSet<>();

        @Override
        public boolean isProcessed(String producer, String eventId) {
            return processed.contains(producer + ":" + eventId);
        }

        @Override
        public void markProcessed(String producer, String eventId) {
            processed.add(producer + ":" + eventId);
        }
    }

    private static final class CapturingSender implements NotificationSenderPort {
        private final java.util.List<NotificationDelivery> sent = new java.util.ArrayList<>();

        @Override
        public void send(NotificationDelivery delivery) {
            sent.add(delivery);
        }
    }
}
