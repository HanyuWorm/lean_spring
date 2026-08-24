# 12 — Production engineering

## SLO theo outcome

Định nghĩa availability và latency của **successful task**, không chỉ HTTP 200 từ model API. Ví dụ: 99% request read-only trả grounded answer trong 8 giây; 100% refund action có approval và idempotency; critical policy violation bằng 0 trong release set.

## Kiến trúc tham chiếu

```text
API → auth/tenant → task service → context/retrieval
                         ↓
                   model gateway
                         ↓
               policy → tool executor
                         ↓
             checkpoint/event/audit store
```

Model gateway quản lý provider adapter, capability, timeout, retry/routing và usage. Task service sở hữu state/budget; tool executor sở hữu side effect. Tách chúng giúp đổi model mà không đổi business security.

## Reliability

- Deadline end-to-end; child timeout phải nhỏ hơn deadline còn lại.
- Retry chỉ lỗi transient, backoff + jitter, giới hạn attempt và idempotency.
- Circuit breaker/bulkhead cho provider và tool downstream.
- Queue + durable checkpoint cho task dài; resume từ state xác định.
- Fallback có capability tương thích; model rẻ hơn không luôn hỗ trợ cùng tool/schema.
- Graceful degradation: search-only, answer-without-action hoặc human handoff.

## Cost và latency

- Cắt irrelevant context, prefix ổn định để cache, summarize có provenance.
- Route task đơn giản sang model nhỏ sau khi eval chứng minh.
- Parallelize independent retrieval/tools nhưng giới hạn fan-out.
- Streaming cải thiện perceived latency, không giảm total compute.
- Theo dõi cost per successful task; retry/loop có thể che dưới average token.

## Version và rollout

Version prompt, model alias/snapshot, tool schema, retriever, corpus và policy. Chạy offline regression → shadow → canary theo tenant/slice → ramp. Có rollback cả config và index. Preview/beta capability không đi critical path nếu thiếu fallback/contract test.

## Build vs buy

Mua/managed khi commodity và đội nhỏ; tự xây phần chứa domain policy, proprietary data pipeline, eval và tool authorization. Tránh khóa business state vào vendor conversation object: lưu task state canonical và adapter hóa provider API.

## Production checklist

- SLO/error budget, quota và capacity test.
- Data residency, retention, provider training/privacy terms.
- Eval gates và continuous sampling.
- Audit, redaction, incident response, kill switch.
- Provider outage và corrupted-index game day.
- Human escalation có context/evidence, không chỉ transcript dài.
