package com.appointymed.auth.infrastructure.messaging;

import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.usecase.ForgotPasswordUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class NotificationEventPublisher
        implements ForgotPasswordUseCase.NotificationEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String notificationQueueUrl;

    public NotificationEventPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${app.aws.sqs.notification-queue-url}") String notificationQueueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.notificationQueueUrl = notificationQueueUrl;
    }

    @Override
    public void publishPasswordResetRequested(
            DomainId userId, String email, String fullName, String resetToken, DomainInstant expiry) {
        PasswordResetEvent event =
                new PasswordResetEvent(
                        UUID.fromString(userId.value()),
                        email,
                        fullName,
                        resetToken,
                        Instant.ofEpochMilli(expiry.toEpochMillis()));
        sendMessage("PASSWORD_RESET_REQUESTED", event);
    }

    public void publishUserRegistered(UUID userId, String email, String fullName) {
        UserRegisteredEvent event = new UserRegisteredEvent(userId, email, fullName);
        sendMessage("USER_REGISTERED", event);
    }

    public void publishAppointmentBooked(
            UUID appointmentId, UUID patientId, UUID doctorId, Instant scheduledAt) {
        AppointmentBookedEvent event =
                new AppointmentBookedEvent(appointmentId, patientId, doctorId, scheduledAt);
        sendMessage("APPOINTMENT_BOOKED", event);
    }

    private void sendMessage(String eventType, Object payload) {
        if (notificationQueueUrl == null || notificationQueueUrl.isBlank()) {
            log.info("SQS notification queue URL is not configured. Skipping event: {}", eventType);
            return;
        }

        try {
            EventEnvelope envelope =
                    new EventEnvelope(
                            UUID.randomUUID(),
                            eventType,
                            1,
                            Instant.now(),
                            "auth-service",
                            UUID.randomUUID(),
                            null,
                            payload);
            String messageBody = objectMapper.writeValueAsString(envelope);
            SendMessageRequest request =
                    SendMessageRequest.builder()
                            .queueUrl(notificationQueueUrl)
                            .messageBody(messageBody)
                            .build();
            sqsClient.sendMessage(request);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification event", e);
        } catch (Exception e) {
            log.warn("Could not send SQS notification: {}", e.getMessage());
        }
    }

    public record PasswordResetEvent(
            UUID userId, String email, String fullName, String resetToken, Instant expiresAt) {}

    public record UserRegisteredEvent(UUID userId, String email, String fullName) {}

    public record AppointmentBookedEvent(
            UUID appointmentId, UUID patientId, UUID doctorId, Instant scheduledAt) {}

    public record EventEnvelope(
            UUID eventId,
            String eventType,
            int version,
            Instant occurredAt,
            String producer,
            UUID correlationId,
            UUID causationId,
            Object payload) {}
}
