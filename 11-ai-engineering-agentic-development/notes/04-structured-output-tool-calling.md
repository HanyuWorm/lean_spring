# 04 — Structured output và tool calling

## Structured output

JSON Schema/constrained decoding tăng syntactic reliability. Sau parse vẫn phải validate business rules, authorization, existence, range và concurrency. Version schema; distinguish nullable/optional; reject unknown fields khi security cần.

## Tool contract

Tool gồm name, purpose, input schema, output/error schema, side-effect class, required permission, idempotency, timeout và data sensitivity. Mô hình chỉ **đề xuất** tool + args; application executor quyết định có chạy.

```json
{
  "name": "refund_order",
  "sideEffect": "financial",
  "approval": "required",
  "input": {"orderId":"string", "amountMinor":"integer", "reason":"string"},
  "result": {"status":"approved|rejected", "refundId":"string|null"}
}
```

## Execution pipeline

Validate schema → authenticate actor/workload → authorize action/resource → enforce approval/budget → idempotency key → execute with timeout → normalize/redact result → audit → return observation. Model không nhận raw secret/stacktrace/huge rows.

## Tool loop

Set max turns/calls/parallelism/retries/time/cost. Retry transient error với backoff; validation/policy error trả structured failure để model sửa args hoặc stop. Không retry destructive non-idempotent call mù quáng.

## Tool catalog

Quá nhiều tools tăng tokens và chọn sai. Route theo agent/domain, expose minimum set hoặc tool-search/dynamic discovery. Spring AI 2.0 có Tool Search pattern cho catalog lớn; OpenAI hiện có tool search/programmatic calling capabilities theo model/platform.
