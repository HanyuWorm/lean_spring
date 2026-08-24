# 09 - Events, Async và Scheduling

## Spring Application Events

```java
public record OrderPlaced(UUID orderId) {}

@Service
class OrderService {
    private final ApplicationEventPublisher events;

    @Transactional
    public void place(...) {
        // persist
        events.publishEvent(new OrderPlaced(id));
    }
}
```

```java
@Component
class NotificationListener {
    @EventListener
    void on(OrderPlaced event) { /* ... */ }
}
```

Mặc định listener chạy synchronous trong publisher thread. Listener exception có thể propagate và làm publisher/transaction fail. Event in-process không durable: process crash thì event mất.

## Transactional event listener

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
void on(OrderPlaced event) { /* ... */ }
```

Phases:

- `BEFORE_COMMIT`;
- `AFTER_COMMIT`;
- `AFTER_ROLLBACK`;
- `AFTER_COMPLETION`.

After-commit listener không biến external side effect thành durable. Process vẫn có thể crash sau commit trước side effect. Dùng Outbox/persistent event publication cho integration quan trọng.

Demo: [`../03-spring-native-patterns-deep-dive/07-observer-domain-events`](../03-spring-native-patterns-deep-dive/07-observer-domain-events/README.md).

## Domain event và integration event

- Domain event là fact trong bounded context.
- Application event là transport in-process.
- Integration event là versioned contract ra ngoài context/process.
- Map giữa chúng ở application/infrastructure boundary; không publish JPA entity.

## `@Async`

```java
@EnableAsync
@Configuration
class AsyncConfiguration {}

@Async("notificationExecutor")
public CompletableFuture<Void> sendNotification(...) {
    // chạy trên executor khác
    return CompletableFuture.completedFuture(null);
}
```

`@Async` thường dùng proxy:

- phải gọi bean từ bên ngoài; self-invocation không đổi thread;
- private method không phải boundary tốt;
- `void` exception cần async exception handler; `Future`/`CompletableFuture` truyền failure qua result;
- transaction/thread local/MDC/security/observation context không được giả định tự truyền;
- task trong memory mất khi process crash.

Nếu job phải durable/retry sau restart, dùng queue/job table/broker/scheduler durable.

## Executor

```java
@Bean
ThreadPoolTaskExecutor notificationExecutor() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("notification-");
    return executor;
}
```

Bound queue và rejection policy; unbounded queue che overload và tăng latency/memory. Với Virtual Threads, vẫn giới hạn concurrency đến downstream bằng semaphore/pool/`@ConcurrencyLimit`.

## Scheduling

```java
@EnableScheduling
@Configuration
class SchedulingConfiguration {}

@Scheduled(fixedDelayString = "${jobs.outbox.delay:1s}")
void publishOutbox() { /* ... */ }
```

- `fixedRate`: tính theo start cadence, có thể overlap tùy scheduler/config.
- `fixedDelay`: chờ sau lần chạy trước hoàn thành.
- `cron`: lịch theo expression/time zone.

Trong nhiều application replicas, mỗi replica có thể chạy cùng scheduled method. Cần partition/leader/DB lock/idempotency hoặc dùng external scheduler. Clock/timezone/DST phải explicit.

## Context propagation

ThreadLocal context không tự nhiên đi qua executor. Micrometer Context Propagation/TaskDecorator có thể capture/restore selected context; luôn clear để tránh leak. Không truyền toàn bộ request object/security token vô thời hạn.

## Test

- Event listener thường và after-commit trong commit/rollback.
- Async method chạy thread khác và future mang exception.
- Executor saturation/rejection.
- Scheduled job idempotency khi trigger hai lần/concurrent replicas.
- Context/correlation propagation.

Nguồn: [Spring Application Events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events), [Task Execution and Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html).

