package dev.learning.springcore.payment;

import dev.learning.springcore.audit.Audited;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<String, PaymentHandler> handlers;

    public PaymentService(List<PaymentHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toUnmodifiableMap(
                PaymentHandler::method,
                Function.identity()
        ));
    }

    @Audited
    public PaymentResult charge(PaymentCommand command) {
        var handler = handlers.get(command.method());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + command.method());
        }
        return handler.charge(command);
    }
}

