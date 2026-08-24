# 06 — Agent workflow và harness

Một agent production là model nằm trong **harness kiểm soát execution**. Model đề xuất; application xác thực, cấp quyền, thực thi và ghi trace.

## Ba mức thiết kế

1. **Deterministic workflow:** code quyết định state machine; model chỉ làm một số bước. Ưu tiên khi quy trình ổn định.
2. **Single-agent + tools:** model chọn bước/tool trong giới hạn. Phù hợp investigation hoặc task có nhánh khó đoán.
3. **Multi-agent:** nhiều context/role/workstream. Chỉ dùng khi decomposition thật sự độc lập và có merge/eval rõ.

Agent loop tối thiểu:

```text
receive task
  → assemble scoped context
  → model proposes answer or tool call
  → validate schema + policy + budget
  → require approval when necessary
  → execute idempotently
  → append observation
  → repeat or stop
```

“Reflect” chỉ có giá trị nếu nhận evidence mới như test output, tool result hoặc grader feedback. Cho model tự phê bình bằng cùng context không đảm bảo đúng hơn.

## Harness phải sở hữu gì?

- Tool registry và allowlist theo task/identity.
- JSON/schema validation và semantic validation.
- Authorization tách khỏi model; approval cho side effect nhạy cảm.
- `max_steps`, `max_tool_calls`, token/cost/deadline/retry budget.
- Timeout, cancellation, circuit breaker và retry có phân loại lỗi.
- Idempotency key cho action có thể retry.
- State machine và durable checkpoint cho long-running task.
- Event trace có input/output đã redact, policy decision và evidence.
- Stop reasons: completed, denied, budget exhausted, timeout, invalid loop, human required.

## Planner, coder, reviewer, tester

Đây là trách nhiệm, không nhất thiết là bốn agent/model call. Một workflow có thể dùng cùng model với context packet khác nhau. Reviewer phải kiểm tra diff/test/eval cụ thể; “looks good” không phải evidence. Tester nên sinh và chạy test, nhưng acceptance criteria do spec sở hữu.

## Khi nào multi-agent đáng dùng?

Dùng khi có thể trả lời “có” cho hầu hết câu sau:

- Các workstream có context và output contract độc lập?
- Chạy song song giảm critical path đáng kể?
- Có owner/merge rule khi kết quả xung đột?
- Có evaluator hoặc test độc lập?
- Chi phí/latency/coordination nằm trong SLO?

Tránh khi agent chỉ truyền bản tóm tắt nối tiếp, cùng đọc toàn repository, hoặc không có tiêu chí dừng.

## Retry đúng cách

- Retry transport/429/5xx với exponential backoff + jitter và deadline.
- Không retry validation/policy deny như transient failure.
- Tool write cần idempotency; kiểm tra outcome trước replay.
- Sau lỗi lặp lại, giảm scope, đổi strategy hoặc handoff; không loop vô hạn.

Lab [agent-harness-node](../labs/agent-harness-node/README.md) hiện thực allowlist, approval, retry, budgets và trace hoàn toàn offline.
