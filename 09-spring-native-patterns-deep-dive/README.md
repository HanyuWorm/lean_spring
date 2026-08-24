# Spring-native Patterns Deep Dive

Reactor này tách tám pattern thành tám project nhỏ để mỗi pattern có boundary và test riêng. Mục tiêu là thấy semantics thật của Spring thay vì chỉ nhìn class diagram.

## Thứ tự học

| Project | Pattern | Điều phải chứng minh bằng test |
|---|---|---|
| [`01-dependency-injection`](01-dependency-injection/README.md) | DI / IoC | collection injection, explicit registry, fail-fast khi thiếu channel |
| [`02-proxy-aop`](02-proxy-aop/README.md) | Proxy / AOP | external proxy call có transaction, self-invocation bypass proxy, rollback thật |
| [`03-strategy`](03-strategy/README.md) | Strategy | strategy registry, policy selection, duplicate/missing key |
| [`04-factory`](04-factory/README.md) | Factory | domain factory giữ invariant và Spring `FactoryBean` tạo product bean |
| [`05-template-callback`](05-template-callback/README.md) | Template / Callback | template sở hữu transaction lifecycle và rollback khi callback fail |
| [`06-chain-of-responsibility`](06-chain-of-responsibility/README.md) | Chain of Responsibility | deterministic order, short-circuit và side-effect boundary |
| [`07-observer-domain-events`](07-observer-domain-events/README.md) | Observer / Domain Event | listener thường chạy sync; transactional listener chỉ chạy sau commit |
| [`08-adapter-decorator`](08-adapter-decorator/README.md) | Adapter / Decorator | external model dừng ở adapter; cache/metrics bọc port có thứ tự rõ |

## Build

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Chạy riêng reactor này
mvn -f 09-spring-native-patterns-deep-dive/pom.xml test

# Chạy một project
mvn -f 09-spring-native-patterns-deep-dive/pom.xml -pl 02-proxy-aop test

# Chạy toàn workspace
mvn test
```

## Cách học mỗi project

1. Đọc README trước và tự trả lời bốn câu: force, dependency, failure mode, test.
2. Đọc test để hiểu behavior contract.
3. Chạy test rồi đặt breakpoint tại proxy/callback/listener/decorator boundary.
4. Thực hiện bài mở rộng cuối README.
5. Viết một ADR ngắn: khi nào dùng và khi nào giữ thiết kế đơn giản hơn.

## Nguyên tắc chung

- Pattern là quyết định có consequence, không phải mục tiêu tăng số class.
- Spring container chỉ compose object graph; business invariant vẫn thuộc domain/application.
- Test proxy, transaction và event phase phải qua Spring context thật.
- Test strategy, factory, chain và decorator ưu tiên pure unit test nếu không cần container semantics.
- Mọi abstraction phải biểu diễn boundary hoặc variation point; không tạo interface cho mọi class.
