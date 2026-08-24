package dev.learning.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class VirtualThreadTest {

    @Autowired
    ThreadExecutionService threads;

    @Autowired
    ObservedOrderService orders;

    @Test
    void bootExecutorUsesVirtualThreadsWhenEnabled() {
        var info = threads.inspectExecutorThread().join();

        assertThat(info.virtual()).isTrue();
    }

    @Test
    void observedUseCaseKeepsBusinessResult() {
        assertThat(orders.process("O-42")).isEqualTo("processed:O-42");
    }
}

