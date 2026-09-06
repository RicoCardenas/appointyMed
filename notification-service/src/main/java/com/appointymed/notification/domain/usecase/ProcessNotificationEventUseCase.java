package com.appointymed.notification.domain.usecase;

import com.appointymed.notification.domain.exception.InvalidNotificationEventException;
import com.appointymed.notification.domain.model.NotificationDelivery;
import com.appointymed.notification.domain.model.NotificationEvent;
import com.appointymed.notification.domain.repository.NotificationEventRepository;
import com.appointymed.notification.domain.repository.NotificationSenderPort;

public final class ProcessNotificationEventUseCase {
    private static final int SUPPORTED_VERSION = 1;

    private final NotificationEventRepository eventRepository;
    private final NotificationSenderPort notificationSender;

    public ProcessNotificationEventUseCase(
            NotificationEventRepository eventRepository, NotificationSenderPort notificationSender) {
        this.eventRepository = eventRepository;
        this.notificationSender = notificationSender;
    }

    public void execute(NotificationEvent event) {
        validate(event);

        if (eventRepository.isProcessed(event.getProducer(), event.getEventId())) {
            return;
        }

        notificationSender.send(
                new NotificationDelivery(
                        event.getEventType(),
                        event.getRecipientEmail(),
                        event.getRecipientName(),
                        event.getSubject(),
                        event.getMessage()));
        eventRepository.markProcessed(event.getProducer(), event.getEventId());
    }

    private void validate(NotificationEvent event) {
        if (event == null) {
            throw new InvalidNotificationEventException("Notification event is required");
        }
        if (isBlank(event.getEventId()) || isBlank(event.getProducer())) {
            throw new InvalidNotificationEventException("Notification event identity is required");
        }
        if (event.getVersion() != SUPPORTED_VERSION) {
            throw new InvalidNotificationEventException(
                    "Unsupported notification event version: " + event.getVersion());
        }
        if (!isSupportedType(event.getEventType())) {
            throw new InvalidNotificationEventException(
                    "Unsupported notification event type: " + event.getEventType());
        }
        if (isBlank(event.getRecipientEmail())) {
            throw new InvalidNotificationEventException("Notification recipient email is required");
        }
        if (isBlank(event.getSubject()) || isBlank(event.getMessage())) {
            throw new InvalidNotificationEventException("Notification subject and message are required");
        }
    }

    private boolean isSupportedType(String eventType) {
        return "PASSWORD_RESET_REQUESTED".equals(eventType)
                || "USER_REGISTERED".equals(eventType)
                || "APPOINTMENT_BOOKED".equals(eventType)
                || "APPOINTMENT_CANCELLED".equals(eventType)
                || "APPOINTMENT_RESCHEDULED".equals(eventType);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
