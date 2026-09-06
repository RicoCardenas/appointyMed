package com.appointymed.notification.domain.model;

public final class NotificationDelivery {
    private final String eventType;
    private final String recipientEmail;
    private final String recipientName;
    private final String subject;
    private final String message;

    public NotificationDelivery(
            String eventType,
            String recipientEmail,
            String recipientName,
            String subject,
            String message) {
        this.eventType = eventType;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.subject = subject;
        this.message = message;
    }

    public String getEventType() {
        return eventType;
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
