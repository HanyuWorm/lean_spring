# 03 - Configuration, Properties và Profiles

## Externalized configuration

Mục tiêu: cùng artifact chạy nhiều environment bằng config ngoài code.

Nguồn phổ biến:

- `application.properties`/`application.yaml` trong jar;
- profile-specific files;
- file ngoài jar;
- environment variables;
- JVM system properties;
- command-line arguments;
- test properties/dynamic properties.

Nguồn có precedence cao hơn override nguồn thấp hơn. Khi giá trị bất ngờ, kiểm tra Actuator `env`/`configprops` với security phù hợp thay vì đoán.

## Properties hay YAML?

```properties
payment.base-url=https://sandbox.example.com
payment.timeout=2s
payment.retry.max-attempts=3
```

```yaml
payment:
  base-url: https://sandbox.example.com
  timeout: 2s
  retry:
    max-attempts: 3
```

Chọn một format nhất quán. YAML dễ biểu diễn hierarchy nhưng indentation error khó thấy; properties dễ grep/override.

## `@Value`

```java
@Component
class Banner {
    Banner(@Value("${app.name:commerce}") String appName) {
        // default là commerce
    }
}
```

Hợp với một vài scalar values. Khi nhiều properties cùng prefix, `@Value` tạo stringly-typed config, khó validation/refactor.

## `@ConfigurationProperties`

```java
@ConfigurationProperties("payment")
@Validated
public record PaymentProperties(
        @NotBlank String baseUrl,
        @NotNull Duration timeout,
        Retry retry) {

    public record Retry(@Min(1) int maxAttempts) {}
}
```

Đăng ký:

```java
@SpringBootApplication
@ConfigurationPropertiesScan
class Application {}
```

Ưu điểm:

- typed binding cho `Duration`, `DataSize`, enum, list/map;
- group config theo capability;
- validation fail-fast lúc startup;
- metadata/IDE support khi dùng configuration processor;
- dễ unit test.

Properties object nên immutable và chỉ chứa config, không chứa business workflow.

## Environment variable mapping

Property:

```text
payment.retry.max-attempts
```

thường map thành:

```text
PAYMENT_RETRY_MAXATTEMPTS
```

Đây là relaxed binding. Với deployment platform, kiểm tra mapping thực tế và tránh tên quá phức tạp.

## Profiles

```yaml
# application.yaml
spring:
  profiles:
    active: dev
```

Không nên hard-code active production profile trong artifact. Kích hoạt từ environment/deployment:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
```

Profile-specific file: `application-prod.yaml` override config base. Nếu nhiều profiles active, thứ tự có ý nghĩa.

Profiles không phải feature flag system. Feature cần gradual rollout/audit/targeting nên dùng feature management phù hợp.

## Secrets

Không commit password/token/private key vào Git hoặc đưa secret vào image. Dùng secret manager/platform injection, rotate và giới hạn quyền. Tránh log toàn bộ `Environment`; Actuator endpoints nhạy cảm phải được bảo vệ và sanitize.

## Config import

```properties
spring.config.import=optional:file:./config/payment.properties
```

`optional:` cho phép location không tồn tại. Chỉ dùng khi absence thật sự hợp lệ; config bắt buộc nên fail startup.

## Lỗi thường gặp

- Dùng `@Value` cho hàng chục values không validation.
- Không hiểu precedence nên local/test/prod nhận giá trị khác dự kiến.
- Secret nằm trong YAML.
- `@Profile` rải khắp domain.
- Default nguy hiểm khiến production startup thành công với fake endpoint.
- Property timeout là số không đơn vị; nên dùng `2s`, `500ms` và `Duration`.

## Test config

```java
class PaymentPropertiesTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(PaymentConfig.class);

    @Test
    void rejectsMissingBaseUrl() {
        runner.run(context -> assertThat(context).hasFailed());
    }
}
```

Với Testcontainers, dùng `@DynamicPropertySource` hoặc service connection support thay vì hard-code random port.

Nguồn: [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html), [Type-safe Configuration Properties](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties).

