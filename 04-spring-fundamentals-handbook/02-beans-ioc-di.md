# 02 - Beans, IoC và Dependency Injection

## Bean là gì?

Spring bean là object được Spring IoC container tạo/đăng ký/quản lý theo một `BeanDefinition`. Container biết:

- type và bean name;
- cách khởi tạo;
- dependencies;
- scope;
- lifecycle callbacks;
- qualifiers/primary/lazy và proxy processors.

Object tạo bằng `new` trong application code vẫn là Java object, không tự trở thành Spring bean. Annotation như `@Transactional`, `@Async`, `@Cacheable` thường không hoạt động trên object ngoài container vì không có proxy/post-processing.

## Hai cách phổ biến để tạo bean

### Component scanning

```java
@Service
public class OrderService {
    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }
}
```

Các stereotype:

- `@Component`: component tổng quát;
- `@Service`: application/domain service role;
- `@Repository`: persistence adapter; hỗ trợ exception translation;
- `@Controller`: MVC controller trả view;
- `@RestController`: `@Controller` + response body mặc định.

Stereotype giúp thể hiện vai trò và cho phép tool/AOP target theo layer. Chúng đều dẫn đến bean registration khi được scan.

### Java configuration với `@Bean`

```java
@Configuration
class PaymentConfiguration {
    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    PaymentClient paymentClient(Clock clock, PaymentProperties properties) {
        return new PaymentClient(properties.baseUrl(), clock);
    }
}
```

`@Bean` đặt trên factory method; return value được đăng ký thành bean. Dùng khi:

- class thuộc thư viện, không sửa để thêm `@Component`;
- construction cần explicit config;
- muốn compose adapter/decorator;
- cần condition/profile hoặc bean name rõ.

Ưu tiên dependency qua tham số method như trên. Trong full `@Configuration`, inter-bean method call có thể được container intercept để giữ semantics; trong “lite mode” (`@Bean` nằm ở class không phải full `@Configuration`), direct method call là Java call bình thường và có thể tạo object mới. Tránh phụ thuộc sự khác biệt này bằng method-parameter injection.

## IoC là gì?

Không phải application code tự dựng graph:

```java
var repository = new JpaOrderRepository(...);
var service = new OrderService(repository);
```

Container đọc bean definitions rồi dựng graph. Quyền điều khiển construction/lifecycle được đảo sang container; business object chỉ khai báo dependency cần thiết.

## Constructor injection

```java
@Service
class CheckoutService {
    private final PaymentPort payments;
    private final Clock clock;

    CheckoutService(PaymentPort payments, Clock clock) {
        this.payments = payments;
        this.clock = clock;
    }
}
```

Nếu class chỉ có một constructor, không cần `@Autowired`. Constructor injection giúp dependency bắt buộc, field `final`, test dễ và circular dependency lộ rõ.

Field injection:

```java
@Autowired
private PaymentPort payments;
```

chạy được nhưng che dependency, khó tạo object thuần và field không final; không khuyến nghị cho application code.

## Nhiều beans cùng type

Giả sử:

```java
interface MessageSender { void send(String message); }

@Component("emailSender")
class EmailSender implements MessageSender { /* ... */ }

@Component("smsSender")
class SmsSender implements MessageSender { /* ... */ }
```

### `@Primary` là gì?

Đánh dấu candidate ưu tiên cho injection point đơn trị:

```java
@Primary
@Component
class EmailSender implements MessageSender { /* ... */ }
```

```java
NotificationService(MessageSender sender) {
    // EmailSender được chọn nếu không có qualifier khác
}
```

Lưu ý:

- `@Primary` không xóa các beans khác.
- Inject `List<MessageSender>` vẫn nhận tất cả senders.
- Nhiều `@Primary` cùng type vẫn ambiguous.
- Dùng primary khi có default kỹ thuật/composition hợp lý; không dùng để che lựa chọn business theo request.

### `@Qualifier` là gì?

Narrow candidates tại injection point:

```java
NotificationService(@Qualifier("smsSender") MessageSender sender) {
    this.sender = sender;
}
```

Có thể tạo custom qualifier semantic:

```java
@Target({FIELD, PARAMETER, TYPE, METHOD})
@Retention(RUNTIME)
@Qualifier
public @interface TransactionalMessageSender {}
```

```java
@TransactionalMessageSender
@Component
class OutboxMessageSender implements MessageSender { /* ... */ }
```

Custom qualifier tốt hơn string khi role là contract ổn định. `@Qualifier` không nhất thiết là bean ID; hãy coi nó là tag semantic để lọc candidates.

### `@Fallback`

Spring hiện đại có `@Fallback` để đánh dấu candidate ưu tiên thấp hơn beans thường. Nếu có bean không-fallback phù hợp, fallback bị loại khỏi lựa chọn. Hữu ích cho default implementation của library/starter; application thường vẫn dùng explicit composition.

### Collection injection

```java
RuleEngine(List<OrderRule> rules) {
    this.rules = List.copyOf(rules);
}
```

Spring inject mọi bean cùng type; `@Order`/`Ordered` có thể sắp xếp. Nếu order là business contract, test exact order hoặc compose list explicit trong `@Configuration`.

## Bean name

- Component mặc định lấy class name viết thường chữ đầu, ví dụ `emailSender`.
- `@Component("customName")` đặt tên rõ.
- `@Bean` mặc định lấy method name.
- Có thể alias/nhiều names.

Không dùng bean name như business strategy key một cách vô thức; refactor tên bean có thể làm đổi behavior.

## Scope

| Scope | Ý nghĩa |
|---|---|
| `singleton` | Một bean instance cho mỗi `ApplicationContext`; mặc định |
| `prototype` | Container tạo instance mới mỗi lần được yêu cầu |
| `request` | Một instance mỗi HTTP request |
| `session` | Một instance mỗi HTTP session |
| `application` | Một instance theo `ServletContext` |

Singleton không tự động thread-safe. Tránh mutable request state trong singleton service.

Inject prototype trực tiếp vào singleton chỉ resolve một lần lúc singleton được tạo. Nếu cần instance mới mỗi use, dùng `ObjectProvider<T>`, scoped proxy hoặc thiết kế lại ownership.

## Lifecycle

```java
@Component
class ClientHolder {
    @PostConstruct
    void initialize() { /* validate/start */ }

    @PreDestroy
    void shutdown() { /* close */ }
}
```

Constructor không nên thực hiện network call nặng. `@PostConstruct` chạy sau dependency injection nhưng có thể làm startup fail; đó đôi khi là fail-fast mong muốn. `@PreDestroy` không được gọi cho mọi prototype instance vì container không quản full lifecycle sau khi giao object.

## `@Lazy`, `ObjectProvider` và optional dependency

- `@Lazy`: trì hoãn bean creation hoặc inject lazy proxy; có thể đẩy lỗi từ startup sang runtime.
- `ObjectProvider<T>`: lookup lazy/optional/multiple có kiểm soát tại infrastructure/composition boundary.
- `Optional<T>` injection: thể hiện dependency optional nhưng có thể che cấu hình thiếu.

Không dùng lazy để “sửa” circular dependency. Circular dependency thường báo module/use-case boundary sai.

## `@Profile` và conditional beans

```java
@Bean
@Profile("dev")
PaymentClient fakePaymentClient() { /* ... */ }
```

```java
@Bean
@ConditionalOnProperty(name = "payment.enabled", havingValue = "true")
PaymentClient paymentClient() { /* ... */ }
```

Profiles phù hợp nhóm environment config lớn; feature/capability cụ thể thường rõ hơn bằng property condition. Không rải `@Profile("prod")` vào business classes.

## Lỗi thường gặp

- Tự `new` bean rồi thắc mắc `@Transactional` không chạy.
- Có hai implementation nhưng không dùng qualifier/primary/composition rõ.
- Dùng field injection ở mọi nơi.
- Singleton giữ mutable user/request data.
- `@Bean` method gọi trực tiếp trong lite mode tạo object ngoài lifecycle mong đợi.
- Dùng `ApplicationContext#getBean()` làm business selection.

## Demo trong workspace

Xem [`../03-spring-native-patterns-deep-dive/01-dependency-injection`](../03-spring-native-patterns-deep-dive/01-dependency-injection/README.md) và [`04-factory`](../03-spring-native-patterns-deep-dive/04-factory/README.md).

Nguồn: [Spring IoC Container](https://docs.spring.io/spring-framework/reference/core/beans.html), [Annotation-based configuration](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config.html), [`@Bean`](https://docs.spring.io/spring-framework/reference/core/beans/java/bean-annotation.html).

