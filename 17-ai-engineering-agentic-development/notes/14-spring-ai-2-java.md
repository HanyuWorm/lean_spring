# 14 — Spring AI 2.0 cho Java/Spring developer

Baseline track: **Spring AI 2.0.0**. Spring AI cung cấp abstraction cho model, `ChatClient`, Advisors, tool calling, vector stores/RAG, MCP và observability. Giữ version bằng BOM và kiểm release notes khi nâng.

## Mental mapping

| Spring AI | Vai trò |
|---|---|
| `ChatModel` / Model API | adapter tới provider/model |
| `ChatClient` | fluent facade để tạo request/response |
| Advisor | chèn behavior quanh call: memory/RAG/tool/guardrail |
| `@Tool` / `ToolCallback` | khai báo capability cho model |
| Vector Store API | abstraction lưu/search embedding |
| ETL/RAG modules | document pipeline và retrieval flow |
| MCP support | client/server interoperability |
| Observability | metrics/traces cho model/tool/vector operations |

## Tool calling

`@Tool` thuận tiện cho method rõ schema; `ToolCallback` phù hợp registration động hoặc cần metadata/control thấp hơn. Trong 2.0, hướng khuyến nghị là `ChatClient` với `ToolCallingAdvisor`. Cơ chế model-internal tool execution cũ đã deprecated; lên kế hoạch migrate trước 3.0.

```java
class OrderTools {
    @Tool(description = "Tra cứu trạng thái đơn theo ID; chỉ đọc")
    OrderView findOrder(String orderId) {
        return service.findAuthorized(orderId);
    }
}
```

Annotation không phải security boundary. Method vẫn phải lấy authenticated principal, authorize tenant/order, validate input, timeout và audit. Với catalog lớn, dùng dynamic tool resolution/Tool Search thay vì gửi mọi schema vào mọi request.

## RAG

Advisor-based RAG thuận tiện nhưng pipeline production vẫn cần document version, ACL filter, hybrid/rerank nếu cần và retrieval eval. Vector Store abstraction không làm mọi engine có semantics/performance giống nhau; test filter, consistency và delete behavior của implementation thật.

## Spring architecture khuyến nghị

- Port domain `AiAssistant` không phụ thuộc provider DTO.
- Adapter Spring AI chuyển domain request ↔ `ChatClient`.
- Tool gọi application service, không truy cập repository tùy tiện.
- Policy/approval nằm trước tool executor.
- Config model/prompt/tool version ngoài domain; secret qua secret manager.
- Resilience ở deadline/bulkhead/rate limit; không retry blind write tool.
- Micrometer/Observation redact content mặc định.

## Test pyramid

1. Unit test tool/policy/retriever không gọi model.
2. Contract test schema và provider adapter với fixture/mock.
3. Offline eval trên golden set.
4. Small opt-in live smoke bằng env API key.
5. Production sampled eval/trace đã redact.

Lab [spring-ai-safe-tools](../labs/spring-ai-safe-tools/README.md) minh họa `@Tool` và policy wrapper chạy không cần key.
