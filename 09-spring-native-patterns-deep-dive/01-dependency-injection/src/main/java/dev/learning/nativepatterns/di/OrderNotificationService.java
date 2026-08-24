package dev.learning.nativepatterns.di;

import org.springframework.stereotype.Service;

@Service
public final class OrderNotificationService {
    private final NotificationChannelRegistry registry;

    public OrderNotificationService(NotificationChannelRegistry registry) {
        this.registry = registry;
    }

    public DeliveryReceipt notify(String channel, String recipient, String message) {
        return registry.required(channel).send(new NotificationRequest(recipient, message));
    }
}
