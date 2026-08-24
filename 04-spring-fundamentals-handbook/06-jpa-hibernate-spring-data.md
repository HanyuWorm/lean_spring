# 06 - JDBC, JPA, Hibernate và Spring Data JPA

## Phân biệt bốn lớp

| Công nghệ | Vai trò |
|---|---|
| JDBC | Java API mức thấp để connection/statement/result set |
| `JdbcTemplate` | Spring template quản boilerplate JDBC và translate exception |
| JPA | Jakarta specification cho ORM/persistence API |
| Hibernate ORM | JPA provider phổ biến, thực thi ORM và có feature riêng |
| Spring Data JPA | Repository abstraction/query derivation trên JPA |

`JpaRepository` không phải Hibernate; nó dùng `EntityManager`, bên dưới provider thường là Hibernate.

## Entity cơ bản

```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    protected OrderEntity() {}

    // behavior/getters
}
```

Các annotation quan trọng:

- `@Entity`: persistent type.
- `@Table`, `@Column`: mapping table/column/constraints.
- `@Id`, `@GeneratedValue`: identity.
- `@Version`: optimistic locking.
- `@Enumerated(EnumType.STRING)`: lưu enum dạng string; rename vẫn là migration.
- `@Transient`: field không persist.
- `@Embedded`/`@Embeddable`: value object nhiều columns.
- `@Convert`: attribute converter.

Entity cần no-arg constructor ít nhất protected cho provider. Cẩn thận `equals/hashCode`: generated ID thay đổi từ null sang value có thể phá hash collection. Không đưa lazy collections/toString vào equality.

## Entity lifecycle/state

- Transient: object mới, chưa thuộc persistence context.
- Managed: `EntityManager` theo dõi; thay đổi được dirty-check lúc flush.
- Detached: từng managed nhưng context đã đóng/clear.
- Removed: được đánh dấu xóa.

```java
@Transactional
public void confirm(Long id) {
    var order = repository.findById(id).orElseThrow();
    order.confirm();
    // không cần save lại entity managed chỉ để dirty checking xảy ra
}
```

Spring Data `save()` vẫn cần cho aggregate mới/detached/upsert semantics cụ thể; đừng gọi máy móc sau mọi setter.

## Persistence context và first-level cache

Trong một persistence context, load cùng entity ID thường trả cùng managed instance; đây là identity map/L1 cache. Nó không phải application cache dùng chung giữa requests và thường sống theo transaction/entity manager.

`flush` đồng bộ changes thành SQL nhưng không nhất thiết commit. Query/commit có thể trigger auto-flush. Vì vậy constraint violation có thể xuất hiện lúc flush/commit, không ngay setter/save.

## Relationships

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "customer_id")
private CustomerEntity customer;

@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private final List<OrderItemEntity> items = new ArrayList<>();
```

Nguyên tắc:

- Owning side quản foreign key; `mappedBy` là inverse side.
- Helper method phải giữ hai phía nhất quán trong memory.
- `cascade` là propagate entity operation, không phải database cascade mặc định.
- `orphanRemoval` xóa child bị loại khỏi relationship; chỉ dùng khi child lifecycle thực sự thuộc parent.
- Tránh `CascadeType.ALL` máy móc, đặc biệt many-to-many.

## Lazy/Eager và N+1

N+1:

```text
1 query load 100 orders
+ 100 queries load customer/items từng order
```

Giải pháp tùy use case:

- fetch join;
- `@EntityGraph`;
- DTO projection;
- batch fetching;
- query/read model riêng.

Không đổi mọi relation thành EAGER; có thể tạo query lớn/cartesian product và vẫn khó kiểm soát. Fetch plan thuộc use case/query, không nên cố định toàn hệ thống trong mapping.

Tắt Open Session in View có chủ ý và map DTO trong transaction/use-case boundary; đừng dựa vào serializer kích hoạt lazy query.

## Repository

```java
interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    @Query("""
        select o from OrderEntity o
        join fetch o.customer
        where o.status = :status
        """)
    List<OrderEntity> findWithCustomer(@Param("status") OrderStatus status);
}
```

Query derivation tốt cho query ngắn/rõ. Khi method name dài hoặc fetch plan phức tạp, dùng explicit JPQL/native query/specification/query DSL phù hợp.

## Projection

```java
public record OrderSummary(Long id, String orderNumber, OrderStatus status) {}

@Query("select new com.example.OrderSummary(o.id, o.orderNumber, o.status) from OrderEntity o")
List<OrderSummary> findSummaries();
```

Projection giảm load entity graph cho read-only query. Không dùng entity nếu chỉ cần ba columns.

## Schema management

- `ddl-auto=create/update` tiện local demo nhưng không phải production migration strategy.
- Dùng Flyway/Liquibase, versioned migration và review SQL.
- Test migration trên database thật và production-like data volume.
- H2 khác PostgreSQL/MySQL về types, locking, planner và SQL dialect.

## Logging và diagnostics

Xem SQL và bind parameters trong môi trường dev/test phù hợp, Hibernate statistics hoặc datasource proxy. Không log sensitive values ở production. Đếm queries trong integration test để bắt N+1 regression.

## Lỗi thường gặp

- Trả entity qua REST.
- `EAGER` mọi relation.
- Transaction quá rộng chứa HTTP call.
- Không hiểu owning side/cascade.
- Generic repository/service cho mọi table làm mất aggregate boundary.
- Dùng `save()` và `flush()` như nghi thức không hiểu SQL timing.
- Tin test H2 chứng minh behavior PostgreSQL.

Nguồn: [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/jpa.html), [Hibernate ORM User Guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html).

