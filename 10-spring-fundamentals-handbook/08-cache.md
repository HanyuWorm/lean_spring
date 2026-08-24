# 08 - Spring Cache và Hibernate Cache

## Cache dùng để làm gì?

Cache đổi memory/storage/consistency complexity lấy latency và giảm tải source. Trước khi thêm cache phải biết:

- source of truth;
- key và namespace;
- freshness tolerance/TTL;
- invalidation khi write;
- behavior khi cache miss/down;
- stampede/hot key;
- data security/tenant isolation;
- metric hit/miss/eviction/load latency.

## Spring Cache abstraction

Dependency + enable:

```java
@SpringBootApplication
@EnableCaching
class Application {}
```

Spring Cache là abstraction; provider thực tế có thể là ConcurrentMap, Caffeine, Redis, Hazelcast... `CacheManager` quản caches.

## `@Cacheable`

```java
@Cacheable(cacheNames = "products", key = "#sku")
public ProductView getProduct(String sku) {
    return repository.load(sku); // chỉ chạy khi miss
}
```

Flow:

```text
proxy computes key -> cache lookup
  hit  -> return cached value, target method không chạy
  miss -> call target -> cache result -> return
```

Method phải được gọi qua Spring proxy. Self-invocation có cùng giới hạn như `@Transactional`.

## `@CachePut`

```java
@CachePut(cacheNames = "products", key = "#result.sku")
public ProductView update(UpdateProduct command) {
    return repository.update(command); // luôn chạy rồi update cache
}
```

`@CachePut` không skip target. Không đặt `@Cacheable` và `@CachePut` trên cùng method nếu không hiểu rõ condition/order.

## `@CacheEvict`

```java
@CacheEvict(cacheNames = "products", key = "#sku")
public void delete(String sku) {
    repository.delete(sku);
}
```

```java
@CacheEvict(cacheNames = "products", allEntries = true)
public void rebuildCatalog() {}
```

`allEntries` có thể tạo cache miss storm. `beforeInvocation=true` evict trước target; mặc định evict sau success. Chọn theo failure semantics.

## `@Caching`

Gom nhiều cache operations:

```java
@Caching(evict = {
    @CacheEvict(cacheNames = "productBySku", key = "#result.sku"),
    @CacheEvict(cacheNames = "catalogPages", allEntries = true)
})
public ProductView update(...) { /* ... */ }
```

Nhiều derived caches làm invalidation graph phức tạp; cân nhắc versioned keys/read model.

## Key generation

Mặc định Spring tạo key từ parameters. Nên explicit khi:

- cùng cache name dùng nhiều method/signature;
- multi-tenant;
- object parameter có equality không ổn định;
- cần version/schema namespace.

```java
@Cacheable(cacheNames = "tenant-products", key = "#tenantId + ':' + #sku")
```

Không để tenant A đọc key tenant B. Tránh chứa PII/secret trong key/log/Redis key nếu không cần.

## `condition`, `unless`, `sync`

```java
@Cacheable(
    cacheNames = "products",
    key = "#sku",
    condition = "#sku != null",
    unless = "#result == null",
    sync = true)
```

- `condition`: quyết định trước call.
- `unless`: quyết định không cache result sau call.
- `sync=true`: provider-dependent synchronization cho concurrent miss cùng key; không mặc định giải quyết distributed stampede.

SpEL mạnh nhưng expression dài làm logic ẩn; dùng custom `KeyGenerator`/service khi policy phức tạp.

## Provider lựa chọn

| Provider | Phạm vi | Điểm chính |
|---|---|---|
| ConcurrentMap | Một JVM, demo/test | Không TTL/size policy production-ready mặc định |
| Caffeine | Một JVM | Nhanh, TTL/size/metrics; mỗi instance có cache riêng |
| Redis | Distributed/shared | Network/serialization/TTL/cluster; cache có thể unavailable |

Local cache nhanh nhưng invalidation giữa replicas khó. Distributed cache nhất quán hơn giữa instances nhưng thêm network/failure/cost. Có thể dùng near-cache hai tầng nhưng complexity cao.

## Cache Aside

Spring `@Cacheable` thể hiện biến thể cache-aside:

```text
read cache -> miss -> read source -> populate cache
```

Write strategies:

- evict after DB commit rồi read lại;
- update cache after DB commit;
- event-driven invalidation;
- versioned key để old value tự hết hạn.

DB update và Redis update không atomic mặc định. Nếu cache update thành công nhưng DB rollback, hoặc ngược lại, có stale window. Ưu tiên source-of-truth correctness và thiết kế convergence.

## Stampede và hot key

Khi key hot hết hạn, nhiều requests cùng load source:

- per-key locking/single flight;
- TTL jitter;
- refresh-ahead;
- stale-while-revalidate;
- admission/concurrency limit;
- pre-warm có kiểm soát.

Lock distributed cần TTL/owner/fencing; không thêm Redis lock máy móc cho cache miss.

## Negative caching

Cache “not found” ngắn hạn giảm repeated miss/abuse, nhưng resource mới tạo có thể vẫn bị thấy là missing. Dùng sentinel/type rõ và TTL ngắn; phân biệt error dependency với legitimate absence.

## Hibernate caches

### First-level cache

- Bắt buộc, theo `EntityManager`/persistence context.
- Identity map cho managed entities.
- Không chia sẻ qua requests/transactions khác.

### Second-level cache

- Optional, chia sẻ theo `SessionFactory`.
- Cache entity/collection data tùy provider/region strategy.
- Cần provider và concurrency strategy.
- Không mặc định tốt cho write-heavy/high-contention data.

### Query cache

- Cache query result identifiers/shape, thường phụ thuộc L2 cache.
- Invalidation rộng và data thay đổi thường xuyên có thể làm lợi ích thấp.

Spring Cache và Hibernate L2/query cache là các lớp khác nhau; không bật cả hai mà không biết key/invalidation/metrics.

## Transaction caveat

Cache advice và transaction advice order quan trọng. Evict/update cache trước DB commit có thể publish state chưa commit. Cân nhắc after-commit event hoặc transaction-aware cache integration, nhưng hiểu failure window vẫn tồn tại với remote cache.

## Test

- Gọi hai lần và assert source chỉ load một lần.
- Update/delete rồi assert evict.
- Concurrent miss và source call count.
- TTL/fake clock nếu provider hỗ trợ.
- Cache unavailable fallback.
- Tenant key isolation.
- Serialization compatibility với Redis.

Nguồn: [Spring Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html), [Spring Boot Caching](https://docs.spring.io/spring-boot/reference/io/caching.html), [Hibernate Caching](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#caching).

