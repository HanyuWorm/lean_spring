# 01 — Security foundations

## Security là quản trị rủi ro

`Risk = likelihood × impact` chỉ là mô hình định tính ban đầu. Kiến trúc sư phải nêu asset, threat actor, attack path, existing control, residual risk và business owner. Bốn treatment: mitigate, avoid, transfer, accept. “Accept” phải có người đủ thẩm quyền, thời hạn/review trigger, không phải backlog bị quên.

## CIA và các property bổ sung

- **Confidentiality:** chỉ principal được phép mới đọc.
- **Integrity:** dữ liệu/code/config không bị sửa trái phép; phát hiện được tampering.
- **Availability:** chức năng/dữ liệu sẵn theo SLO, kể cả khi bị tấn công.
- **Authenticity:** danh tính/nguồn là thật.
- **Accountability/non-repudiation:** hành động truy vết tới principal với log đáng tin.
- **Privacy:** xử lý dữ liệu cá nhân đúng purpose, minimization, consent/retention.

Security và reliability giao nhau ở DDoS, ransomware, backup, dependency compromise và operator error.

## NIST CSF 2.0

Sáu function chạy đồng thời:

1. **Govern:** context, risk strategy, policy, roles, supply-chain risk, oversight.
2. **Identify:** asset, dependency, data flow, vulnerability và improvement.
3. **Protect:** identity/access, awareness, data/platform security, resilience.
4. **Detect:** continuous monitoring và adverse-event analysis.
5. **Respond:** incident management, analysis, communication, mitigation.
6. **Recover:** restore, validate, communicate và improve.

Control prevention mà không có Detect/Respond/Recover là kiến trúc chưa hoàn chỉnh.

## Secure by design/default

- Security requirement xuất hiện cùng functional design.
- Default deny, least privilege, safe configuration và secure failure.
- Loại whole vulnerability class bằng framework/platform guardrail.
- Giảm management scope bằng managed/centralized control khi trade-off hợp lý.
- Không bắt user tự gánh security mặc định yếu.
- Memory-safe components, parameterized APIs và strong types giảm lỗi có hệ thống.

## Defense in depth

Các layer phải độc lập tương đối: identity → edge → network → workload → application authorization → data/key → detection/recovery. Nhiều tool cùng phụ thuộc một credential/admin plane không tạo defense-in-depth thật.

## Security requirements mẫu

```text
SR-01: Mọi thao tác refund phải xác thực phishing-resistant cho operator,
       authorize theo merchant + amount, yêu cầu step-up trên 50M VND,
       và ghi immutable audit event có actor, reason, before/after.

SR-02: Rò rỉ một application credential không cho phép decrypt backup;
       quyền data và key tách role, có alert khi decrypt bất thường.
```

Requirement phải test được. “Dùng encryption”, “tuân thủ OWASP” chưa đủ.
