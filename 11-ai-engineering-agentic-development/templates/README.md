# Spec-driven templates

Copy các file cần thiết vào project và điền ngắn gọn theo mức rủi ro. Với feature AI production, nên dùng đủ sáu file; change nhỏ có thể gộp nhưng không bỏ acceptance/eval/security.

1. [01-system-spec.md](01-system-spec.md): outcome, scope, invariants, NFR.
2. [02-architecture.md](02-architecture.md): boundaries, decisions, contracts, failure.
3. [03-agent-harness.md](03-agent-harness.md): tools, policies, budgets, state, approvals.
4. [04-implementation-plan.md](04-implementation-plan.md): task atomic và verification.
5. [05-eval-plan.md](05-eval-plan.md): dataset, grader, baseline, gates.
6. [06-ai-security.md](06-ai-security.md): data flow, threats, controls, incident.
7. [task-packet.md](task-packet.md): context tối thiểu cho từng coding-agent task.

Mỗi claim “done” phải trỏ tới diff, test/eval output hoặc artifact kiểm chứng được.
