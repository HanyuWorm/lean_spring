package dev.learning.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class NativeRetryTest {

    @Autowired
    RetryProbe probe;

    @BeforeEach
    void reset() {
        probe.reset();
    }

    @Test
    void retriesTransientFailureThroughSpringProxy() {
        assertThat(probe.succeedOnThirdAttempt()).isEqualTo("ok");
        assertThat(probe.attempts()).isEqualTo(3);
    }
}

