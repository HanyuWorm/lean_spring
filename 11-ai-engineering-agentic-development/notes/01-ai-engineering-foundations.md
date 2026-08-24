# 01 — AI Engineering foundations

## Mental model

LLM dự đoán token tiếp theo dựa trên context; reasoning/tool behavior vẫn probabilistic. Application phải cung cấp contract, data, tools, validation và feedback loop. Không coi model là database, policy engine hay transaction coordinator.

- **Token:** đơn vị model đọc/sinh, không bằng word/character.
- **Context window:** tổng instruction, input, history, tool definitions/results và output budget.
- **Inference:** chạy model để tạo output.
- **Sampling:** lựa chọn token; temperature/top-p ảnh hưởng diversity nhưng không tạo truth.
- **Reasoning effort/mode:** provider-specific control đổi quality/latency/cost; chọn bằng eval.
- **Multimodal:** input/output text, image, audio/video tùy model/API; mỗi modality có token/cost/safety riêng.
- **Embedding:** vector biểu diễn semantic similarity; không phải factual answer.

## Training, inference và adaptation

Pretraining tạo general capability. Instruction tuning/alignment định hình behavior. Prompt/context/RAG thay input lúc inference; fine-tuning thay model behavior/weights theo technique. Ưu tiên prompt + retrieval + eval trước fine-tuning; fine-tune khi pattern ổn định và dữ liệu/ROI đủ.

## Three application planes

1. **Model plane:** provider/model/parameters/API.
2. **Context plane:** instructions, retrieval, memory, tool catalog/state.
3. **Control plane:** policy, orchestration, approval, eval, telemetry, budget.

Agent harness thuộc control plane; model không được tự quyết security boundary.

## Failure taxonomy

- Knowledge missing/stale → retrieval/tool.
- Retrieval miss/noise → data/chunk/query/rerank.
- Instruction conflict/context pollution → context assembly.
- Invalid structure → schema/constrained output/retry.
- Wrong tool/arguments → tool description/schema/eval/policy.
- Correct call, wrong side effect → business validation/idempotency/approval.
- Hallucinated evidence/citation → grounded generation + citation verification.
- Non-deterministic regression → eval dataset + versioned config.
