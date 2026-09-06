package com.appointymed.notification.infrastructure.config;

import com.appointymed.notification.domain.repository.NotificationEventRepository;
import com.appointymed.notification.domain.repository.NotificationSenderPort;
import com.appointymed.notification.domain.usecase.ProcessNotificationEventUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationUseCaseConfig {

    @Bean
    public ProcessNotificationEventUseCase processNotificationEventUseCase(
            NotificationEventRepository eventRepository, NotificationSenderPort notificationSender) {
        return new ProcessNotificationEventUseCase(eventRepository, notificationSender);
    }
}
