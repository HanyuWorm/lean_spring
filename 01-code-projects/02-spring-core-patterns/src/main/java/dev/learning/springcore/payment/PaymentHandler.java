package dev.learning.springcore.payment;

public interface PaymentHandler {

    String method();

    PaymentResult charge(PaymentCommand command);
}

