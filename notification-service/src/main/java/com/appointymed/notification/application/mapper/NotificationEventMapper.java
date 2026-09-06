package com.appointymed.notification.application.mapper;

import com.appointymed.notification.domain.exception.InvalidNotificationEventException;
import com.appointymed.notification.domain.model.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public final class NotificationEventMapper {
    private final ObjectMapper objectMapper;

    public NotificationEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NotificationEvent toDomain(String messageBody) {
        try {
            JsonNode envelope = objectMapper.readTree(messageBody);
            JsonNode payload = requiredNode(envelope, "payload");
            String eventType = requiredText(envelope, "eventType");
            requiredText(envelope, "occurredAt");
            if (!payload.isObject()) {
                throw new InvalidNotificationEventException("Event payload must be an object");
            }

            return new NotificationEvent(
                    requiredText(envelope, "eventId"),
                    eventType,
                    requiredVersion(envelope),
                    requiredText(envelope, "producer"),
                    requiredText(envelope, "correlationId"),
                    firstText(payload, "email", "patientEmail"),
                    firstText(payload, "fullName", "patientName"),
                    subjectFor(eventType),
                    messageFor(eventType, payload));
        } catch (InvalidNotificationEventException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new InvalidNotificationEventException(
                    "Invalid notification event payload: " + exception.getMessage());
        }
    }

    private int requiredVersion(JsonNode envelope) {
        JsonNode version = requiredNode(envelope, "version");
        if (!version.isInt() || version.asInt() <= 0) {
            throw new InvalidNotificationEventException("Event version must be a positive integer");
        }
        return version.asInt();
    }

    private String messageFor(String eventType, JsonNode payload) {
        String message = text(payload, "message");
        if (!isBlank(message)) {
            return message;
        }

        if ("PASSWORD_RESET_REQUESTED".equals(eventType)) {
            String resetToken = text(payload, "resetToken");
            String expiration = firstText(payload, "expiresAt", "expiry");
            return "Password reset token: "
                    + valueOrUnknown(resetToken)
                    + ". Expiration: "
                    + valueOrUnknown(expiration);
        }
        if ("USER_REGISTERED".equals(eventType)) {
            return "Welcome to Appointy Med.";
        }
        return "Appointment notification received.";
    }

    private String subjectFor(String eventType) {
        return switch (eventType) {
            case "PASSWORD_RESET_REQUESTED" -> "Password reset request";
            case "USER_REGISTERED" -> "Welcome to Appointy Med";
            case "APPOINTMENT_BOOKED" -> "Appointment confirmed";
            case "APPOINTMENT_CANCELLED" -> "Appointment cancelled";
            case "APPOINTMENT_RESCHEDULED" -> "Appointment rescheduled";
            default -> "Appointy Med notification";
        };
    }

    private String requiredText(JsonNode node, String field) {
        String value = text(node, field);
        if (isBlank(value)) {
            throw new InvalidNotificationEventException("Event field is required: " + field);
        }
        return value;
    }

    private JsonNode requiredNode(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            throw new InvalidNotificationEventException("Event field is required: " + field);
        }
        return node.get(field);
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String valueOrUnknown(String value) {
        return isBlank(value) ? "unknown" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
