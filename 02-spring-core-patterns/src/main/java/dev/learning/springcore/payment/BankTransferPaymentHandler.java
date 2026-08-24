package dev.learning.springcore.payment;

import org.springframework.stereotype.Component;

@Component
final class BankTransferPaymentHandler implements PaymentHandler {

    @Override
    public String method() {
        return "bank-transfer";
    }

    @Override
    public PaymentResult charge(PaymentCommand command) {
        return new PaymentResult("BANK-" + command.reference(), method());
    }
}

