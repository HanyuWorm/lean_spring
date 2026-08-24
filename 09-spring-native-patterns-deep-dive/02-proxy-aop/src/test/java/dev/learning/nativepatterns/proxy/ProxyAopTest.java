package dev.learning.nativepatterns.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProxyAopTest {
    @Autowired
    private TransactionProbeService probe;

    @Autowired
    private LedgerService ledger;

    @AfterEach
    void cleanDatabase() {
        ledger.deleteAll();
    }

    @Test
    void externalInvocationCrossesProxyButSelfInvocationDoesNot() {
        assertThat(AopUtils.isAopProxy(probe)).isTrue();
        assertThat(probe.transactionalProbe()).isTrue();
        assertThat(probe.callTransactionalMethodThroughSelf()).isFalse();
    }

    @Test
    void transactionAdviceRollsBackDatabaseMutation() {
        assertThatThrownBy(() -> ledger.recordThenFail("payment-42"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ledger.countEntries()).isZero();
    }
}
