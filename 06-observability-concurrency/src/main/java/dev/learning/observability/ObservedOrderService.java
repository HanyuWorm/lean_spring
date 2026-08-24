package dev.learning.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Service;

@Service
class ObservedOrderService {

    private final ObservationRegistry observations;

    ObservedOrderService(ObservationRegistry observations) {
        this.observations = observations;
    }

    String process(String orderId) {
        return Observation.createNotStarted("order.process", observations)
                .lowCardinalityKeyValue("operation", "process")
                .highCardinalityKeyValue("order.id", orderId)
                .observe(() -> "processed:" + orderId);
    }
}

