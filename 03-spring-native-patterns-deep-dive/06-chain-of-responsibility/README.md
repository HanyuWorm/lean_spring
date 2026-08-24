# 06 - Chain of Responsibility

Chain tổ chức nhiều handler/rule theo thứ tự và cho phép dừng sớm mà caller không biết concrete handlers. Spring Security filter chain, servlet filters, MVC interceptors và validation pipelines đều dùng biến thể của pattern này.

```text
request
  -> CustomerActiveRule (10)
  -> PositiveAmountRule (20)
  -> RiskScoreRule (30)
  -> accepted
```

Spring inject `List<OrderRule>` theo `@Order`. `OrderValidationChain` copy thành immutable list, chạy tuần tự và trả ngay failure đầu tiên.

## Decisions phải explicit

- Order là business order hay technical order?
- Fail-fast hay collect all violations?
- Handler có quyền mutate request/context không?
- Handler có side effect không; nếu handler sau fail thì side effect trước xử lý thế nào?
- Exception dừng chain, map thành rejection hay retry?
- Rule optional theo tenant/feature flag được snapshot thế nào?

Demo dùng pure validation, fail-fast và không side effect. Đây là cấu hình dễ reason nhất.

## Force, boundary, failure mode

- Force: rules cần thêm/bớt/reorder độc lập nhưng orchestration phải nhất quán.
- Boundary: chain sở hữu traversal/short-circuit; rule sở hữu một decision nhỏ.
- Failure mới: order ẩn, rule chạy hai lần, side effect trước failure, mutation context và catch-all che exception.
- Test: assert exact visited order, short-circuit, exception propagation và side-effect count nếu có.

## Chain khác Strategy

Strategy chọn một policy để xử lý toàn operation. Chain cho nhiều handlers cơ hội xử lý/biến đổi/dừng một request. Nếu chỉ có một matching handler thì registry/strategy thường rõ hơn.

## `@Order` caution

Magic numbers rải rác khó maintain. Với business pipeline quan trọng, có thể compose list explicit trong `@Configuration` hoặc dùng phase enum/dependency graph. Test order là contract bắt buộc.

## Bài mở rộng

1. Chuyển sang collect-all validation và so sánh UX/performance.
2. Thêm rule gọi fraud service; đặt timeout và quyết định technical failure khác business rejection ra sao.
3. Thêm side-effect rule rồi thiết kế transaction/compensation; sau đó refactor side effect ra khỏi validation chain.

