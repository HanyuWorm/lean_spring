# 10 — Reliability và disaster recovery

## Scope chính xác

- Instance failure: self-healing/replacement.
- Zone failure: distribute stateless capacity và data HA qua zones.
- Region failure: replicated state, routing/failover, dependency và runbook.
- Control-plane failure: existing data plane có thể chạy nhưng deploy/scale thay đổi bị hạn chế.
- Logical corruption/security incident: replica có thể replicate lỗi; cần PITR/immutable backup.

## DR patterns

| Pattern | RTO/cost tương đối | Mô tả |
|---|---|---|
| Backup & restore | chậm/rẻ | dựng lại và restore |
| Pilot light | trung bình | data/core luôn chạy, compute scale khi DR |
| Warm standby | nhanh/đắt hơn | bản thu nhỏ luôn chạy |
| Multi-site active-active | nhanh/rất phức tạp | cả hai phục vụ, data/routing conflict khó |

AWS multi-AZ/GCP regional HA không tự bảo vệ region. Multi-region service không có nghĩa application dependency nào cũng multi-region.

## Design method

Đặt SLO, RPO, RTO theo business journey và failure scope. Map dependency graph; chọn redundancy/replication; capacity failover; automate nhưng có fencing; backup/PITR; test game day. DNS TTL/cache, certificate, secrets/KMS, quota, network, identity, third party và cold cache thường bị quên.

## Data

Synchronous replication giảm RPO nhưng tăng latency/coupling; async có loss/lag window. Failover phải ngăn old writer (fencing) và client retry idempotent. Sau recovery reconcile lost/duplicate writes.
