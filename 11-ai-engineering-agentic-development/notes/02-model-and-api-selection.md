# 02 — Chọn model, API và provider

## Capability profile thay model name

Ghi requirement: modalities, reasoning difficulty, tool/structured output, context size, latency TTFT/total, throughput/rate limit, data retention/residency, safety, price và availability. Sau đó benchmark candidates trên dữ liệu thật.

## Routing tiers

- Fast/cheap model: classification, extraction, simple transformation.
- Balanced: normal RAG/tool requests.
- Frontier/high reasoning: planning, hard coding/review, ambiguous synthesis.
- Specialized: embedding, reranker, speech, image/video, moderation.

Escalate dựa trên measured confidence/failure signal, không yêu cầu model tự chấm điểm mơ hồ. Fallback provider cần semantic compatibility test; cùng prompt/schema không bảo đảm cùng behavior.

## API primitives hiện đại

- Stateful/stateless conversation continuation.
- Structured Outputs/JSON schema.
- Function/tool calling và hosted tools.
- Streaming/background execution/webhooks.
- Prompt/context caching và usage accounting.
- Reasoning controls và preserved reasoning/state tùy provider.
- Evals/graders/batch/fine-tuning tùy platform.

OpenAI hiện khuyến nghị Responses API cho reasoning, tool calling và multi-turn. Gemini có Interactions/Generate Content routes; Anthropic Messages API có tool use. Xây provider adapter quanh **capabilities mình dùng**, không abstract toàn bộ API thành lowest common denominator.

## Selection scorecard

`task success`, `constraint/schema pass`, `groundedness`, `tool accuracy`, `safety`, `p50/p95 latency`, `input/output/cache tokens`, `cost/successful task`, `rate-limit/error rate`.

Không dùng public benchmark để thay eval domain. Pin model snapshot nơi reproducibility cần; alias tiện cập nhật nhưng phải có canary/regression gate.
