# 16 — 60 câu hỏi AI Engineering có đáp án

## Nền tảng và API

1. **Token là gì?** Đơn vị model đọc/sinh, không trùng từ/ký tự; ảnh/audio có cách tính riêng theo API.
2. **Context window gồm gì?** Instructions, history/state được gửi, tool schemas, retrieved data, tool results và output budget.
3. **Temperature thấp có hết hallucination?** Không; chỉ giảm ngẫu nhiên, không thêm evidence hay correctness.
4. **Reasoning model khác gì?** Có khả năng/compute cho suy luận nhiều bước; vẫn cần tools, evidence và eval.
5. **Streaming có giảm compute?** Không nhất thiết; chủ yếu giảm time-to-first-visible-output.
6. **Embedding là gì?** Vector biểu diễn để so độ gần ngữ nghĩa; không phải database hay fact checker.
7. **Structured output bảo đảm gì?** Hình dạng/schema nếu API hỗ trợ; không bảo đảm giá trị đúng nghiệp vụ.
8. **Chọn model ra sao?** Theo capability, quality eval, latency, cost, limits, privacy và availability trên workload thật.
9. **Có nên hard-code latest model?** Không; config model, pin khi cần ổn định và chạy regression trước đổi.
10. **Multimodal rủi ro gì?** OCR/grounding sai, payload lớn, PII và injection trong ảnh/tài liệu.

## Prompt và context

11. **Prompt engineering khác context engineering?** Prompt định nghĩa instruction/contract; context engineering chọn, tổ chức và kiểm soát toàn input/state/tools.
12. **System prompt có phải security boundary?** Không; authorization và validation phải ở code/tool executor.
13. **Few-shot dùng khi nào?** Khi format/decision boundary khó mô tả; examples phải đại diện và được eval.
14. **Tại sao context càng dài không càng tốt?** Tăng cost/latency, nhiễu, injection surface và có thể giảm attention vào evidence.
15. **Context caching là gì?** Tái sử dụng compute cho prefix ổn định; không thay freshness/ACL.
16. **Prompt version cần gì?** ID/version, owner, change reason, model/tool compatibility và eval evidence.
17. **Khi model thiếu dữ liệu?** Contract cho phép abstain/ask clarification hoặc retrieve; không ép luôn trả lời.
18. **Có nên yêu cầu chain-of-thought?** Không cần; yêu cầu answer/evidence/check có thể audit, không phụ thuộc reasoning nội bộ.
19. **Context packet cho coding agent gồm gì?** Goal, scope, constraints, relevant files/contracts, acceptance, verify command và stop conditions.
20. **Fresh chat có bắt buộc?** Không; mục tiêu là context scope sạch và canonical artifacts.

## Tool và agent

21. **Function calling hoạt động thế nào?** App khai báo tools; model đề xuất name/args; app validate/execute; result quay lại model.
22. **Ai chịu trách nhiệm authorize tool?** Application/tool server, không phải model.
23. **Idempotency cần khi nào?** Mọi side effect có thể retry/replay như payment, email hoặc ticket creation.
24. **Agent khác workflow?** Agent tự chọn bước/tool trong boundary; workflow có control flow chủ yếu do code định trước.
25. **Harness là gì?** Runtime bao model để quản lý context, tools, policy, state, budgets, retries, approvals, trace và stop.
26. **Plan–Act–Reflect có đủ production?** Không; cần authorization, durable state, errors, budgets, eval và observability.
27. **Multi-agent khi nào đáng dùng?** Workstream độc lập, parallelism có lợi, context riêng và có merge/eval rõ.
28. **Multi-agent có giảm hallucination?** Không mặc định; có thể nhân lỗi và coordination cost.
29. **Stop condition nào cần có?** Completed, human required, denied, max steps/tools/tokens/cost, timeout và repeated invalid loop.
30. **Approval đặt ở đâu?** Sau validated proposal nhưng trước side effect; approval phải hiển thị action/target/scope.

## RAG và memory

31. **RAG giải quyết gì?** Cung cấp evidence cập nhật/private vào inference và cho phép citation; không tự bảo đảm truth.
32. **Dense và lexical search khác gì?** Dense theo semantic; lexical mạnh ở exact keyword/ID; hybrid kết hợp hai loại.
33. **Reranker để làm gì?** Chấm lại candidate set bằng model mạnh hơn nhằm cải thiện top context.
34. **Chunk size tốt nhất?** Không có số chung; chọn theo cấu trúc tài liệu và đo retrieval/answer eval.
35. **Recall@k là gì?** Tỷ lệ relevant items được tìm thấy trong top-k.
36. **MRR là gì?** Trung bình reciprocal rank của relevant result đầu tiên; thưởng xếp đúng nguồn lên cao.
37. **RAG trả sai thì debug thế nào?** Tách corpus/parse/index/retrieve/rerank/context/generation để tìm stage lỗi.
38. **ACL enforce ở prompt được không?** Không; enforce ở retrieval/query/tool layer trước khi data vào context.
39. **Memory khác history?** Memory là state chọn lọc/có semantics và lifecycle; history chỉ chuỗi message/event.
40. **Lưu memory an toàn thế nào?** Provenance, validation, tenant ACL, retention/deletion, consent và chống poisoning.

## Eval, production và security

41. **Golden dataset gồm gì?** Representative happy/edge/adversarial/no-answer cases với expected properties và forbidden behavior.
42. **Model-as-judge có đáng tin?** Hữu ích cho semantic rubric nhưng phải calibrate với human/code grader và version rõ.
43. **Tại sao unit test chưa đủ?** Model/retrieval behavior probabilistic và phụ thuộc provider/corpus; cần task/trajectory/E2E eval.
44. **Release gate AI nên có gì?** Critical safety pass, slice non-regression, quality threshold, p95 latency/cost và rollback.
45. **Metric quan trọng nhất?** Cost/latency/quality trên successful user outcome, cộng hard safety invariants.
46. **Trace một agent run gồm gì?** Context/retrieval, model/tool proposals, policy/approval, tool result, retries, versions và stop reason.
47. **Có log raw prompt không?** Default không; redact/minimize, access/retention nghiêm và debug sampling có kiểm soát.
48. **Prompt injection là gì?** Untrusted content cố thay instruction hoặc kích hoạt hành động ngoài ý định.
49. **Cách chặn injection tốt nhất?** Defense in depth: isolate data, least privilege, allowlist, authz, validation, approval, egress control và tests.
50. **Excessive agency là gì?** Agent có capability/scope/time/quyền quá mức cần thiết.

## Interoperability và Java

51. **MCP dùng để làm gì?** Chuẩn hóa kết nối AI app với tool/data capability server.
52. **MCP 2026-07-28 thay đổi lớn gì?** Stateless self-describing core, discovery/cache/MRTR/extensions và auth/deprecation updates.
53. **MCP có thay REST không?** Không; có thể expose AI-oriented capability trên hệ thống dùng HTTP/API hiện hữu.
54. **A2A khác MCP?** A2A giao task/message/artifact giữa agent systems; MCP nối app với capability/data server.
55. **A2A 1.0 đã là published baseline chưa?** Chưa theo baseline 24/08/2026; published spec được track là 0.3.0, 1.0 nằm roadmap.
56. **Spring AI cung cấp gì?** Model abstraction, ChatClient, Advisors, tools, vector/RAG, MCP và observability.
57. **`@Tool` có tự authorize không?** Không; method/application service vẫn phải authz, validate, timeout và audit.
58. **Tại sao dùng port quanh Spring AI?** Giữ domain độc lập provider/framework và dễ mock/eval/migrate.
59. **Test Spring AI không API key thế nào?** Unit test tool/policy/retriever và fake/stub model; live smoke là opt-in.
60. **Capstone tốt cho senior Java là gì?** Grounded support agent có Spring AI, ACL RAG, read-only tools, eval/trace; sau đó thêm write tool với approval/idempotency.
