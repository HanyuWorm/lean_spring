# Official Sources và Version Baseline

Nội dung handbook ưu tiên tài liệu chính thức. Version snapshot tại thời điểm biên soạn: 2026-08-24. Concept cốt lõi áp dụng rộng cho Spring Boot 3.x/4.x và Java 21+, nhưng option/metric/API cụ thể cần kiểm tra lại theo JDK, Spring, Hibernate và runtime đang triển khai.

## Java/JVM

- [Java 25 Troubleshooting Guide — Diagnostic Tools](https://docs.oracle.com/en/java/javase/25/troubleshoot/diagnostic-tools.html): `jcmd`, JFR, heap dump, class histogram và Native Memory Tracking.
- [Java 25 Troubleshooting with JFR](https://docs.oracle.com/en/java/javase/25/troubleshoot/troubleshoot-performance-issues-using-jfr.html): heap statistics, allocation site và recording workflow.
- [Java 25 GC Tuning Guide](https://docs.oracle.com/en/java/javase/25/gctuning/index.html): collector, heap sizing và GC fundamentals.
- [Available Collectors](https://docs.oracle.com/en/java/javase/25/gctuning/available-collectors.html): trade-off giữa Serial, Parallel, G1 và ZGC.
- [Factors Affecting GC Performance](https://docs.oracle.com/en/java/javase/25/gctuning/factors-affecting-garbage-collection-performance.html): heap/generation sizing và throughput/pause trade-off.
- [Java Virtual Threads](https://docs.oracle.com/en/java/javase/25/core/virtual-threads.html): lifecycle, scheduling và quan sát Virtual Threads.
- [Thread-Local Variables](https://docs.oracle.com/en/java/javase/25/core/thread-local-variables.html): semantics và lifetime của per-thread values.

## Spring

- [Spring Framework Bean Scopes](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html): singleton, prototype và web scopes; prototype destruction lifecycle.
- [Spring Declarative Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-decl-explained.html): thread-bound imperative transaction và Reactor-context reactive transaction.
- [Spring WebFlux Reference](https://docs.spring.io/spring-framework/reference/web/webflux.html): reactive HTTP stack, codecs, streaming và data buffers.
- [Spring Boot Actuator Endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html): metrics, heap dump, thread dump và endpoint exposure/security.
- [Spring Boot Heap Dump Endpoint](https://docs.spring.io/spring-boot/api/rest/actuator/heapdump.html): HPROF response và cách gọi endpoint.

## Hibernate

- [Hibernate ORM 7.1 User Guide](https://docs.hibernate.org/orm/7.1/userguide/html_single/): persistence context, flushing, batching, caching, fetching và dirty checking.

## Cách dùng nguồn

- Ưu tiên reference đúng major/minor đang chạy, không copy flag/config từ blog cũ.
- Test option JVM bằng `java -XX:+PrintFlagsFinal -version` hoặc `jcmd <pid> VM.flags` trên chính runtime.
- Xác nhận tên Micrometer metric qua `/actuator/metrics`/registry vì binder và naming convention có thể đổi.
- Không expose diagnostic endpoint chỉ vì tài liệu có ví dụ gọi được; production phải qua security và operational policy.
