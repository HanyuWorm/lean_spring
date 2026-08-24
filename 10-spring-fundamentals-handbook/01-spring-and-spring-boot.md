# 01 - Spring Framework và Spring Boot

## Spring Framework là gì?

Spring Framework cung cấp các khối nền:

- IoC container và Dependency Injection;
- AOP/proxy;
- transaction abstraction;
- Spring MVC/WebFlux;
- JDBC, cache, events, validation integration và testing.

Spring không bắt buộc Spring Boot. Có thể tự tạo `ApplicationContext`, khai báo beans và cấu hình server/dependencies thủ công.

## Spring Boot là gì?

Spring Boot làm việc khởi tạo Spring application nhanh và nhất quán hơn:

- starters gom dependencies tương thích;
- auto-configuration tạo beans dựa trên classpath, properties và beans đã có;
- embedded server;
- externalized configuration;
- Actuator/production-ready features;
- test support và executable jar.

Boot không thay thế Spring Framework; Boot cấu hình và đóng gói một Spring application.

## `@SpringBootApplication`

```java
@SpringBootApplication
public class CommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommerceApplication.class, args);
    }
}
```

Annotation này kết hợp ba ý chính:

- `@Configuration`: class khai báo bean definitions;
- `@EnableAutoConfiguration`: cho phép Boot auto-configure;
- `@ComponentScan`: scan package của application class và subpackages.

Đặt application class ở root package hợp lý:

```text
com.example.commerce
├── CommerceApplication.java
├── order
├── payment
└── inventory
```

Nếu đặt class quá sâu, component ngoài subtree không được scan. Nếu đặt ở default package, scan scope có thể quá rộng.

## Auto-configuration hoạt động thế nào?

Auto-configuration dùng điều kiện như:

- class có trên classpath;
- property có giá trị cụ thể;
- chưa có bean do application khai báo;
- application type là servlet/reactive/non-web.

Ví dụ conceptual:

```java
@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnMissingBean(DataSource.class)
class DataSourceAutoConfiguration {
    // tạo DataSource mặc định nếu application chưa định nghĩa
}
```

Nguyên tắc “back off”: nếu application tạo bean phù hợp, auto-configuration thường nhường quyền. Dùng Actuator `conditions` endpoint hoặc chạy với `--debug` để hiểu vì sao config match/không match.

## Starter là gì?

Starter là dependency descriptor, ví dụ:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Starter Web kéo các dependency MVC/JSON/server phù hợp theo dependency management của Boot. Starter không phải annotation và không tự viết business code.

## Request lifecycle MVC cơ bản

```text
HTTP
 -> servlet filters / Spring Security
 -> DispatcherServlet
 -> HandlerMapping
 -> Controller
 -> Application Service
 -> Repository / HTTP adapter
 -> HttpMessageConverter serializes response
```

`DispatcherServlet` là front controller. Controller chỉ nên map protocol, validate input, gọi use case và map output; không chứa toàn bộ business/transaction flow.

## `ApplicationContext` và `BeanFactory`

- `BeanFactory`: container contract nền, tạo và quản lý beans.
- `ApplicationContext`: superset dùng phổ biến, thêm events, resources, environment, message resolution và integration.

Không gọi `context.getBean()` rải rác trong business code. Đó là Service Locator và che dependency; để container inject qua constructor.

## Câu hỏi tự kiểm tra

1. Spring Boot khác Spring Framework ở đâu?
2. Vì sao application class location ảnh hưởng component scan?
3. Auto-configuration “back off” nghĩa là gì?
4. Starter, auto-configuration và annotation khác nhau thế nào?

Nguồn: [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/), [Spring IoC Container](https://docs.spring.io/spring-framework/reference/core/beans.html).

