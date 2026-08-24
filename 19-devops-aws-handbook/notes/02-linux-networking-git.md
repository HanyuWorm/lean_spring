# 02 — Linux, networking và Git cho DevOps

## 1. Linux mental model

### Process và signal

- Process có PID, parent, user/group, environment, file descriptors và address space.
- `SIGTERM` yêu cầu shutdown có kiểm soát; `SIGKILL` dừng ngay và không cho cleanup.
- Container orchestrator thường gửi `SIGTERM`, chờ grace period rồi mới `SIGKILL`.
- PID 1 cần forward/reap signal đúng; ứng dụng phải ngừng nhận traffic trước khi đóng dependency.

Lệnh điều tra điển hình:

```bash
ps aux --sort=-%cpu
top
free -h
df -h
df -i
du -xh /var | sort -h | tail
lsof -p <pid>
ss -lntp
journalctl -u <service> --since "30 min ago"
```

Không copy lệnh vào production mà chưa hiểu scope và cost. `du` trên filesystem lớn hay packet capture không filter có thể tự tạo thêm tải.

### Memory, disk và file descriptor

- RSS khác Java heap; còn native memory, mapped files, thread stacks và page cache.
- Disk còn dung lượng nhưng hết inode vẫn không tạo được file.
- File đã xóa nhưng process còn mở vẫn giữ disk; kiểm tra `lsof +L1`.
- `ulimit -n` thấp gây `Too many open files`; phải tìm leak/connection lifecycle trước khi chỉ tăng limit.
- Load average cao có thể do runnable CPU hoặc task chờ uninterruptible I/O.

### Permission

- Phân biệt owner/group/other và read/write/execute.
- Directory cần execute để traverse.
- Tránh `chmod 777`; cấp đúng user/group hoặc ACL.
- Service account không login shell, không sudo rộng và không sở hữu secret không cần thiết.

## 2. Network mental model

```text
URL
 -> DNS lookup
 -> route/NAT/firewall
 -> TCP handshake
 -> TLS handshake + certificate/SNI
 -> HTTP request
 -> proxy/load balancer
 -> application
 -> downstream/database
```

Chẩn đoán theo layer, không kết luận “network lỗi”:

| Triệu chứng | Kiểm tra đầu tiên |
|---|---|
| Name not resolved | DNS record, resolver, search domain, TTL |
| Connection refused | route tới host được nhưng không có listener hoặc reject |
| Connection timeout | route, security rule, firewall, NACL, listener saturation |
| TLS error | hostname/SNI, chain, expiry, trust store, protocol/cipher |
| HTTP 502 | proxy không nhận response hợp lệ từ upstream |
| HTTP 503 | upstream unavailable, no healthy target hoặc load shedding |
| HTTP 504 | proxy timeout chờ upstream; chưa chắc root cause nằm ở proxy |

### Timeout budget

Timeout ngoài nên lớn hơn timeout trong đủ để tầng trong fail có kiểm soát và trả lỗi. Ví dụ:

```text
client deadline 3s
  gateway 2.8s
    service outbound 1.0s x tối đa 2 attempts
      DB acquire + query nằm trong remaining budget
```

Không cấu hình retry ở mọi tầng. Ba tầng, mỗi tầng ba attempts có thể khuếch đại thành 27 call.

## 3. Git dành cho delivery

- Commit nhỏ, có ý nghĩa và build được giúp rollback/bisect.
- Protected branch yêu cầu review và status checks.
- Trunk-based development giảm merge batch; branch ngắn, feature chưa release nằm sau flag.
- Tag/version trỏ tới commit; artifact metadata chứa commit SHA.
- Không commit secret, binary build, Terraform state hay `.env` production.

### Revert khác reset

- `git revert` tạo commit đảo thay đổi, phù hợp lịch sử đã share.
- `git reset` di chuyển ref và có thể rewrite history; không dùng tùy tiện trên shared branch.
- Revert application code không tự reverse schema migration hoặc data đã biến đổi.

## 4. Runbook: service không trả lời

1. Xác nhận user impact và scope: một instance, AZ, region hay mọi tenant.
2. Kiểm tra DNS/TLS/LB target health trước khi SSH vào host ngẫu nhiên.
3. So sánh traffic, error, latency, saturation với deployment/config change gần nhất.
4. Kiểm tra process, port, CPU, memory, disk, FD và dependency.
5. Mitigate bằng rollback, traffic shift, scale hoặc feature disable theo stop condition.
6. Lưu timeline/evidence; không “dọn sạch” log trước post-incident analysis.

## 5. Bài tập

- Tái hiện DNS sai, port đóng, certificate hostname mismatch và upstream timeout.
- Viết script health check trả exit code khác nhau cho DNS/TCP/TLS/HTTP.
- Dùng `git bisect` tìm commit làm test fail.
- Giải thích vì sao ping thành công không chứng minh HTTPS/application/database hoạt động.

