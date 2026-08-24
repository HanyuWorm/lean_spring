# 05 - Template Method / Callback

Spring template APIs đảo quyền điều khiển: framework/template giữ phần lifecycle khó và callback chỉ cung cấp phần logic biến đổi.

```text
OrderRegistrationTemplate.register
    -> TransactionTemplate opens transaction
       -> JdbcTemplate acquires connection, executes SQL, translates exception
       -> AfterOrderWrite callback runs
    -> commit or rollback
```

## Hai biến thể

- Template Method cổ điển dùng inheritance: base class định nghĩa skeleton, subclass override steps.
- Callback/composition dùng function/object được truyền vào. Spring thường ưu tiên cách này vì tránh fragile base class và cho phép lambda.

`JdbcTemplate`, `TransactionTemplate`, `RedisTemplate` và nhiều execute APIs dùng cùng tư tưởng: “framework owns lifecycle, caller owns operation”.

## Ai sở hữu điều gì?

Trong demo:

- Template validate input, mở/đóng transaction, insert order và quyết định rollback.
- Callback thực thi extension step nhưng không commit, close connection hoặc đổi transaction policy.
- Exception callback được propagate để transaction rollback.

Nếu callback spawn thread hoặc giữ resource reference sau khi scope kết thúc, template không còn bảo đảm lifecycle.

## Force, boundary, failure mode

- Force: resource/transaction cleanup phải đồng nhất nhưng operation thay đổi.
- Boundary: template sở hữu invariant lifecycle; callback chỉ chạy trong lexical/dynamic scope đó.
- Failure mới: callback side effect không transactional, callback giữ lock quá lâu, swallow exception, nested template/transaction semantics khó hiểu.
- Test: callback chạy trong transaction; failure rollback DB; resource/exception behavior ở cả hai path.

## So với `@Transactional`

`@Transactional` declarative và phù hợp public use-case boundary. `TransactionTemplate` phù hợp khi cần transaction scope nhỏ, conditional hoặc nhiều phase trong cùng method mà không muốn self-invocation/proxy trick. Programmatic không có nghĩa business code nên quản lý JDBC connection thủ công.

## Cảnh báo side effect

Nếu callback gửi HTTP/email, rollback database không thu hồi side effect. Nếu publish broker message, đây vẫn là dual write. Dùng Outbox khi cần atomic intent giữa DB mutation và integration event.

## Bài mở rộng

1. Callback ghi audit row trong cùng DB và chứng minh cùng rollback.
2. Callback gọi fake HTTP rồi fail DB; giải thích inconsistency và chuyển sang Outbox.
3. Thêm timeout/read-only/isolation bằng `TransactionTemplate` configuration và đo lock time.

