package dev.learning.nativepatterns.di;

public interface NotificationChannel {
    String key();

    DeliveryReceipt send(NotificationRequest request);
}
