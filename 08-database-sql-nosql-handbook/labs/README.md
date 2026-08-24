# Database comparison lab

Lab dùng cùng commerce domain để so MySQL, PostgreSQL và MongoDB. Credentials mặc định chỉ dành cho local learning.

## Versions và ports

| Service | Image | Host port | Database/user |
|---|---|---:|---|
| MySQL | `mysql:9.7` (LTS line tag) | 3307 | `commerce` / `app` |
| PostgreSQL | `postgres:18.6` | 5433 | `commerce` / `app` |
| MongoDB | `mongo:8.3` (stable line tag) | 27018 | `commerce`, auth root |

Line tags MySQL/MongoDB có thể trỏ sang patch mới. Production phải pin exact tested patch hoặc image digest; lab cố ý theo stable line để học patch hiện tại.

## Khởi động

```powershell
Set-Location 08-database-sql-nosql-handbook/labs
Copy-Item .env.example .env
docker compose config
docker compose up -d
docker compose ps
```

Kết nối bằng client trong container:

```powershell
docker compose exec mysql mysql -uapp -papp_lab_only commerce
docker compose exec postgres psql -U app -d commerce
docker compose exec mongo mongosh -u root -p root_lab_only --authenticationDatabase admin commerce
```

Nếu đổi password trong `.env`, thay password ở command tương ứng. `.env` không được commit.

Init scripts chỉ chạy khi volume rỗng. Dừng nhưng giữ data:

```powershell
docker compose down
```

Reset hoàn toàn lab — lệnh này **xóa ba named volumes và toàn bộ dữ liệu lab**:

```powershell
docker compose down --volumes
docker compose up -d
```

## Bài 1 — Query và execution plan

Chạy [queries/mysql.sql](queries/mysql.sql), [queries/postgres.sql](queries/postgres.sql) và [queries/mongo.js](queries/mongo.js). Dataset seed nhỏ nên planner có thể chọn scan; đó là đúng cost decision, không phải index hỏng. Muốn benchmark, sinh ít nhất hàng trăm nghìn row với distribution/skew thực tế rồi `ANALYZE`.

Quan sát:

- rows/docs examined so với returned;
- estimate so với actual;
- index condition và sort;
- heap/table/document fetch;
- buffers/I/O và temp spill;
- query latency khi cache lạnh/nóng.

## Bài 2 — Atomic inventory decrement

MySQL/PostgreSQL:

```sql
UPDATE products
SET stock = stock - 3, version = version + 1
WHERE id = 2 AND stock >= 3;
```

Kiểm tra affected rows. MongoDB:

```javascript
db.products.findOneAndUpdate(
  { _id: NumberLong(2), stock: { $gte: 3 } },
  { $inc: { stock: -3, version: NumberLong(1) } },
  { returnDocument: "after" }
)
```

Mục tiêu: invariant `stock >= 0` được bảo vệ trong một atomic command, không phải `SELECT/find` rồi update riêng.

## Bài 3 — Tái hiện lock wait

Mở hai terminal SQL cùng engine.

Session A:

```sql
BEGIN;
UPDATE products SET stock = stock - 1 WHERE id = 1;
-- Không commit ngay.
```

Session B:

```sql
BEGIN;
UPDATE products SET stock = stock - 1 WHERE id = 1;
-- Quan sát session này chờ row lock.
```

Commit A rồi quan sát B tiếp tục. Lặp lại với update product 1 rồi 2 ở A, product 2 rồi 1 ở B để tạo deadlock. Một transaction sẽ bị abort. Viết retry outer transaction có backoff và giới hạn.

## Bài 4 — Optimistic locking

Đọc `stock, version`, rồi hai client cùng chạy:

```sql
UPDATE products
SET stock = :new_stock, version = version + 1
WHERE id = :id AND version = :expected_version;
```

Chỉ một client affected rows = 1. Client kia phải reload/retry hoặc trả conflict; không coi zero row là success.

## Bài 5 — Idempotency và Outbox

Trong một transaction:

1. insert order với `(customer_id, idempotency_key)` unique;
2. insert line items và update inventory;
3. insert outbox event;
4. commit.

Gửi cùng idempotency key hai lần; unique constraint/index phải chặn order thứ hai. Sau đó giả lập relay đọc unpublished event. Thiết kế consumer dedupe vì relay có thể publish lại sau crash.

MongoDB sample có unique `(customerId, idempotencyKey)`, nhưng atomicity giữa order và outbox cần transaction trên replica set. Compose tối giản đang chạy standalone để tập modeling/query; muốn transaction/change streams phải chuyển thành replica-set topology.

## Bài 6 — Modeling comparison

Relational order items là table riêng để enforce FK/query product. MongoDB embed item snapshot để lấy order trong một document và atomic aggregate. Trả lời:

- Product name đổi có làm invoice cũ đổi không?
- Nếu một order có hàng triệu events/items, model nào vỡ trước?
- Query “mọi order chứa product X” cần index gì?
- Source of truth của price hiện tại và price tại purchase?
- Mongo transaction có đáng dùng hay aggregate cần đổi?

## Definition of done

- Không chỉ xem output; giải thích vì sao plan chọn như vậy.
- Tái hiện ít nhất một lock wait và một unique-key race.
- Viết ADR chọn engine cho ledger và catalog.
- Thực hiện `docker compose down`, khởi động lại và xác nhận persistence.
- Thực hiện reset/restore lab; trong production phải có backup/PITR drill thực tế.
