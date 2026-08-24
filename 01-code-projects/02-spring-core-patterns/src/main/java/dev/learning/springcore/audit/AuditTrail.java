package dev.learning.springcore.audit;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AuditTrail {

    private final CopyOnWriteArrayList<String> entries = new CopyOnWriteArrayList<>();

    public void record(String operation) {
        entries.add(operation);
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }

    public void clear() {
        entries.clear();
    }
}

