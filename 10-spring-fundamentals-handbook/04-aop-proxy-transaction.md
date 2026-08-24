# 04 - AOP, Proxy và `@Transactional`

## AOP giải quyết gì?

Aspect-Oriented Programming tách concern lặp lại quanh method invocation:

- transaction;
- authorization;
- caching;
- retry/concurrency limit;
- metrics/tracing/logging.

Các khái niệm:

- Aspect: module chứa cross-cutting concern.
- Advice: logic chạy before/after/around.
- Join point: điểm có thể intercept; trong Spring AOP chủ yếu là method execution trên bean.
- Pointcut: rule chọn join points.
- Proxy: object đứng trước target và chạy advice.

## Proxy mental model

```text
caller -> proxy -> advice(s) -> target method
```

Nếu tự tạo object bằng `new`, caller không có proxy. Nếu target gọi `this.otherMethod()`, call thường không quay lại proxy.

```java
@Service
class PaymentService {
    public void checkout() {
        savePayment(); // self-invocation
    }

    @Transactional
    public void savePayment() { /* ... */ }
}
```

`savePayment()` có thể không mở transaction khi được gọi qua `this`. Đặt transaction ở public use case `checkout()`, tách bean nếu đó là boundary thật, hoặc dùng `TransactionTemplate`.

## `@Transactional` cơ bản

```java
@Service
class PlaceOrderService {
    @Transactional
    public OrderId place(PlaceOrder command) {
        // load/update aggregates trong một local transaction
    }
}
```

Annotation có thể đặt ở class hoặc method; method config cụ thể override class config. Ưu tiên boundary application service, không đặt tràn lan repository/controller/entity.

### Rollback

Mặc định, runtime exception/error trigger rollback; checked exception không mặc định rollback.

```java
@Transactional(rollbackFor = IOException.class)
public void importFile() throws IOException { /* ... */ }
```

Không catch exception rồi trả success nếu muốn rollback. Nếu catch để map, throw exception phù hợp hoặc đánh dấu rollback có chủ ý.

### Propagation

| Propagation | Ý nghĩa cơ bản |
|---|---|
| `REQUIRED` | Join transaction hiện có hoặc tạo mới; mặc định |
| `REQUIRES_NEW` | Suspend transaction ngoài và tạo transaction mới |
| `SUPPORTS` | Join nếu có; nếu không chạy không transaction |
| `MANDATORY` | Bắt buộc transaction đã tồn tại |
| `NOT_SUPPORTED` | Chạy ngoài transaction, suspend nếu cần |
| `NEVER` | Fail nếu có transaction |
| `NESTED` | Savepoint nếu transaction manager/resource hỗ trợ |

`REQUIRES_NEW` có thể partial commit và cần connection khác; lạm dụng gây pool exhaustion. `NESTED` không đồng nghĩa transaction độc lập.

### Isolation

| Isolation | Ý nghĩa giản lược |
|---|---|
| `READ_UNCOMMITTED` | Có thể dirty read; ít dùng |
| `READ_COMMITTED` | Không dirty read; behavior phantom/non-repeatable tùy DB |
| `REPEATABLE_READ` | Đọc lại row ổn định hơn |
| `SERIALIZABLE` | Gần serial execution; contention/abort cao hơn |
| `DEFAULT` | Dùng database default |

Đừng chọn isolation bằng tên. Đọc semantics của database production và test concurrent transactions thật.

### `readOnly`

```java
@Transactional(readOnly = true)
public OrderView find(...) { /* ... */ }
```

Đây là hint/optimization tùy transaction manager/provider, không phải security guarantee rằng write chắc chắn bị cấm.

### Timeout

Transaction timeout giới hạn transaction duration theo support của manager. Nó không tự thay connect/read timeout của HTTP call. Tránh remote network call dài bên trong DB transaction.

## Advice order

Nếu method vừa cache, retry, transaction và metrics, thứ tự proxy/advice làm thay đổi:

- mỗi retry có transaction mới hay cùng transaction lỗi;
- cache hit có tạo transaction không;
- metrics đếm logical call hay mỗi attempt;
- circuit breaker thấy từng attempt hay final outcome.

Không “stack annotations” mà không có test về order/semantics.

## Custom AOP cơ bản

```java
@Aspect
@Component
class TimingAspect {
    @Around("@annotation(MeasuredUseCase)")
    Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        }
        finally {
            long duration = System.nanoTime() - start;
        }
    }
}
```

Trong production, dùng Micrometer Observation thay tự log timer thủ công. Custom AOP phù hợp concern kỹ thuật đồng nhất; không giấu business orchestration trong aspect.

## Test đúng

- Lấy bean từ context, assert nó là proxy nếu behavior phụ thuộc proxy.
- Gọi method từ bên ngoài bean.
- Dùng H2/PostgreSQL và assert commit/rollback thật.
- Tạo self-invocation test để hiểu giới hạn.
- Test advice order/attempt count khi kết hợp retry/transaction.

Demo: [`../09-spring-native-patterns-deep-dive/02-proxy-aop`](../09-spring-native-patterns-deep-dive/02-proxy-aop/README.md) và [`05-template-callback`](../09-spring-native-patterns-deep-dive/05-template-callback/README.md).

Nguồn: [Spring AOP](https://docs.spring.io/spring-framework/reference/core/aop.html), [Declarative Transactions](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative.html).

