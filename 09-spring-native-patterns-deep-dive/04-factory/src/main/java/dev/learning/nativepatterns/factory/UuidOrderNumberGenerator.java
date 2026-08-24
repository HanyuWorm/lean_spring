package dev.learning.nativepatterns.factory;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
final class UuidOrderNumberGenerator implements OrderNumberGenerator {
    @Override
    public String next() {
        return "ord-" + UUID.randomUUID();
    }
}
