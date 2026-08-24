package dev.learning.springcore.payment;

import org.springframework.stereotype.Component;

@Component
final class CardPaymentHandler implements PaymentHandler {

    @Override
    public String method() {
        return "card";
    }

    @Override
    public PaymentResult charge(PaymentCommand command) {
        return new PaymentResult("CARD-" + command.reference(), method());
    }
}

