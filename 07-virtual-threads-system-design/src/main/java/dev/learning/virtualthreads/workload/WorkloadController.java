package dev.learning.virtualthreads.workload;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class WorkloadController {

    private final WorkloadService workload;
    private final LoadGenerator loadGenerator;

    WorkloadController(WorkloadService workload, LoadGenerator loadGenerator) {
        this.workload = workload;
        this.loadGenerator = loadGenerator;
    }

    @PostMapping("/work-items")
    WorkResult create(@RequestBody CreateWorkCommand command) {
        return workload.process(command);
    }

    @GetMapping("/work-items/stats")
    WorkloadStats stats() {
        return workload.stats();
    }

    @PostMapping("/load")
    LoadSummary load(
            @RequestParam(defaultValue = "20") int requests,
            @RequestParam(defaultValue = "200") long processingMillis) {
        return loadGenerator.run(requests, processingMillis);
    }
}

