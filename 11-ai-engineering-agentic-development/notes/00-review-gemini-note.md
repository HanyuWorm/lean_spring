# 00 — Review note Gemini

## Những điểm đúng

- Spec/context có cấu trúc tốt hơn prompt ad-hoc.
- Plan → act → observe/reflect là loop nền tảng của agent dùng tool.
- Task nhỏ, self-contained và verification command giảm context drift.
- Terminal/test output thật tạo deterministic feedback tốt hơn mô tả lỗi bằng lời.
- Role separation Planner/Coder/Tester/Reviewer hữu ích khi trách nhiệm và evidence rõ.

## Những điểm cần sửa

### “Khóa spec để code khớp 100%”

Spec luôn có unknown và thay đổi. Đúng hơn: version spec/API/schema, ghi ADR, trace requirement → task → test, và thay đổi qua review. Contract test/eval là evidence; câu chữ “100%” không đo được.

### “Multi-agent tránh hallucination”

Nhiều agent có thể nhân hallucination, token và coordination failure. Default là một agent có tools và checkpoints. Tách agent khi workstream thật sự độc lập, context khác biệt, parallelism có lợi và có deterministic merge/eval.

### “Tự trị không cần human”

Đúng cho local/reversible/read-only tasks trong scope. External write, production change, purchase, secret/data access và destructive action cần policy + approval. Autonomy là một budget có boundary, không phải boolean.

### “Mở chat mới cho mỗi task”

Mục tiêu là context hygiene, không bắt buộc session mới. Dùng task packet nhỏ, artifact canonical, compact/summarize và tránh đưa irrelevant history. Với stateful API, vẫn cần kiểm tra claim/assumption cũ còn đúng.

## Bộ tài liệu nâng cấp

Ngoài bốn file ban đầu, thêm:

1. `01-system-spec.md` — outcome, invariants, NFR và out-of-scope.
2. `02-architecture.md` — decisions, boundaries, contracts và failure modes.
3. `03-agent-harness.md` — tools, policies, budgets, events và approvals.
4. `04-implementation-plan.md` — atomic tasks và verification.
5. `05-eval-plan.md` — datasets, metrics, baseline, threshold và regression.
6. `06-ai-security.md` — trust boundaries, injection, data/tool/identity controls.

Template đầy đủ nằm tại `templates/`.
