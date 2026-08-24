package dev.learning.observability;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/lab")
class LabController {

    private final ObservedOrderService orders;
    private final ThreadExecutionService threads;

    LabController(ObservedOrderService orders, ThreadExecutionService threads) {
        this.orders = orders;
        this.threads = threads;
    }

    @GetMapping("/process")
    String process(@RequestParam String orderId) {
        return orders.process(orderId);
    }

    @GetMapping("/thread")
    CompletableFuture<ThreadExecutionService.ThreadInfo> thread() {
        return threads.inspectExecutorThread();
    }
}

