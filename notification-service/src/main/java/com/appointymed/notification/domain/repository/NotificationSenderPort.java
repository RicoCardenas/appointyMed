package com.appointymed.notification.domain.repository;

import com.appointymed.notification.domain.model.NotificationDelivery;

public interface NotificationSenderPort {

    void send(NotificationDelivery delivery);
}
