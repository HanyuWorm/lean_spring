# 06 — Performance, Memory và Observability

## Đo đúng thứ

Performance là throughput + latency distribution + resource cost dưới workload cụ thể. Benchmark hello-world không quyết định framework cho domain service.

Golden signals:

- request rate, error rate, p50/p95/p99 latency;
- event-loop delay/utilization;
- CPU và throttling;
- heap used/limit, RSS/external/array buffers, GC;
- active handles/requests, sockets;
- DB pool active/pending, query latency;
- queue depth/consumer lag/cache metrics.

## Event-loop lag

Event-loop delay tăng khi callback CPU dài, sync I/O, huge JSON, GC pause hoặc microtask starvation. CPU thấp toàn pod không loại trừ một core chạy main isolate bị nghẽn.

Dùng `perf_hooks.monitorEventLoopDelay()`/eventLoopUtilization và correlate với endpoint/payload/deploy. Không tạo histogram quá chi tiết hoặc export high-cardinality label.

## Memory model

```text
process RSS
├── V8 heap: JS objects, closures, maps, promises
├── external/ArrayBuffer/Buffer backing memory
├── native addons/libraries
├── code/JIT/runtime structures
└── thread stacks và allocator fragmentation
```

Heap stable nhưng RSS tăng: xem Buffer/external/native/fragmentation. Heap leak: post-GC baseline, heap snapshots, dominator/retainer. Common owners: global map/cache, listeners, timers, AsyncLocalStorage/closure, unresolved Promise, queue.

## Profiling workflow

1. Reproduce with production-like traffic/data.
2. Confirm symptom: event loop, CPU, heap, RSS hay downstream.
3. CPU profile/flame graph cho hot stack; allocation profile cho churn; heap snapshot cho retention.
4. Thay một biến; load/soak lại.
5. Add regression limit/metric.

Inspector/profile endpoint là sensitive; không expose public. Heap snapshot có PII/secret và có thể pause/tốn disk.

## Logging

Structured JSON log với timestamp, level, service/version, trace/correlation ID và stable event code. Không log full request/token/password. Error nên giữ stack server-side nhưng response chỉ safe contract.

Async logger vẫn có buffer/backpressure/failure mode. Logging quá nhiều có thể block hoặc ăn memory; sampling/rate limit repeated error.

## Metrics cardinality

Không dùng user ID, order ID, raw URL hoặc error message làm metric label. Cardinality explosion phá memory/cost của app/collector/backend. Route template, status class và bounded error code phù hợp hơn.

## Tracing

Trace qua HTTP, DB, messaging và background job bằng W3C Trace Context/OpenTelemetry. AsyncLocalStorage giúp context propagation; message phải có trace/correlation/causation fields. Sampling phải giữ error/slow traces đủ để điều tra mà không vượt cost.

## Performance patterns

- parallelize I/O độc lập bằng bounded concurrency;
- stream large body/file;
- cache có policy và freshness;
- avoid repeated serialization/copy;
- response schema/compact DTO;
- move CPU-bound work sang worker/service;
- colocate app–DB khi latency quan trọng;
- keep-alive/pooling nhưng có max/lifetime/timeout.

## Load testing

Test warm-up, steady, spike, soak và failure:

- downstream slow/timeout;
- DB pool exhausted;
- client slow/cancel;
- large payload/worst cardinality;
- SIGTERM/rolling deploy;
- cache cold/stampede.

Coordinated omission và test client saturation có thể làm số liệu đẹp giả. Theo dõi cả client và server.

## Checklist

- SLO/traffic model và data shape rõ?
- Event-loop lag và downstream pool cùng dashboard?
- Metric label bounded?
- Trace/log redaction và sampling?
- Heap/RSS profile có soak test?
- Performance fix có benchmark trước/sau và correctness test?
