# Duplicate Processing & Idempotency

## 1. Định nghĩa đúng

Idempotency nghĩa là nhiều lần thực hiện cùng một logical operation tạo ra một hiệu ứng nghiệp vụ. Nó không đồng nghĩa “consumer không bao giờ nhận message trùng”. Trong distributed system, redelivery là bình thường: response/ack/offset commit có thể mất sau khi side effect đã commit.

Idempotency key phải có scope và request fingerprint:

```text
scope = tenant/user + operation
key = UUID do client tạo
fingerprint = hash(method + canonical path + canonical business payload)
```

Cùng key, khác fingerprint phải trả `409 Conflict`; không được replay kết quả của payload khác.

## 2. HTTP request ledger

Schema tối thiểu:

```sql
create table idempotency_request (
  scope varchar(200) not null,
  idempotency_key varchar(100) not null,
  request_hash varchar(64) not null,
  status varchar(20) not null,
  resource_id varchar(100),
  response_code int,
  response_body text,
  expires_at timestamp not null,
  primary key (scope, idempotency_key)
);
```

Trong cùng DB transaction: insert key/transition state, thực hiện business write, lưu resource/result và commit. Unique index phân xử hai request chạy đồng thời. Request thua unique race đọc record hiện tại:

- `SUCCEEDED`: replay cùng status/body hoặc trả resource hiện tại;
- `PROCESSING`: `409/425` hoặc chờ có giới hạn;
- `FAILED_RETRYABLE`: chỉ owner/lease hợp lệ được retry;
- khác fingerprint: `409`.

TTL phải dài hơn retry window của client và mọi upstream queue. Xóa key quá sớm có thể chạy lại side effect. Dữ liệu audit/payment thường cần giữ lâu hơn cache response.

## 3. Redis fast gate + DB correctness

`SET idem:{scope}:{key} token NX PX ttl` giảm duplicate traffic trước DB, nhưng không phải lớp correctness cuối:

- Redis có thể restart/evict/failover;
- TTL có thể hết khi request đầu vẫn chạy;
- process có thể chết sau side effect nhưng trước khi update Redis;
- xóa lock của process khác nếu không so token.

Nếu dùng lock, release bằng compare-token Lua. Dù vậy DB unique/conditional write vẫn bắt buộc. Redis là optimization, DB là authority.

## 4. Kafka inbox pattern

Consumer thực hiện inbox insert và business mutation trong cùng local transaction:

```sql
begin;
insert into processed_message(consumer_name, event_id, processed_at)
values (:consumer, :event_id, now())
on conflict do nothing;

-- Chỉ khi insert count = 1:
update account set balance = balance - :amount
where account_id = :id and balance >= :amount;
insert into outbox_event(...);
commit;
```

Ack/commit Kafka offset sau DB commit. Nếu crash sau DB commit trước offset commit, message được giao lại nhưng inbox unique biến lần hai thành no-op. Nếu crash trước DB commit, cả inbox và business write rollback rồi retry an toàn.

Không insert inbox ở transaction khác business write; crash giữa hai transaction sẽ hoặc đánh dấu đã xử lý nhưng chưa trừ tiền, hoặc trừ tiền mà chưa đánh dấu.

## 5. Exactly-once có phạm vi

Kafka idempotent/transactional producer giúp deduplicate producer retry và atomically write nhiều Kafka records/offset trong Kafka. Nó không tự atomically commit PostgreSQL, gọi payment provider hoặc gửi email. Với Kafka + DB, inbox/outbox và idempotent side effects vẫn cần.

Payment call nên chuyển provider idempotency key từ business operation ID. Nếu timeout, query trạng thái bằng cùng reference trước khi capture lại. Không dựa vào retry count.

## 6. Failure matrix

| Crash point | Trạng thái | Retry |
|---|---|---|
| trước DB transaction | chưa có effect | chạy bình thường |
| sau inbox insert nhưng trước business write, cùng transaction | rollback tất cả | chạy bình thường |
| sau business commit, trước Kafka offset | effect đã có | inbox phát hiện duplicate |
| sau gọi payment thành công, trước lưu DB | remote có effect, local không biết | query/retry với provider idempotency key |
| sau outbox commit, trước publish | event còn trong DB | relay/CDC publish sau |
| sau publish, trước relay checkpoint | có thể publish lại | downstream inbox dedupe |

## 7. Những lỗi hay gặp

- Dùng `order_id` ngẫu nhiên mới ở mỗi retry thay vì giữ logical operation ID.
- Chỉ check `exists` rồi mới insert: hai request vẫn race; unique constraint phải phân xử.
- Đánh dấu processed trước khi transaction nghiệp vụ commit.
- Swallow exception rồi commit Kafka offset.
- Dùng payload equality thô; JSON field order làm fingerprint khác.
- Cache response chứa token/PII quá lâu hoặc replay cho sai principal.
