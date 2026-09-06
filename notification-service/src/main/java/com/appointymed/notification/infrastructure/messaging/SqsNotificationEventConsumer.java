package com.appointymed.notification.infrastructure.messaging;

import com.appointymed.notification.application.service.NotificationApplicationService;
import com.appointymed.notification.domain.exception.InvalidNotificationEventException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
public final class SqsNotificationEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(SqsNotificationEventConsumer.class);

    private final SqsClient sqsClient;
    private final NotificationApplicationService notificationService;
    private final String notificationQueueUrl;

    public SqsNotificationEventConsumer(
            SqsClient sqsClient,
            NotificationApplicationService notificationService,
            @Value("${app.aws.sqs.notification-queue-url:}") String notificationQueueUrl) {
        this.sqsClient = sqsClient;
        this.notificationService = notificationService;
        this.notificationQueueUrl = notificationQueueUrl;
    }

    @Scheduled(fixedDelayString = "${app.aws.sqs.poll-delay-ms:1000}")
    public void poll() {
        if (notificationQueueUrl == null || notificationQueueUrl.isBlank()) {
            return;
        }

        ReceiveMessageRequest request =
                ReceiveMessageRequest.builder()
                        .queueUrl(notificationQueueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(5)
                        .visibilityTimeout(30)
                        .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        for (Message message : messages) {
            process(message);
        }
    }

    private void process(Message message) {
        try {
            notificationService.process(message.body());
            delete(message);
        } catch (InvalidNotificationEventException exception) {
            log.error("Invalid notification event. Message will be retried or moved to DLQ", exception);
        } catch (RuntimeException exception) {
            log.warn("Notification processing failed. Message will be retried", exception);
        }
    }

    private void delete(Message message) {
        sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(notificationQueueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
    }
}
