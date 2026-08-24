package io.github.hanyuworm.lld.notification;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationServiceTest {
    @Test
    void retryDecoratorRetriesOnlyItsTypedTransientFailure() {
        var attempts = new AtomicInteger();
        NotificationChannel unstable = notification -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RetryingChannel.TransientDeliveryException("temporary");
            }
            return new NotificationChannel.DeliveryReceipt("provider-1");
        };
        var service = new NotificationService(Map.of(
                NotificationService.Channel.EMAIL, new RetryingChannel(unstable, 3)));
        var receipt = service.send(NotificationService.Channel.EMAIL,
                new Notification("N-1", "dev@example.com", "Hello"));
        assertEquals("provider-1", receipt.providerReference());
        assertEquals(3, attempts.get());
    }

    @Test
    void unsupportedChannelFailsFast() {
        var service = new NotificationService(Map.of());
        assertThrows(NotificationService.UnsupportedChannelException.class,
                () -> service.send(NotificationService.Channel.SMS,
                        new Notification("N-1", "+840000", "Hello")));
    }
}
