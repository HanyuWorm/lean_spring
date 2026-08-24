# 12 — Extensibility, configuration và versioning

## Trục mở rộng thật

Extension point hợp lý khi variation đã biết, độc lập và có contract/test. Ví dụ notification channel, pricing policy. Không thiết kế plugin engine cho mọi class.

Mỗi extension point cần:

- Stable input/output/error semantics.
- Registration/selection và duplicate handling.
- Capability/version compatibility.
- Ordering/composition rule.
- Isolation/timeout/security nếu code bên thứ ba.
- Contract test suite.

## Registry

```java
Map<ChannelType, NotificationChannel> channels
```

Fail fast nếu missing/duplicate key khi startup. Không silently last-wins theo bean order. Dynamic registry cần atomic snapshot/version và rollback config.

## Configuration vs code

Config cho threshold/routing/feature toggle có validation, default, owner, secret separation và reload semantics. Business workflow phức tạp nhét vào YAML tạo programming language kém tooling. Rule đổi thường nhưng critical có thể cần decision table/rules engine, nhưng chỉ khi governance/audit authoring cần.

## Backward compatibility

- Add field optional trước, consumer tolerate unknown.
- Expand → migrate → contract.
- Enum mới có thể làm consumer `switch` fail; define unknown behavior.
- Method/interface public thêm abstract method phá implementers; default method cũng cần semantic review.
- Serialized class/event cần schema/version/migration test.

## Feature flags

Flag ở decision boundary, không rải `if(flag)` khắp domain. Có owner, expiry, default khi service flag lỗi và metric theo variant. Hai path cùng tồn tại tăng state space/test burden.

## Rules và policy composition

Khi rules tăng:

- Strategy cho một policy được chọn.
- Chain cho ordered processing.
- Specification/Composite cho boolean tree.
- Decision table cho nhiều combination.
- State machine cho transition theo lifecycle.

Chọn representation theo cách business thay đổi và cần audit, không theo framework phổ biến.

## Compatibility test

- Old producer → new consumer và ngược lại trong migration window.
- Unknown fields/enum/version.
- Duplicate/out-of-order event.
- Config missing/invalid/partial rollout.
- Plugin failure/timeout.
