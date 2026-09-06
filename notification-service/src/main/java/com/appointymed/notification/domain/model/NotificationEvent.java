package com.appointymed.notification.domain.model;

public final class NotificationEvent {
    private final String eventId;
    private final String eventType;
    private final int version;
    private final String producer;
    private final String correlationId;
    private final String recipientEmail;
    private final String recipientName;
    private final String subject;
    private final String message;

    public NotificationEvent(
            String eventId,
            String eventType,
            int version,
            String producer,
            String correlationId,
            String recipientEmail,
            String recipientName,
            String subject,
            String message) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.version = version;
        this.producer = producer;
        this.correlationId = correlationId;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.subject = subject;
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getVersion() {
        return version;
    }

    public String getProducer() {
        return producer;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }
}
