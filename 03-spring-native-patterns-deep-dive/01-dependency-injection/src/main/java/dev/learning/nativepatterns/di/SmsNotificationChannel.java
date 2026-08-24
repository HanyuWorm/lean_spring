package dev.learning.nativepatterns.di;

import org.springframework.stereotype.Component;

@Component
final class SmsNotificationChannel implements NotificationChannel {
    @Override
    public String key() {
        return "sms";
    }

    @Override
    public DeliveryReceipt send(NotificationRequest request) {
        return new DeliveryReceipt(key(), request.recipient(), "SENT");
    }
}
