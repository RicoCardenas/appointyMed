package com.appointymed.notification.infrastructure.messaging;

import com.appointymed.notification.domain.model.NotificationDelivery;
import com.appointymed.notification.domain.repository.NotificationSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class LoggingNotificationSender implements NotificationSenderPort {
    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(NotificationDelivery delivery) {
        log.info(
                "Notification simulated: type={}, recipient={}, subject={}",
                delivery.getEventType(),
                delivery.getRecipientEmail(),
                delivery.getSubject());
    }
}
