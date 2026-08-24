package dev.learning.nativepatterns.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class FactoryPatternTest {
    @Autowired
    private OrderFactory orders;

    @Autowired
    private ApplicationContext context;

    @Test
    void domainFactoryCreatesOnlyValidInitializedAggregate() {
        var order = orders.create("customer-1", new BigDecimal("42.00"));
        assertThat(order.id()).startsWith("ord-");
        assertThat(order.status()).isEqualTo("PENDING");

        assertThatThrownBy(() -> orders.create("customer-1", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void springFactoryBeanExposesProductAndFactorySeparately() {
        var product = context.getBean("shippingClient", ShippingClient.class);
        var factory = context.getBean("&shippingClient", ShippingClientFactoryBean.class);

        assertThat(product.endpoint()).isEqualTo("https://shipping.example.test");
        assertThat(factory.getObject()).isSameAs(product);
    }
}
