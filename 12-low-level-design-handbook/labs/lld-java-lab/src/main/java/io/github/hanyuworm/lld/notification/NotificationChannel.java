package io.github.hanyuworm.lld.notification;

@FunctionalInterface
public interface NotificationChannel {
    DeliveryReceipt send(Notification notification);

    record DeliveryReceipt(String providerReference) {}
}
