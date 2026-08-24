# 05 - Spring MVC, REST và Validation

## MVC request flow

```text
Filter -> DispatcherServlet -> HandlerMapping -> Controller
       -> Argument Resolvers -> Validation -> Service
       -> Return Value Handler -> HttpMessageConverter -> JSON
```

## Controller cơ bản

```java
@RestController
@RequestMapping("/api/orders")
class OrderController {
    private final OrderApplicationService service;

    OrderController(OrderApplicationService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable UUID id) {
        return service.get(id);
    }
}
```

- `@RestController`: return value đi vào response body.
- `@RequestMapping`: base mapping hoặc mapping tổng quát.
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`: method-specific shortcuts.
- `@PathVariable`: lấy segment URI.
- `@RequestParam`: query parameter.
- `@RequestHeader`: HTTP header.
- `@RequestBody`: deserialize body qua message converter.

## Create API

```java
record CreateOrderRequest(
        @NotBlank String customerId,
        @NotEmpty List<@Valid OrderItemRequest> items) {}

record OrderItemRequest(
        @NotBlank String sku,
        @Positive int quantity) {}
```

```java
@PostMapping
ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
    var created = service.create(request);
    var location = URI.create("/api/orders/" + created.id());
    return ResponseEntity.created(location).body(created);
}
```

`@Valid` kích hoạt Bean Validation cho nested object. Dùng `jakarta.validation` annotations trong Boot 3+.

## Validation layers

- Protocol validation: JSON shape, required field, format/range.
- Application validation: quyền thực thi use case, referenced resource tồn tại.
- Domain invariant: trạng thái hợp lệ bất kể entry point.
- Database constraint: lớp bảo vệ cuối cho uniqueness/referential/concurrency.

Không đặt mọi business rule vào DTO annotation.

## DTO và entity

Không trả JPA entity trực tiếp:

- lazy relation có thể query khi serialize;
- lộ schema/internal fields;
- recursion/bidirectional relation;
- client contract bị khóa vào persistence model;
- request có thể mass-assign field không nên sửa.

Dùng request/response DTO và mapper tại adapter boundary.

## Status code cơ bản

| Status | Khi dùng |
|---:|---|
| `200 OK` | Read/update thành công có body |
| `201 Created` | Resource được tạo; thường có `Location` |
| `202 Accepted` | Đã nhận nhưng chưa hoàn thành; cần operation/status resource |
| `204 No Content` | Thành công không body |
| `400 Bad Request` | Request syntax/binding chung không hợp lệ |
| `401 Unauthorized` | Chưa/không authentication hợp lệ |
| `403 Forbidden` | Đã xác thực nhưng không được phép |
| `404 Not Found` | Resource không tồn tại hoặc được che theo security policy |
| `409 Conflict` | Conflict với state hiện tại/duplicate business resource |
| `412 Precondition Failed` | `If-Match`/conditional request thất bại |
| `422 Unprocessable Content` | Content hiểu được nhưng semantic validation fail |
| `429 Too Many Requests` | Rate limit |
| `500`/`503` | Server bug/dependency unavailable theo error model |

## Exception handling với Problem Details

```java
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(OrderNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Order not found");
        problem.setProperty("errorCode", "ORDER_NOT_FOUND");
        return problem;
    }
}
```

Không trả stack trace/class exception. Client parse `errorCode`/`type`, không parse message con người. Validation errors cần field/path/code và không lộ sensitive value.

## Pagination

```java
@GetMapping
Page<OrderSummary> search(
        @RequestParam Optional<String> status,
        @PageableDefault(size = 20) Pageable pageable) {
    return service.search(status, pageable);
}
```

Luôn giới hạn page size và whitelist sort/filter fields. Offset pagination đơn giản nhưng đắt/không ổn định ở offset lớn; cursor/keyset tốt hơn cho feed lớn.

Không expose Spring `Page` như public contract dài hạn nếu không muốn contract phụ thuộc framework; map sang page response riêng.

## Filters, interceptors và advice

| Cơ chế | Boundary phù hợp |
|---|---|
| Servlet `Filter` | Raw HTTP, security chain, correlation trước MVC |
| `HandlerInterceptor` | Trước/sau controller, handler metadata |
| `@ControllerAdvice` | Exception/binding/controller cross-cutting |
| AOP | Method-level bean concern, không có raw HTTP semantics |

## CORS và content negotiation

CORS là browser cross-origin policy, không phải authentication. Cấu hình allowed origins/methods/headers cụ thể; tránh wildcard với credentials. Xác định JSON content type và versioning/deprecation policy.

## Lỗi thường gặp

- Controller chứa transaction/domain workflow lớn.
- `200 OK` cho mọi error.
- Entity vào/ra REST.
- Không giới hạn page size/filter complexity.
- Catch `Exception` và trả message nội bộ.
- Dùng request thread giữ chờ job dài thay vì `202 + operation resource`.

Nguồn: [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html), [Annotated Controllers](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html), [Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html).

