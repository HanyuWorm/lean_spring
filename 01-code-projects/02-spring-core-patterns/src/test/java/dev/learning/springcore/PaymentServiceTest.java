package dev.learning.springcore;

import dev.learning.springcore.audit.AuditTrail;
import dev.learning.springcore.payment.PaymentCommand;
import dev.learning.springcore.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    AuditTrail auditTrail;

    @BeforeEach
    void resetAudit() {
        auditTrail.clear();
    }

    @Test
    void selectsInjectedStrategyAndAuditsThroughProxy() {
        var result = paymentService.charge(
                new PaymentCommand("card", "ORDER-1", new BigDecimal("50.00")));

        assertThat(result.transactionId()).isEqualTo("CARD-ORDER-1");
        assertThat(auditTrail.entries()).hasSize(1);
        assertThat(AopUtils.isAopProxy(paymentService)).isTrue();
    }

    @Test
    void rejectsUnknownStrategy() {
        assertThatThrownBy(() -> paymentService.charge(
                new PaymentCommand("crypto", "ORDER-2", BigDecimal.TEN)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("crypto");
    }
}

