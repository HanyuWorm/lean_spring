package dev.learning.resilience.payment;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
class PaymentController {

    private final PaymentPort payments;

    PaymentController(PaymentPort payments) {
        this.payments = payments;
    }

    @PostMapping
    PaymentReceipt charge(@RequestBody ChargePayment command) {
        return payments.charge(command);
    }
}

