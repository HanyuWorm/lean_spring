package dev.learning.resilience;

import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RetryProbe {

    private final AtomicInteger attempts = new AtomicInteger();

    @Retryable(includes = IllegalStateException.class, maxRetries = 2, delay = 1)
    public String succeedOnThirdAttempt() {
        if (attempts.incrementAndGet() < 3) {
            throw new IllegalStateException("temporary failure");
        }
        return "ok";
    }

    int attempts() {
        return attempts.get();
    }

    void reset() {
        attempts.set(0);
    }
}
