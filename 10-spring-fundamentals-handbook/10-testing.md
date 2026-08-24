# 10 - Testing trong Spring Boot

## Test pyramid thực dụng

| Test | Có Spring context? | Mục tiêu |
|---|---:|---|
| Unit | Không | Domain/service behavior nhanh, mọi branches |
| Slice | Một phần | MVC/JPA/JSON/client boundary |
| Integration | Có | Wiring, transaction, DB/broker/HTTP thật hoặc container |
| End-to-end | Toàn stack | Critical journey, ít và đắt |

Đừng dùng `@SpringBootTest` cho mọi test. Context startup chậm và failure khó định vị.

## Unit test

```java
class PricingServiceTest {
    @Test
    void appliesVipDiscount() {
        var service = new PricingService(new VipPricingStrategy());
        assertThat(service.quote(...)).isEqualTo(...);
    }
}
```

Không cần Spring cho pure business object. Constructor injection giúp test dễ.

## `@SpringBootTest`

Load full application context:

```java
@SpringBootTest
class OrderApplicationTest {}
```

Web environment:

- `MOCK`: mock servlet environment, mặc định cho servlet app;
- `RANDOM_PORT`: start real embedded server random port;
- `DEFINED_PORT`;
- `NONE`.

Dùng khi cần chứng minh wiring, transactions, events, full security filter chain hoặc integration qua nhiều layers.

## Slice tests

### `@WebMvcTest`

Load MVC slice:

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean OrderApplicationService service;
}
```

Test routing, JSON, validation, status, exception advice và security MVC behavior. `@MockitoBean` thay collaborator trong test context; không mock domain value object.

### `@DataJpaTest`

Load JPA/repository slice, thường dùng embedded DB và rollback test transaction:

```java
@DataJpaTest
class OrderRepositoryTest {
    @Autowired OrderRepository orders;
    @Autowired TestEntityManager entityManager;
}
```

Dùng `flush()` và `clear()` để SQL/lazy behavior xuất hiện. Với PostgreSQL-specific behavior, dùng Testcontainers thay H2.

### Các slices khác

- `@JsonTest`: JSON serialization/deserialization.
- `@RestClientTest`: REST client slice/mock server.
- `@JdbcTest`: JDBC components.

Slice annotation/exact module có thể thay đổi theo Boot generation; đọc test module docs của version đang dùng.

## MockMvc

```java
mvc.perform(post("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"customerId":""}"""))
    .andExpect(status().isBadRequest())
    .andExpect(jsonPath("$.title").value("Validation failed"));
```

Test contract, không assert implementation method call quá chi tiết.

## Real server test

`@SpringBootTest(webEnvironment = RANDOM_PORT)` dùng HTTP client gọi server thật, phù hợp filter/serialization/network stack. Chậm hơn MockMvc.

## Testcontainers

Chạy PostgreSQL/Redis/Kafka thật trong container:

```java
@Container
static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:17-alpine");

@DynamicPropertySource
static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
}
```

Pin version, reuse cẩn thận, chờ readiness và giữ test data isolation.

## Transaction test trap

Test method transactional có thể rollback sau test và giữ persistence context mở, làm code pass dù production controller đã ngoài transaction. Khi kiểm tra after-commit/listener/lazy-loading, dùng test boundary giống production và không bọc test trong transaction nếu không phù hợp.

## Context caching

Spring Test cache context giữa tests có cùng config. `@DirtiesContext` làm mất cache và chậm suite; chỉ dùng khi test thật sự làm bẩn singleton/application context state. Reset database/fake state có mục tiêu.

## Test names và AAA

```text
Given: state/input/dependency behavior
When: action duy nhất
Then: result + state + side effects
```

Test failure modes: timeout, duplicate, conflict, rollback, authorization, invalid input; không chỉ happy path/context-load.

## Nên test gì theo annotation

- `@Transactional`: DB rollback/commit qua proxied bean.
- `@Cacheable`: source call count/hit/miss/evict.
- `@Async`: thread/future/error/context.
- `@EventListener`: commit/rollback/listener exception.
- `@ConfigurationProperties`: bind + validation fail startup.
- JPA mapping: SQL constraint, relationship, fetch/query count.

Nguồn: [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html), [Spring Framework Testing](https://docs.spring.io/spring-framework/reference/testing.html).

