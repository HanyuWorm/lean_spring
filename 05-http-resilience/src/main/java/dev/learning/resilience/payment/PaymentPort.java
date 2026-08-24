package dev.learning.resilience.payment;

public interface PaymentPort {

    PaymentReceipt charge(ChargePayment command);
}

