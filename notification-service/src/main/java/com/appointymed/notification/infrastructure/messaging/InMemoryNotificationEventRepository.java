package com.appointymed.notification.infrastructure.messaging;

import com.appointymed.notification.domain.repository.NotificationEventRepository;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public final class InMemoryNotificationEventRepository implements NotificationEventRepository {
    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isProcessed(String producer, String eventId) {
        return processedEvents.contains(key(producer, eventId));
    }

    @Override
    public void markProcessed(String producer, String eventId) {
        processedEvents.add(key(producer, eventId));
    }

    private String key(String producer, String eventId) {
        return producer + ":" + eventId;
    }
}
