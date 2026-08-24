# 07 — MCP và A2A interoperability

## MCP giải quyết gì?

Model Context Protocol chuẩn hóa cách AI application kết nối capability/data provider. Revision hiện hành trong track này là **2026-07-28** và chuyển protocol core sang stateless, self-describing request.

Điểm đáng chú ý của revision này:

- Không còn bắt buộc handshake `initialize/initialized` hay `Mcp-Session-Id` trong core.
- Có discovery và metadata/header giúp request tự mô tả.
- Hỗ trợ multi round-trip requests, cacheable list results và extension framework.
- Tasks là extension cho công việc kéo dài.
- Auth được siết; Dynamic Client Registration bị thay thế theo hướng Client ID Metadata Documents.
- Legacy HTTP+SSE và một số capability cũ có lộ trình deprecation; kiểm tra migration guide trước nâng cấp.

Đừng thiết kế server mới dựa trên giả định MCP luôn giữ session state. Business workflow state vẫn phải nằm trong datastore/checkpoint của application.

## MCP security boundary

- Tool description, resource content và server response đều là untrusted input.
- Server phải authorize caller và từng action; model không phải principal đáng tin.
- Client hiển thị rõ tool/action, scope và side effect trước approval.
- Không truyền credential vào prompt; dùng token audience/scope hẹp và secret store.
- Chống confused deputy, SSRF, path traversal, data exfiltration và tool-name collision.
- Pin/trust server, audit call và giới hạn egress.

MCP làm integration chuẩn hơn, không tự giải quyết authorization, consent, tenancy hay reliability.

## A2A giải quyết gì?

A2A specification được publish hiện tại là **0.3.0**. Nó nhắm tới giao tiếp giữa các agent system độc lập/opaque: discovery, capability, message/artifact và lifecycle của task. Agent không cần lộ internal prompt, memory hoặc tool graph.

| Câu hỏi | MCP | A2A |
|---|---|---|
| Kết nối chính | AI app ↔ tool/data server | agent system ↔ agent system |
| Abstraction | capability/resource/tool | task/message/artifact/agent capability |
| State | core mới thiên stateless | task có lifecycle |
| Có thể phối hợp | Có | Có; không loại trừ MCP |

Roadmap A2A nói về 1.0 nhưng không được ghi như bản đã phát hành. Version-pin schema và contract test khi interoperability là yêu cầu thật.

## Decision guide

- Internal function trong một service: function/tool calling bình thường.
- Nhiều client AI cần dùng cùng capability: cân nhắc MCP server.
- Hai tổ chức/agent platform giao việc và theo dõi task: cân nhắc A2A.
- Không thêm protocol chỉ để “modern”; deployment, auth, schema evolution và observability là chi phí thật.

Nguồn phiên bản và migration được ghi tại [SOURCES.md](../SOURCES.md).
