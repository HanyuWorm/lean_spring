package io.github.hanyuworm.lld.notification;

import java.util.Objects;

public final class RetryingChannel implements NotificationChannel {
    private final NotificationChannel delegate;
    private final int maxAttempts;

    public RetryingChannel(NotificationChannel delegate, int maxAttempts) {
        this.delegate = Objects.requireNonNull(delegate);
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts");
        this.maxAttempts = maxAttempts;
    }

    @Override
    public DeliveryReceipt send(Notification notification) {
        TransientDeliveryException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return delegate.send(notification);
            } catch (TransientDeliveryException error) {
                last = error;
            }
        }
        throw last;
    }

    public static final class TransientDeliveryException extends RuntimeException {
        public TransientDeliveryException(String message) { super(message); }
    }
}
