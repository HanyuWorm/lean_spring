package dev.learning.nativepatterns.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
class TemplateCallbackTest {
    @Autowired
    private OrderRegistrationTemplate template;

    @AfterEach
    void cleanDatabase() {
        template.deleteAll();
    }

    @Test
    void templateOwnsTransactionWhileCallbackProvidesVariableBehavior() {
        var callbackSawTransaction = new AtomicBoolean();

        template.register("order-1", ignored -> callbackSawTransaction.set(
                TransactionSynchronizationManager.isActualTransactionActive()));

        assertThat(callbackSawTransaction).isTrue();
        assertThat(template.countOrders()).isOne();
    }

    @Test
    void callbackFailureRollsBackWorkOwnedByTheTemplate() {
        assertThatThrownBy(() -> template.register("order-2", ignored -> {
            throw new IllegalStateException("callback failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(template.countOrders()).isZero();
    }
}
