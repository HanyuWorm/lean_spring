# 03 — JPA/Hibernate Memory Management

## 1. Persistence context là một managed object graph

Hibernate `Session`/JPA `EntityManager` chứa persistence context — first-level cache bắt buộc theo dõi entity đang managed. Nó cần:

- identity map để cùng một database row ánh xạ về cùng entity instance;
- trạng thái/snapshot phục vụ dirty checking;
- action queue cho insert/update/delete chờ flush;
- collection wrapper và association đã load.

Vì vậy transaction càng dài và càng load nhiều entity, live set càng lớn. Đây thường không phải “Hibernate leak”; application đã yêu cầu persistence context giữ object để đảm bảo unit of work.

## 2. `flush()` khác `clear()`

- `flush()` đồng bộ thay đổi đang pending xuống database, nhưng entity vẫn managed.
- `clear()` detach toàn bộ entity khỏi persistence context.
- `detach(entity)` chỉ detach một entity cụ thể.

Batch insert/update lớn cần cả flush và clear định kỳ:

```java
@Transactional
public void importRows(List<InputRow> rows) {
    int batchSize = 100;
    for (int i = 0; i < rows.size(); i++) {
        entityManager.persist(toEntity(rows.get(i)));
        if ((i + 1) % batchSize == 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

Đo batch size với database thật. `clear()` làm entity bị detached; code sau đó không được giả định dirty checking vẫn cập nhật chúng.

### Vì sao `saveAll(oneMillionEntities)` vẫn nguy hiểm?

API nhận sẵn list một triệu entity đã giữ toàn bộ input trên heap. Trong một transaction, persistence context/action queue có thể giữ thêm state. JDBC batching giảm round-trip, không tự giải phóng list hoặc persistence context.

Thiết kế đúng hơn:

```text
read page/chunk -> map -> persist -> flush -> clear -> release chunk -> next chunk
```

Nếu nghiệp vụ cho phép, tách transaction theo chunk để giảm rollback scope, lock time và connection hold time. Đổi transaction boundary là quyết định nghiệp vụ, không chỉ tối ưu memory.

## 3. Dirty checking cost

Khi flush, Hibernate phải xác định entity nào thay đổi. Số entity managed lớn làm tăng:

- memory cho entity/snapshot;
- CPU dirty checking;
- thời gian flush;
- thời gian giữ transaction và connection.

`@Transactional(readOnly = true)` thể hiện intent và có thể cho framework/provider cơ hội tối ưu, nhưng không nên coi là bảo đảm rằng query lớn sẽ không materialize/retain object. Pagination, projection và fetch plan mới quyết định volume chính.

## 4. Đọc dữ liệu lớn

### Không dùng `findAll()` cho bảng lớn

Dùng:

- `Page` khi cần cả tổng số bản ghi;
- `Slice` khi chỉ cần biết còn trang sau, tránh count query;
- keyset pagination khi offset lớn;
- DTO/interface projection để chỉ lấy cột cần thiết;
- streaming/cursor khi provider và driver hỗ trợ.

Stream database phải được consume và đóng trong transaction phù hợp:

```java
@Transactional(readOnly = true)
public void export() {
    try (Stream<OrderView> rows = repository.streamForExport()) {
        rows.forEach(this::writeIncrementally);
    }
}
```

“Stream” không đảm bảo low memory nếu downstream gọi `toList()`, serializer buffer toàn bộ hoặc persistence context vẫn giữ mọi entity. Projection/read-only/periodic clear cần được kiểm chứng bằng profile.

## 5. Fetch plan và object graph

### EAGER quá mức

Một root entity có thể kéo cả graph. Multiple collection join-fetch có thể tạo Cartesian product lớn: database trả rất nhiều row rồi Hibernate de-duplicate thành object graph, làm cả network và heap tăng.

### LAZY và serialization

Trả entity trực tiếp từ controller khiến JSON serializer đi qua association, kích hoạt query ngoài dự kiến, N+1 hoặc graph khổng lồ. Nên map sang response DTO trong application boundary.

### `@EntityGraph`

Entity graph hữu ích khi query cần dữ liệu liên quan, nhưng không phải “fetch everything safely”. Mỗi use case cần graph vừa đủ; đo số row SQL và số object allocation.

## 6. Open Session in View (OSIV)

OSIV giữ persistence context mở qua web request để lazy loading còn hoạt động ở view/controller serialization. Nó không tự động là leak, nhưng:

- kéo dài vòng đời managed graph;
- che giấu query ngoài service transaction;
- dễ gây N+1 và response-dependent memory;
- làm ownership/fetch boundary khó quan sát.

Với API service, thường nên tắt OSIV và fetch/map DTO trong transaction rõ ràng. Nếu giữ OSIV, phải có lý do, query monitoring và response-size limit.

## 7. First-level, second-level và query cache

| Cache | Phạm vi | Rủi ro |
|---|---|---|
| First-level | một persistence context | transaction dài giữ nhiều entity |
| Second-level | nhiều session, theo cache provider | capacity/cardinality, invalidation, stale data |
| Query cache | cache key/result identifier | tổ hợp parameter lớn, invalidation phức tạp |

Second-level cache là cache thật sự và phải bounded/observed. Đừng cache entity có write rate cao hoặc graph lớn nếu invalidation làm hit rate thấp. Cache remote giảm resident heap của app nhưng serialization/deserialization và temporary object vẫn dùng heap.

## 8. JDBC batching không đồng nghĩa memory batching

`hibernate.jdbc.batch_size` và ordered insert/update có thể giảm round-trip, nhưng:

- ID generation strategy có thể ảnh hưởng khả năng batch;
- một transaction vẫn có thể quá lớn;
- input collection và persistence context vẫn cần giới hạn;
- driver có thể buffer batch/payload;
- rollback toàn bộ batch lớn có operational cost.

Đánh giá đồng thời throughput, heap, transaction duration, DB locks và failure recovery.

## 9. Checklist JPA/Hibernate

- Query tối đa trả bao nhiêu row/cột/association?
- Có DTO projection thay vì managed entity không?
- Transaction/persistence context giữ bao nhiêu entity ở peak?
- Batch loop có `flush()` và `clear()` định kỳ không?
- Input có được đọc theo chunk thay vì thành một list lớn không?
- Stream/cursor có được đóng và không bị downstream collect không?
- OSIV có chủ đích hay chỉ theo default?
- L2/query cache có capacity, TTL và metric không?
- API có trả entity trực tiếp hoặc serialize lazy graph không?
- Load test có data cardinality gần production không?
