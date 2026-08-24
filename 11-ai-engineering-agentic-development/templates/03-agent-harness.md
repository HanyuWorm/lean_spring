# Agent harness: <tên>

## Goal và autonomy boundary

- Agent may:
- Agent must not:
- Must ask human when:

## State machine

- States/transitions:
- Durable checkpoints:
- Terminal stop reasons:

## Tools

| Tool + version | Input/output schema | Side effect | Authz | Approval | Idempotency |
|---|---|---:|---|---:|---|

## Policy

- Tool allowlist theo role/task/tenant:
- Semantic validation/business invariants:
- Egress/path/URL restrictions:
- Untrusted content handling:

## Budgets

- Max steps / tool calls / retries:
- Token / cost / wall-clock deadline:
- Parallel fan-out:

## Errors và recovery

- Transient/retryable:
- Permanent/deny:
- Compensate/handoff:

## Trace và audit

- Events/spans:
- Redaction/retention:
- Versions cần capture:

## Harness tests

- Unauthorized tool, invalid args, approval deny, duplicate/retry, loop, timeout, budget exhausted, injected tool result.
