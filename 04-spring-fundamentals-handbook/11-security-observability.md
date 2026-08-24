# 11 - Security, Actuator và Observability

## Spring Security mental model

Servlet stack:

```text
HTTP -> DelegatingFilterProxy -> FilterChainProxy
     -> matching SecurityFilterChain
     -> authentication filters
     -> authorization filter
     -> DispatcherServlet/controller
```

Authentication trả lời “ai?”, authorization trả lời “được làm gì trên resource nào?”.

## Security configuration cơ bản

```java
@Configuration
@EnableMethodSecurity
class SecurityConfiguration {
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/catalog/**").hasAuthority("SCOPE_catalog.read")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

Với session/form application, CSRF thường phải bật. Với stateless bearer-token API, cấu hình CSRF phụ thuộc cách credential được gửi và browser exposure; không disable chỉ vì copy tutorial.

## Method security

```java
@PreAuthorize("hasAuthority('SCOPE_order.write') and #customerId == authentication.name")
public void cancel(String customerId, UUID orderId) { /* ... */ }
```

Method security bổ sung defense in depth. Complex object-level authorization nên đưa vào policy component, không viết SpEL dài khó test.

## Password

Không lưu plaintext hoặc reversible encryption. Dùng `PasswordEncoder` adaptive one-way hash (bcrypt/argon2/pbkdf2 theo policy), salt và migration strategy. Không tự viết crypto.

## CORS, CSRF và JWT

- CORS: browser có được gọi cross-origin không.
- CSRF: attacker lợi dụng browser tự gửi credential tới site khác.
- JWT: token format, không tự bảo đảm authorization/revocation/key rotation.

Resource server phải validate signature, issuer, audience, expiry/not-before và key rotation. Domain authorization vẫn kiểm tra tenant/resource ownership.

## Actuator

Dependency `spring-boot-starter-actuator` cung cấp endpoints như:

- `health`;
- `info`;
- `metrics`;
- `prometheus` khi registry phù hợp;
- `loggers`, `env`, `configprops`, `conditions`, `threaddump` tùy exposure.

Chỉ expose endpoints cần thiết và bảo vệ chúng. `env`, config, heap/thread dump có thể lộ thông tin nhạy cảm hoặc tăng attack surface.

## Health probes

- Liveness: process có cần restart không; không phụ thuộc mọi external dependency kẻo restart loop.
- Readiness: instance có nhận traffic được không.
- Startup: ứng dụng chậm khởi động có hoàn thành chưa.

Dependency down không phải lúc nào cũng làm liveness fail. Readiness policy phải phù hợp degraded mode.

## Observability

Ba signal:

- logs: discrete events/details;
- metrics: aggregate time series/alert/SLO;
- traces: distributed request path và latency.

Spring dùng Micrometer Observation để instrument operation. Low-cardinality tags như method/status/outcome dùng cho metrics; user/order ID là high-cardinality, không đưa vào metric labels.

## Metrics cơ bản

- RED cho service: Rate, Errors, Duration.
- USE cho resource: Utilization, Saturation, Errors.
- Business/async: order placed, payment rejected, queue lag, outbox backlog, cache hit ratio, DB connection pending.

Không tạo metric tag từ URL raw có ID, exception message, email hoặc tenant hàng triệu giá trị.

## Correlation

Trace ID/span ID giúp liên kết logs với trace. Qua HTTP dùng standard propagation; qua message cần copy trace context/correlation/causation có kiểm soát. Không tự dùng một `ThreadLocal` rồi giả định hoạt động qua `@Async`/virtual thread.

## Logging

- Structured logs với timestamp, level, service, environment, trace IDs, event/error code.
- Không log token/password/card/PII không cần thiết.
- Log exception một lần ở ownership boundary; tránh mỗi layer log cùng stack trace.
- Không dùng log thay metric cho high-volume health signal.

## SLO

SLI xuất phát user journey, ví dụ checkout success/latency. SLO là target; error budget giúp quyết định release/reliability work. CPU 80% là resource signal, không phải user-facing SLO.

## Test

- Endpoint public/private và object-level authorization.
- JWT invalid issuer/audience/expiry.
- CSRF/CORS behavior phù hợp client.
- Actuator exposure/security.
- Metrics tag cardinality và trace propagation.
- Sensitive data redaction.

Nguồn: [Spring Security Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html), [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/), [Spring Boot Observability](https://docs.spring.io/spring-boot/reference/actuator/observability.html).

