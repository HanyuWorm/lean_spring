# 11 — Safety và security

AI security là security của một hệ thống xử lý input không tin cậy và có thể gọi tool. Prompt không phải policy engine.

## Trust boundaries

- User input, web/document retrieved, tool metadata/result và memory đều untrusted.
- Model output là proposal, không phải authorization hay validated fact.
- Tool executor là điểm enforce identity, tenant, scope, schema và business invariant.
- External write/destructive/costly action cần approval tương xứng rủi ro.

## Các mối đe dọa chính

1. **Direct/indirect prompt injection:** nội dung bảo model bỏ instruction hoặc gọi tool.
2. **Data exfiltration:** ghép dữ liệu nhạy cảm với kênh egress như URL/email/tool.
3. **Excessive agency:** agent có tool/scope/time quá rộng.
4. **Tool misuse/confused deputy:** model lợi dụng quyền của service thay người dùng.
5. **Memory/RAG poisoning:** dữ liệu độc được lưu hoặc xếp hạng cao.
6. **Supply chain:** MCP server, model, package, prompt/template bị thay đổi.
7. **Denial of wallet/service:** loop, context phình, fan-out hoặc tool tốn phí.
8. **Insecure output handling:** model tạo SQL/shell/HTML được execute không validate.

## Control matrix

| Layer | Controls |
|---|---|
| Identity | short-lived token, audience/scope, delegated identity |
| Context | ACL retrieval, classification, provenance, minimize secrets |
| Model | instruction hierarchy, structured output, deny unsafe requests |
| Harness | allowlist, budget, approval, stop, policy decision |
| Tool | server-side authz, validation, idempotency, least privilege |
| Network | egress allowlist, SSRF guard, DNS/IP validation |
| Data | encryption, retention, deletion, DLP/redaction |
| Operations | audit, anomaly alerts, kill switch, incident playbook |

## Prompt injection defense in depth

Không có magic prompt. Tách instruction khỏi data bằng message/field rõ; gắn provenance; không cho retrieved text thay đổi policy; giới hạn tool theo task; validate URL/path/query; require approval; và test adversarial corpus. Với high-risk action, dùng deterministic policy/business code thay vì hỏi model “có an toàn không?”.

## Threat-model questions

- Agent có thể đọc dữ liệu nào và gửi ra đâu?
- Nếu document độc gọi tool thì control nào chặn?
- Ai là principal của tool call: user, service hay agent?
- Retry có tạo giao dịch trùng không?
- Có kill switch theo tool/model/tenant không?
- Log có vô tình lưu secret/PII không?
- Có thể replay incident với version/evidence nào?

Áp dụng NIST AI RMF GenAI Profile cho governance/risk lifecycle và OWASP Top 10 for Agentic Applications cho abuse cases kỹ thuật. Template [06-ai-security.md](../templates/06-ai-security.md) dùng cho review.
