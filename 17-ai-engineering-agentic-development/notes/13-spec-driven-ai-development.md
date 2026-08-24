# 13 — Spec-driven AI development

Spec-driven không có nghĩa viết tài liệu lớn trước mọi thay đổi. Nó biến ý định thành contract nhỏ, versioned và có thể verify để coding agent không tự lấp chỗ trống nguy hiểm.

## Workflow

1. **Discover:** đọc code/docs/test thật, liệt kê unknown và constraint.
2. **Specify:** outcome, invariants, scope, interface, NFR, acceptance.
3. **Architect:** boundary, decision/ADR, data flow, failure và migration.
4. **Plan:** task atomic có file scope, dependency và verification command.
5. **Implement:** context packet tối thiểu; agent sửa trong scope.
6. **Verify:** tests, static checks, security/eval, diff review.
7. **Record:** evidence, deviations, ADR/changelog và follow-up.

## Task packet tốt

```text
Goal: thêm idempotency cho POST /payments
In scope: PaymentController, PaymentService, migration, tests
Constraints: không đổi response success; key unique theo merchant
Acceptance: concurrent duplicate chỉ tạo một payment
Verify: mvn -pl payments test
Stop/ask: cần thay public API hoặc migration destructive
```

Packet trỏ tới artifact canonical thay vì paste toàn repository. Nêu file/module được sửa, command kiểm chứng, forbidden changes và khi nào phải dừng.

## Dùng coding agent an toàn

- Bắt đầu read-only discovery và yêu cầu evidence từ code.
- Cho quyền theo task; secret/prod/external write không mặc định.
- Chia change nhỏ, inspect diff, chạy test thực và giữ user change ngoài scope.
- Không chấp nhận test bị xóa/nới lỏng chỉ để xanh.
- Generated code cần cùng review, SAST/dependency/license rules như code người viết.
- Dùng fresh/scoped session khi context cũ gây nhiễu; không coi “new chat” là requirement.

## Spec không bất biến

Unknown là bình thường. Khi implementation phát hiện assumption sai, dừng, cập nhật spec/ADR/test rồi tiếp tục. Traceability nên là `requirement → task → diff → test/eval evidence`, không phải lời hứa “khớp 100%”.

Sao chép bộ [templates](../templates/README.md) vào capstone và điền theo mức rủi ro; không cần làm tài liệu nặng cho change nhỏ.

## Playbook dùng AI hằng ngày

### Khám phá codebase

Yêu cầu agent tìm entry point, dependency direction, data flow và test liên quan; mọi kết luận phải kèm file/symbol. Chưa cho sửa. Sau đó tự kiểm bằng search/build output, không tin README cũ tuyệt đối.

### Implement feature

Đưa task packet, yêu cầu plan ngắn rồi sửa trong scope. Chạy formatter/static check/unit/integration test phù hợp; cuối cùng review diff theo acceptance và báo deviation. Với thay đổi public API/data migration, bắt buộc checkpoint người dùng.

### Debug incident

Đưa symptom, timeline, logs/metrics đã redact và recent changes. Yêu cầu lập hypothesis table gồm evidence thuận/nghịch và test phân biệt; chạy read-only diagnostics trước. Không cho agent “fix thử” production khi chưa xác định blast radius/rollback.

### Test và review

Cho reviewer spec + diff, không cho bản giải thích của author làm nguồn chính. Tìm correctness, concurrency, security, backward compatibility và missing tests; mỗi finding cần đường dẫn/dòng và scenario tái hiện. Test generator phải bổ sung boundary/property/failure tests, không sửa expected behavior để pass.

### Refactor hoặc migration

Đầu tiên khóa behavior bằng characterization/contract tests. Chia migration thành bước deploy-compatible, theo dõi dual-read/write nếu có và có rollback. Dùng agent cho mechanical edits nhưng đo diff, compile/test mỗi batch.

### Prompt khởi đầu dùng được

```text
Mục tiêu: <outcome>. Hãy bắt đầu bằng read-only discovery.
Scope: <modules/files>. Không thay đổi ngoài scope.
Constraints/invariants: <list>.
Acceptance: <assertions>. Verify bằng: <commands>.
Mỗi claim về code phải kèm file/symbol. Giữ thay đổi người dùng không liên quan.
Dừng và hỏi nếu cần đổi public contract, destructive migration, secret/prod/external write
hoặc evidence phủ định assumption chính. Cuối cùng báo diff, test/eval evidence và deviation.
```
