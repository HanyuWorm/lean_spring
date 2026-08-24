# 10 — Observability cho AI system

Trace phải nối user outcome với model, retrieval và tools; chỉ ghi tổng token không đủ điều tra.

## Trace model

```text
request
├─ context assembly
├─ retrieval → rerank
├─ model response/tool proposal
├─ policy + approval
├─ tool execution
├─ model synthesis
└─ evaluator / feedback
```

Mỗi span nên có correlation/trace ID, component/version, latency, status, retry, token/cache/cost estimate và stop reason. Tool span thêm tool name/version, sanitized arguments, idempotency key, policy result và side-effect result.

## Metrics cần theo dõi

- Product: task success, handoff, abandonment, user correction.
- Quality: groundedness, citation validity, tool accuracy, eval pass rate.
- Retrieval: hit/recall proxy, empty result, rerank latency, stale source.
- Reliability: error/timeout/retry/loop/budget-exhausted rate.
- Security: denied calls, approval rate, injection alerts, cross-tenant attempts.
- Efficiency: input/output/cached tokens, model/tool latency, cost per successful task.

OpenTelemetry GenAI semantic conventions vẫn tiến hóa; pin version và đặt lớp mapping nội bộ thay vì để vendor-specific attribute lan toàn codebase.

## Privacy

- Default không log raw prompt/document/tool payload ở production.
- Redact secret, PII, credential và customer content trước export.
- Tách debug sampling có thời hạn, quyền hạn và audit.
- Hash/pseudonymize identifier; không dùng trace backend làm data lake không kiểm soát.
- Ghi model/prompt/schema version, không cần ghi chain-of-thought nội bộ.

## Runbook

Khi quality giảm: xác định slice/model/version → kiểm retrieval/context → inspect tool/policy → replay sanitized eval cases → rollback/reroute nếu vượt gate. Khi cost tăng: phân rã input/output/cache/retry/tool loop, không chỉ đổi sang model rẻ hơn.
