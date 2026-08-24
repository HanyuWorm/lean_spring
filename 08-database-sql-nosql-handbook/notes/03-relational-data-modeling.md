# 03 — Relational data modeling

## Bắt đầu từ invariant, không bắt đầu từ entity class

Liệt kê trước:

- dữ liệu nào có identity và lifecycle độc lập;
- invariant nào phải đúng ở mọi thời điểm commit;
- operation nào cần atomic;
- access pattern và retention;
- ownership/source of truth;
- volume và growth.

JPA mapping là consumer của schema, không phải lý do duy nhất để thiết kế schema.

## Key và constraint

- **Primary key:** identity ổn định, non-null, unique.
- **Natural key:** có nghĩa nghiệp vụ (`country_code`, external reference); chỉ làm PK khi thật sự bất biến và nhỏ.
- **Surrogate key:** tách identity kỹ thuật khỏi thuộc tính thay đổi.
- **Foreign key:** bảo vệ referential integrity; cân nhắc index ở phía referencing để delete/update parent không scan lớn.
- **Unique:** triển khai idempotency và business uniqueness đúng dưới concurrency.
- **Check:** encode range/state (`stock >= 0`, `amount >= 0`).
- **Not null:** unknown có hợp lệ không phải quyết định tùy tiện.

Validation ở Java cho lỗi thân thiện; constraint trong DB chống mọi writer và race. Cần cả hai.

## Normalization

- **1NF:** thuộc tính có miền giá trị nguyên tử theo model, không lặp group.
- **2NF:** non-key phụ thuộc toàn bộ candidate key.
- **3NF:** non-key không phụ thuộc bắc cầu vào non-key khác.
- **BCNF:** determinant là candidate key.

Normalization giảm update anomaly và xác định source of truth. Denormalization là optimization có chủ đích khi read path quan trọng và phải có:

1. field/source canonical;
2. cơ chế đồng bộ (transaction, CDC/event, batch repair);
3. staleness SLA;
4. reconciliation và alert.

## Aggregate và transaction boundary

Order cùng line items thường là một aggregate: tạo và đọc cùng nhau, item không có lifecycle hữu ích ngoài order. Customer và product thường độc lập. Trong relational model vẫn có thể dùng nhiều table nhưng transaction boundary phản ánh invariant.

Snapshot tên/giá sản phẩm vào order item là đúng nếu đó là sự thật lịch sử tại thời điểm mua, không phải “duplicate xấu”. Không join giá hiện tại để tái dựng hóa đơn cũ.

## Many-to-many

Dùng association table và đưa thuộc tính quan hệ vào đó:

```sql
CREATE TABLE product_category (
    product_id  bigint NOT NULL,
    category_id bigint NOT NULL,
    assigned_at timestamp NOT NULL,
    PRIMARY KEY (product_id, category_id)
);
```

Entity ORM hai chiều không bắt buộc. Chỉ map hướng traversal thật sự cần.

## Soft delete

Soft delete tạo chi phí lâu dài:

- mọi query phải filter;
- unique key với row đã xóa trở nên khó;
- foreign key/lifecycle mơ hồ;
- index phình và optimizer thấy nhiều row chết;
- dữ liệu vẫn tồn tại, không tự đáp ứng privacy erasure.

Chỉ dùng khi có requirement phục hồi/audit cụ thể. Cân nhắc status state machine, temporal history/audit table hoặc archive hard delete. PostgreSQL có partial unique index; MySQL thường cần generated column/thiết kế key khác.

## Multi-tenancy

| Model | Ưu điểm | Rủi ro |
|---|---|---|
| Shared schema + `tenant_id` | Rẻ, vận hành đơn giản | data leak do thiếu predicate; noisy neighbor |
| Schema per tenant | Cách ly logic hơn | migration/catalog scale phức tạp |
| Database per tenant | Cách ly/restore tốt | fleet management và connection cost |

Shared schema cần tenant trong unique/index prefix theo access pattern, guard ở application/data access, và kiểm thử cross-tenant. PostgreSQL Row-Level Security có thể thêm lớp bảo vệ nhưng không thay thế review quyền và context.

## ID strategy

- Auto-increment/bigint: nhỏ, locality tốt; có thể lộ số lượng và khó tạo offline.
- UUIDv4: phân tán nhưng random B-tree writes, 16 byte và index lớn.
- UUIDv7/time-sortable: locality tốt hơn, vẫn cần kiểm tra collision/clock/library.
- Business ID: dùng ở boundary nhưng thường giữ surrogate PK bên trong.

Trong InnoDB, primary key nằm trong mọi secondary-index leaf, nên PK dài nhân chi phí toàn bộ secondary indexes.

## Schema evolution

Thực hiện expand-contract:

1. thêm schema backward-compatible;
2. deploy code có thể đọc/ghi trạng thái chuyển tiếp;
3. backfill có throttle/checkpoint;
4. chuyển read path và xác minh;
5. dừng ghi cũ;
6. xóa field/index cũ ở release sau.

Không gộp rename/drop column với deploy code duy nhất khi có rolling deployment.

## Review checklist

- Invariant nào do DB bảo vệ, invariant nào do service bảo vệ?
- Cardinality và growth của từng relationship?
- Delete/update cascade có thể khóa bao nhiêu row?
- Index có hỗ trợ FK và critical access path?
- Audit, retention, GDPR/PII và encryption boundary?
- Online migration và rollback/roll-forward?
