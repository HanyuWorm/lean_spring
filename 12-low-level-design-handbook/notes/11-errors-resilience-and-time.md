# 11 — Error, resilience và time

## Error taxonomy

| Loại | Ví dụ | Xử lý |
|---|---|---|
| Validation | malformed ID, missing field | reject tại boundary |
| Business rejection | seat unavailable | typed result/4xx phù hợp |
| Authorization | actor không có quyền | deny, không leak existence |
| Conflict | version/idempotency mismatch | retry có giới hạn hoặc trả conflict |
| Transient infrastructure | timeout/503 | retry policy/bulkhead/fallback nếu safe |
| Permanent infrastructure | schema/config/auth sai | fail fast + alert |
| Programming defect | impossible state/null | fail, telemetry, fix code |

Không catch `Exception` rồi trả “success=false”. Giữ cause/metadata nội bộ, map message public an toàn.

## Result hay exception?

- Expected alternate flow → sealed result/error code.
- Cannot fulfill contract do infrastructure/unexpected → exception.
- Hot path có rejection thường xuyên → result tránh exception-as-control-flow.
- API boundary map cả hai về stable error contract có correlation ID.

## Retry

Retry thuộc boundary biết idempotency và failure semantics. Exponential backoff + jitter + max attempts + deadline. Không retry validation, decline hoặc unknown payment outcome mù quáng. Tránh retry ở controller + client + SDK cùng lúc.

## Timeout và cancellation

Mỗi IO có timeout; tốt hơn là propagate deadline còn lại. Cancellation cần tới queued/running tool nếu có thể. Cleanup resource trong `finally`/try-with-resources. Timeout không chứng minh downstream không thực hiện side effect.

## Clock

Không gọi `Instant.now()` rải rác trong domain. Inject `Clock` hoặc truyền `now` để test expiry/boundary/timezone/DST deterministically.

```java
boolean isExpiredAt(Instant now) { return !now.isBefore(expiresAt); }
```

Lưu instant UTC cho event; timezone là business input khi tính lịch/ngày. Không cộng “24 giờ” nếu rule là “ngày tiếp theo theo local calendar”.

## Randomness và ID

Inject `IdGenerator`/`RandomGenerator` khi behavior cần deterministic test. ID phải xét collision, ordering, information leakage và multi-node generation. Không dùng sequential ID public nếu enumeration là risk.

## Resource lifecycle

Owner tạo resource phải đóng hoặc transfer ownership rõ. Stream/iterator từ DB có transaction/lifecycle; không trả lazy stream ra ngoài boundary rồi đóng connection sớm. Scheduler task cần overlap policy, idempotency và clock skew handling.
