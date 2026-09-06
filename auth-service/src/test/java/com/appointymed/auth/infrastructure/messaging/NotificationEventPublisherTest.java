package com.appointymed.auth.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.appointymed.auth.domain.model.DomainId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

    @Mock private SqsClient sqsClient;

    @Test
    void publishesPasswordResetUsingVersionOneEnvelope() throws Exception {
        NotificationEventPublisher publisher =
                new NotificationEventPublisher(
                        sqsClient,
                        new ObjectMapper().findAndRegisterModules(),
                        "http://localhost/notification-queue");

        publisher.publishPasswordResetRequested(
                DomainId.of("11111111-1111-1111-1111-111111111111"),
                "patient@example.com",
                "Patient One",
                "reset-token",
                com.appointymed.auth.domain.model.DomainInstant.ofEpochMillis(1_800_000_000_000L));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        JsonNode envelope =
                new ObjectMapper().findAndRegisterModules().readTree(captor.getValue().messageBody());

        assertThat(envelope.get("eventType").asText()).isEqualTo("PASSWORD_RESET_REQUESTED");
        assertThat(envelope.get("version").asInt()).isOne();
        assertThat(envelope.get("producer").asText()).isEqualTo("auth-service");
        assertThat(envelope.get("payload").get("email").asText()).isEqualTo("patient@example.com");
        assertThat(envelope.get("payload").get("resetToken").asText()).isEqualTo("reset-token");
    }
}
