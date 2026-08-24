package dev.learning.nativepatterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdapterDecoratorTest {
    @Autowired
    private InventoryService inventory;

    @Autowired
    private InventoryMetrics metrics;

    @BeforeEach
    void resetMetrics() {
        metrics.reset();
    }

    @Test
    void adapterMapsExternalModelAndDecoratorsAddBehavior() {
        assertThat(inventory.check("SKU-1")).isEqualTo(new Stock("SKU-1", 10));
        assertThat(inventory.check("SKU-1")).isEqualTo(new Stock("SKU-1", 10));

        assertThat(metrics.logicalCalls()).isEqualTo(2);
        assertThat(metrics.warehouseLoads()).isOne();
    }

    @Test
    void adapterTranslatesVendorExceptionAtTheBoundary() {
        assertThatThrownBy(() -> inventory.check("MISSING"))
                .isInstanceOf(UnknownSkuException.class)
                .hasMessageContaining("MISSING");
    }
}
