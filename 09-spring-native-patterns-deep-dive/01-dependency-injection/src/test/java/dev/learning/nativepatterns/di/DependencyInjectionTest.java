package dev.learning.nativepatterns.di;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DependencyInjectionTest {
    @Autowired
    private OrderNotificationService service;

    @Autowired
    private NotificationChannelRegistry registry;

    @Test
    void injectsEveryChannelAndSelectsByExplicitBusinessKey() {
        assertThat(registry.supportedChannels()).containsExactlyInAnyOrder("email", "sms");
        assertThat(service.notify("email", "senior@example.com", "Order confirmed"))
                .isEqualTo(new DeliveryReceipt("email", "senior@example.com", "SENT"));
    }

    @Test
    void failsFastInsteadOfSilentlyChoosingADefaultChannel() {
        assertThatThrownBy(() -> service.notify("push", "device-1", "Order confirmed"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("push");
    }
}
