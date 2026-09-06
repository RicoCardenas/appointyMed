package com.appointymed.notification.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.appointymed.notification.application.mapper.NotificationEventMapper;
import com.appointymed.notification.application.service.NotificationApplicationService;
import com.appointymed.notification.domain.model.NotificationDelivery;
import com.appointymed.notification.domain.repository.NotificationEventRepository;
import com.appointymed.notification.domain.repository.NotificationSenderPort;
import com.appointymed.notification.domain.usecase.ProcessNotificationEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Testcontainers
class SqsNotificationEventConsumerIntegrationTest {

    @Container
    static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.4"))
                    .withServices(LocalStackContainer.Service.SQS);

    private SqsClient sqsClient;
    private String queueUrl;

    @BeforeEach
    void setUpQueue() {
        sqsClient =
                SqsClient.builder()
                        .endpointOverride(
                                LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.SQS))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create("test", "test")))
                        .build();

        String queueName = "notification-integration-" + UUID.randomUUID();
        queueUrl =
                sqsClient
                        .createQueue(CreateQueueRequest.builder().queueName(queueName).build())
                        .queueUrl();
    }

    @AfterEach
    void closeClient() {
        sqsClient.close();
    }

    @Test
    void consumesEnvelopeFromSqsDeletesMessageAndProcessesDuplicateOnce() {
        CapturingSender sender = new CapturingSender();
        SqsNotificationEventConsumer consumer = createConsumer(sender);
        String eventBody = validEventBody();

        sqsClient.sendMessage(
                SendMessageRequest.builder().queueUrl(queueUrl).messageBody(eventBody).build());
        sqsClient.sendMessage(
                SendMessageRequest.builder().queueUrl(queueUrl).messageBody(eventBody).build());

        consumer.poll();

        assertThat(sender.deliveries).hasSize(1);
        assertThat(sender.deliveries.get(0).getRecipientEmail()).isEqualTo("patient@example.com");
        assertThat(sender.deliveries.get(0).getSubject()).isEqualTo("Appointment confirmed");
        assertThat(visibleMessageCount()).isZero();
    }

    private SqsNotificationEventConsumer createConsumer(CapturingSender sender) {
        NotificationEventMapper mapper =
                new NotificationEventMapper(new ObjectMapper().findAndRegisterModules());
        NotificationApplicationService applicationService =
                new NotificationApplicationService(
                        mapper,
                        new ProcessNotificationEventUseCase(
                                new InMemoryEventRepository(), sender));
        return new SqsNotificationEventConsumer(sqsClient, applicationService, queueUrl);
    }

    private int visibleMessageCount() {
        String count =
                sqsClient
                        .getQueueAttributes(
                                GetQueueAttributesRequest.builder()
                                        .queueUrl(queueUrl)
                                        .attributeNames(
                                                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                                        .build())
                        .attributes()
                        .get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES);
        return Integer.parseInt(count);
    }

    private String validEventBody() {
        return """
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
    }

    private static final class InMemoryEventRepository implements NotificationEventRepository {
        private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

        @Override
        public boolean isProcessed(String producer, String eventId) {
            return processedEvents.contains(producer + ":" + eventId);
        }

        @Override
        public void markProcessed(String producer, String eventId) {
            processedEvents.add(producer + ":" + eventId);
        }
    }

    private static final class CapturingSender implements NotificationSenderPort {
        private final ArrayList<NotificationDelivery> deliveries = new ArrayList<>();

        @Override
        public void send(NotificationDelivery delivery) {
            deliveries.add(delivery);
        }
    }
}
