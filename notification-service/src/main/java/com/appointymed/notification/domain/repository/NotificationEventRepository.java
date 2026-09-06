package com.appointymed.notification.domain.repository;

public interface NotificationEventRepository {

    boolean isProcessed(String producer, String eventId);

    void markProcessed(String producer, String eventId);
}
