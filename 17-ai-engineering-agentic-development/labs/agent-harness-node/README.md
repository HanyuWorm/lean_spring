# Agent Harness Node

Lab offline minh họa boundary quan trọng nhất của agent runtime: model chỉ **đề xuất** tool call; harness enforce allowlist, approval, retry, budget và trace. Không dependency, không API key.

```powershell
cd 17-ai-engineering-agentic-development/labs/agent-harness-node
npm test
npm run demo
```

## Cấu trúc

- `orchestrator.mjs`: loop và terminal states.
- `tool-registry.mjs`: tool metadata/executor.
- `fake-model.mjs`: model scripted để test deterministic.
- `openai-responses-client.mjs`: adapter tối thiểu, opt-in cho Responses API.
- `test/`: deny, approval, retry và budget tests.

Demo không thực hiện side effect thật. Để thử adapter live, import `OpenAIResponsesModel`, đặt `OPENAI_API_KEY` và `AI_MODEL`; không có model ID mặc định vì catalog thay đổi. Live test phải là opt-in và không thay offline regression.

## Bài tập

1. Thêm JSON Schema validation trước executor.
2. Thêm approval token có action hash và expiry.
3. Persist event/checkpoint rồi resume sau process restart.
4. Thêm idempotency store cho write tool.
5. Export trace sang OpenTelemetry và redact payload.
