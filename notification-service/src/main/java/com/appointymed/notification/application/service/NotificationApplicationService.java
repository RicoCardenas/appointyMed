package com.appointymed.notification.application.service;

import com.appointymed.notification.application.mapper.NotificationEventMapper;
import com.appointymed.notification.domain.usecase.ProcessNotificationEventUseCase;
import org.springframework.stereotype.Service;

@Service
public final class NotificationApplicationService {
    private final NotificationEventMapper eventMapper;
    private final ProcessNotificationEventUseCase processNotificationEvent;

    public NotificationApplicationService(
            NotificationEventMapper eventMapper,
            ProcessNotificationEventUseCase processNotificationEvent) {
        this.eventMapper = eventMapper;
        this.processNotificationEvent = processNotificationEvent;
    }

    public void process(String messageBody) {
        processNotificationEvent.execute(eventMapper.toDomain(messageBody));
    }
}
