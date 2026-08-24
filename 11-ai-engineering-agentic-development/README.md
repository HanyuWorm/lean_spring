# AI Engineering & Agentic Development for Developers

Track dành cho senior developer/solution architect muốn dùng AI để **xây sản phẩm** và **tăng tốc software delivery**, không dừng ở chat/prompt. Nội dung lấy note Gemini làm seed, sau đó cập nhật theo tài liệu chính thức ngày **24/08/2026**.

## Baseline hiện tại

- OpenAI: Responses API cho reasoning/tool/multi-turn; GPT-5.6 family, multi-agent còn beta.
- Google: Gemini Interactions/Generate Content, function calling, structured output, context caching và ADK.
- Anthropic: Messages API/tool use, Claude Code và MCP ecosystem.
- MCP specification: revision `2026-07-28`, stateless core; prompts/resources/tools và extensions.
- A2A published specification: `0.3.0`; roadmap 1.0 không được coi là đã phát hành.
- Spring AI stable: `2.0.0`, có ChatClient, Advisors, Tool Calling, Vector Store, RAG, MCP và observability.
- Security: NIST AI 600-1 và OWASP Top 10 for Agentic Applications 2025.

Model, pricing, limits và preview status thay đổi nhanh. Không hard-code “model tốt nhất” vào kiến trúc; dùng capability profile + eval để chọn.

## Kết quả cần đạt

- Hiểu token, context, sampling, reasoning, embeddings và multimodal API.
- Thiết kế prompt/context có version, schema, citations và failure behavior.
- Xây RAG có ingestion, retrieval, rerank, grounding và retrieval eval.
- Xây tool loop/agent harness có permission, approval, budget, retry và stop condition.
- Phân biệt workflow, single-agent, multi-agent, MCP và A2A.
- Thiết kế memory/state mà không biến toàn bộ chat thành “bộ nhớ”.
- Dùng eval-driven development, trace và cost/latency/quality gates.
- Bảo vệ khỏi prompt injection, data exfiltration, tool misuse và excessive agency.
- Dùng coding agent theo spec-driven workflow có test/evidence, không giao quyền mơ hồ.
- Áp dụng Spring AI 2.0 cho Java/Spring Boot.

## Nội dung

| Chương | Trọng tâm |
|---|---|
| [00](notes/00-review-gemini-note.md) | Phản biện và nâng cấp note Gemini |
| [01](notes/01-ai-engineering-foundations.md) | Model/token/context/reasoning/multimodal |
| [02](notes/02-model-and-api-selection.md) | Chọn model/API/provider bằng eval |
| [03](notes/03-prompt-context-engineering.md) | Prompt contract và context assembly |
| [04](notes/04-structured-output-tool-calling.md) | JSON schema, tools và deterministic execution |
| [05](notes/05-rag-and-retrieval.md) | Embedding, chunk, hybrid search, rerank, citations |
| [06](notes/06-agent-workflows-and-harness.md) | Plan/act/observe, orchestrator, multi-agent |
| [07](notes/07-mcp-a2a-interoperability.md) | MCP 2026-07-28, A2A 0.3.0 |
| [08](notes/08-memory-and-state.md) | Conversation, working, episodic, semantic memory |
| [09](notes/09-evals-and-testing.md) | Dataset, graders, regression và red-team |
| [10](notes/10-observability.md) | Trace, tokens, tools, quality/cost telemetry |
| [11](notes/11-safety-security.md) | Prompt injection, excessive agency, privacy |
| [12](notes/12-production-engineering.md) | SLO, routing, cache, budget, fallback |
| [13](notes/13-spec-driven-ai-development.md) | SDD và coding-agent workflow |
| [14](notes/14-spring-ai-2-java.md) | Spring AI 2.0 cho Java developer |
| [15](notes/15-landscape-2024-2026.md) | Những thay đổi cần biết gần đây |
| [16](notes/16-interview-questions.md) | 60 câu hỏi có đáp án |

## Project thực hành

- [agent-harness-node](labs/agent-harness-node/README.md): harness chạy offline, tool allowlist, approval, retry, budget, trace và eval.
- [rag-eval-node](labs/rag-eval-node/README.md): Precision@k, Recall@k, MRR và regression gate chạy offline.
- [spring-ai-safe-tools](labs/spring-ai-safe-tools/README.md): Spring AI 2.0 `@Tool`, policy wrapper và test không cần API key.

Bộ tài liệu SDD nâng cấp nằm trong [templates](templates/README.md). Nguồn chính thức ở [SOURCES.md](SOURCES.md).

## Quy tắc cốt lõi

- Evals trước optimization; prompt hay model mới không được merge chỉ vì “trông có vẻ tốt”.
- Model output luôn untrusted; schema validation không chứng minh business correctness.
- Tool description là interface cho model; executor mới là security boundary.
- Context ít nhưng liên quan tốt hơn dump toàn repository/chat history.
- Single-agent + tools là default; multi-agent chỉ khi workstream độc lập và merge/eval rõ.
- Side effect external/destructive/costly cần authorization và approval rõ.
- RAG failure trước hết là retrieval/data problem, không luôn là prompt problem.
- Mọi run có budget: turns, tools, tokens, latency, money và retry.
