package com.appointymed.appointment.infrastructure.messaging;

import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.repository.AppointmentEventPublisherPort;
import com.appointymed.appointment.infrastructure.persistence.mapper.AppointmentDomainValueMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SqsAppointmentEventPublisher implements AppointmentEventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(SqsAppointmentEventPublisher.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String notificationQueueUrl;

    public SqsAppointmentEventPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${app.aws.sqs.notification-queue-url:}") String notificationQueueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.notificationQueueUrl = notificationQueueUrl;
    }

    @Override
    public void publishAppointmentBooked(
            Appointment appointment,
            String patientEmail,
            String patientName,
            String doctorName,
            String specialtyName) {
        var event =
                new AppointmentNotificationEvent(
                        AppointmentDomainValueMapper.toUuid(appointment.getId()),
                        AppointmentDomainValueMapper.toUuid(appointment.getPatientId()),
                        patientEmail,
                        patientName,
                        AppointmentDomainValueMapper.toUuid(appointment.getDoctorId()),
                        doctorName,
                        specialtyName,
                        AppointmentDomainValueMapper.toLocalDateTime(appointment.getAppointmentDateTime()),
                        AppointmentDomainValueMapper.toLocalDateTime(appointment.getEndDateTime()),
                        "Your appointment with "
                                + doctorName
                                + " ("
                                + specialtyName
                                + ") has been successfully scheduled for "
                                + appointment.getAppointmentDateTime()
                                + ".",
                        Instant.now());
        sendEvent("APPOINTMENT_BOOKED", event);
    }

    @Override
    public void publishAppointmentCancelled(
            Appointment appointment,
            String patientEmail,
            String patientName,
            String doctorName,
            String reason) {
        var event =
                new AppointmentNotificationEvent(
                        AppointmentDomainValueMapper.toUuid(appointment.getId()),
                        AppointmentDomainValueMapper.toUuid(appointment.getPatientId()),
                        patientEmail,
                        patientName,
                        AppointmentDomainValueMapper.toUuid(appointment.getDoctorId()),
                        doctorName,
                        null,
                        AppointmentDomainValueMapper.toLocalDateTime(appointment.getAppointmentDateTime()),
                        AppointmentDomainValueMapper.toLocalDateTime(appointment.getEndDateTime()),
                        "Your appointment on "
                                + appointment.getAppointmentDateTime()
                                + " has been cancelled. Reason: "
                                + reason,
                        Instant.now());
        sendEvent("APPOINTMENT_CANCELLED", event);
    }

    @Override
    public void publishAppointmentRescheduled(
            Appointment appointment,
            String patientEmail,
            String patientName,
            String doctorName,
            String specialtyName) {
        var event =
                new AppointmentNotificationEvent(
                        AppointmentDomainValueMapper.toUuid(appointment.getId()),
                        AppointmentDomainValueMapper.toUuid(appointment.getPatientId()),
                        patientEmail,
                        patientName,
                        AppointmentDomainValueMapper.toUuid(appointment.getDoctorId()),
                        doctorName,
                        specialtyName,
                        AppointmentDomainValueMapper.toLocalDateTime(appointment.getAppointmentDateTime()),
                        AppointmentDomainValueMapper.toLocalDateTime(appointment.getEndDateTime()),
                        "Your appointment with "
                                + doctorName
                                + " has been rescheduled to "
                                + appointment.getAppointmentDateTime()
                                + ".",
                        Instant.now());
        sendEvent("APPOINTMENT_RESCHEDULED", event);
    }

    private void sendEvent(String eventType, Object payload) {
        if (notificationQueueUrl == null || notificationQueueUrl.isBlank()) {
            log.info(
                    "SQS notification queue URL is not configured. Simulating event publish: {}", eventType);
            return;
        }
        try {
            EventEnvelope envelope =
                    new EventEnvelope(
                            UUID.randomUUID(),
                            eventType,
                            1,
                            Instant.now(),
                            "appointment-service",
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
            log.info("Published appointment notification event to SQS queue");
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize appointment notification event", e);
        } catch (Exception e) {
            log.warn("Could not send SQS message: {}", e.getMessage());
        }
    }

    public record AppointmentNotificationEvent(
            UUID appointmentId,
            UUID patientId,
            String patientEmail,
            String patientName,
            UUID doctorId,
            String doctorName,
            String specialtyName,
            LocalDateTime appointmentDateTime,
            LocalDateTime endDateTime,
            String message,
            Instant timestamp) {}

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
