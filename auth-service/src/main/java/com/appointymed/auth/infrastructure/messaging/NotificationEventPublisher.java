package com.appointymed.auth.infrastructure.messaging;

import com.appointymed.auth.domain.usecase.ForgotPasswordUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class NotificationEventPublisher implements ForgotPasswordUseCase.NotificationEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String notificationQueueUrl;

    public NotificationEventPublisher(SqsClient sqsClient,
                                      ObjectMapper objectMapper,
                                      @Value("${app.aws.sqs.notification-queue-url}") String notificationQueueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.notificationQueueUrl = notificationQueueUrl;
    }

    @Override
    public void publishPasswordResetRequested(UUID userId, String email, String fullName, String resetToken, Instant expiry) {
        PasswordResetEvent event = new PasswordResetEvent(
                "PASSWORD_RESET_REQUESTED",
                userId,
                email,
                fullName,
                resetToken,
                expiry
        );
        sendMessage(event);
    }

    public void publishUserRegistered(UUID userId, String email, String fullName) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "USER_REGISTERED",
                userId,
                email,
                fullName
        );
        sendMessage(event);
    }

    public void publishAppointmentBooked(UUID appointmentId, UUID patientId, UUID doctorId, Instant scheduledAt) {
        AppointmentBookedEvent event = new AppointmentBookedEvent(
                "APPOINTMENT_BOOKED",
                appointmentId,
                patientId,
                doctorId,
                scheduledAt
        );
        sendMessage(event);
    }

    private void sendMessage(Object event) {
        if (notificationQueueUrl == null || notificationQueueUrl.isBlank()) {
            log.info("SQS notification queue URL is not configured. Skipping event: {}", event);
            return;
        }

        try {
            String messageBody = objectMapper.writeValueAsString(event);
            SendMessageRequest request = SendMessageRequest.builder()
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
            String eventType,
            UUID userId,
            String email,
            String fullName,
            String resetToken,
            Instant expiry
    ) {}

    public record UserRegisteredEvent(
            String eventType,
            UUID userId,
            String email,
            String fullName
    ) {}

    public record AppointmentBookedEvent(
            String eventType,
            UUID appointmentId,
            UUID patientId,
            UUID doctorId,
            Instant scheduledAt
    ) {}
}