# Spring AI 2.0 Safe Tools

Project Java 21 minh họa Spring AI `@Tool` nhưng giữ authorization trong application code. Không Spring context, model provider, API key hay network call; test chạy deterministic.

```powershell
mvn -f 17-ai-engineering-agentic-development/labs/spring-ai-safe-tools/pom.xml test
```

## Luồng

`model proposal → Spring AI tool binding → OrderLookupTools → input validation → ToolPolicy → OrderService`

- Tool chỉ read-only và nhận actor từ trusted request context, không nhận `tenantId` do model tự khai.
- Policy kiểm role và tenant ownership.
- Error không làm rò order khác tenant.
- Annotation/schema là integration contract; policy mới là security boundary.

Trong Spring Boot thật, expose tool qua `ChatClient` + `ToolCallingAdvisor`, map authenticated principal vào request-scoped actor provider, thêm Observation/redaction và không register write tools mặc định. Với write tool, thêm approval + idempotency + audit trước service call.

## Bài tập

1. Tạo `request_refund` thành proposal trước, chỉ execute với approval token.
2. Thêm optimistic/idempotent persistence bằng H2.
3. Dùng `ToolCallback` để resolve tool động theo role.
4. Thêm Spring AI `ChatClient` integration test với stub `ChatModel`.
