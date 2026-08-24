# 09 — Evals và testing

AI system không deterministic tuyệt đối, nhưng release vẫn phải dựa trên bằng chứng có thể lặp lại. Eval-driven development tương đương test + benchmark + product analytics cho behavior của model system.

## Eval stack

1. **Unit/contract tests:** schema, tool policy, parsers, budgets, idempotency.
2. **Retrieval eval:** Recall@k, Precision@k, MRR, nDCG, ACL leakage.
3. **Task eval:** correctness, groundedness, citation, tool selection/arguments.
4. **Trajectory eval:** số bước, loop, policy decision, recovery và stop reason.
5. **End-to-end:** user outcome, latency, cost và side effect correctness.
6. **Red-team:** injection, exfiltration, excessive agency, harmful/invalid action.

## Dataset

Mỗi case nên có input, fixture/context, expected properties, forbidden behavior, tags và source. Bao gồm happy path, edge, adversarial, multilingual, no-answer và provider outage. Tách train/tuning set khỏi holdout regression set.

```json
{
  "id": "refund-017",
  "input": "Hoàn tiền đơn đã giao",
  "expected": {"tool": "create_refund", "approval": true},
  "forbidden": ["execute_without_approval"],
  "tags": ["side-effect", "policy"]
}
```

## Grader

- Code/assertion grader cho schema, exact fact, tool call và policy: ưu tiên.
- Reference/rule grader cho keyword, citation mapping hoặc numerical tolerance.
- Model grader cho tiêu chí ngữ nghĩa khó; rubric rõ, blind model/version và calibrate với human labels.
- Human review cho high-risk/ambiguous cases và audit sampling.

Không dùng cùng model tự tạo answer, reference và judge mà không calibration.

## Release gate

So sánh candidate với baseline trên cùng dataset và report confidence/variance. Đặt hard gate cho security/policy, non-regression cho critical slices, budget cho p95 latency/cost và manual review cho change lớn. Log prompt/model/tool/schema/dataset version để tái hiện run.

Online metric không thay offline eval: thumbs-up dễ bias; conversion có thể tăng dù answer sai hoặc unsafe. Dùng cả hai và theo dõi drift.

Lab [rag-eval-node](../labs/rag-eval-node/README.md) cung cấp metric deterministic; [05-eval-plan.md](../templates/05-eval-plan.md) là template release gate.
