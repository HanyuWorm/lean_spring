# Repository Guide

Guide này chỉ ra nơi nên đọc và điều phải phản biện. Commit được cố định theo lần clone ngày 2026-08-24; các clone dùng `--depth 1`.

## 1. FTGO Application

- Upstream/commit: `microservices-patterns/ftgo-application` — `558dfc53b11d`
- Stack: Spring Boot 2.2.6, Java 8 target, Gradle 6.9.1, Eventuate, MySQL/Kafka/DynamoDB local.
- Bắt đầu tại [CreateOrderSaga.java](repositories/ftgo-application/ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/sagas/createorder/CreateOrderSaga.java): forward steps, participant command/reply và compensation.
- So sánh [CancelOrderSaga.java](repositories/ftgo-application/ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/sagas/cancelorder/CancelOrderSaga.java) với [ReviseOrderSaga.java](repositories/ftgo-application/ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/sagas/reviseorder/ReviseOrderSaga.java).
- Xem [OrderService.java](repositories/ftgo-application/ftgo-order-service/src/main/java/net/chrisrichardson/ftgo/orderservice/domain/OrderService.java) để nối local transaction/domain event với Saga.
- CQRS projection nằm ở [OrderHistoryEventHandlers.java](repositories/ftgo-application/ftgo-order-history-service/src/main/java/net/chrisrichardson/ftgo/cqrs/orderhistory/messaging/OrderHistoryEventHandlers.java).
- Test đáng đọc: [CreateOrderSagaTest.java](repositories/ftgo-application/ftgo-order-service/src/test/java/net/chrisrichardson/ftgo/orderservice/sagas/createorder/CreateOrderSagaTest.java).

Điểm học: Saga là persistent state machine, không phải chuỗi `try/catch` REST. Mỗi participant phải idempotent; compensation là nghiệp vụ ngược có thể thất bại/retry, không phải rollback ACID toàn cục.

Điểm phản biện: version cũ nên học pattern, không copy dependency/config production. Full demo phụ thuộc nhiều container và Eventuate infrastructure.

## 2. Debezium Outbox

- Upstream/commit: `debezium/debezium-examples` — `b990468f6203`
- Phạm vi: chỉ tập trung folder [outbox](repositories/debezium-examples/outbox/README.md).
- [OrderService.java](repositories/debezium-examples/outbox/order-service/src/main/java/io/debezium/examples/outbox/order/service/OrderService.java) persist entity và fire `ExportedEvent` trong transaction.
- [OrderCreatedEvent.java](repositories/debezium-examples/outbox/order-service/src/main/java/io/debezium/examples/outbox/order/event/OrderCreatedEvent.java) cho thấy aggregate type/id, event type, timestamp và payload.
- [register-postgres.json](repositories/debezium-examples/outbox/register-postgres.json) cấu hình PostgreSQL CDC + `EventRouter`, route thành topic theo aggregate type và đưa event type vào header.
- [KafkaEventConsumer.java](repositories/debezium-examples/outbox/shipment-service/src/main/java/io/debezium/examples/outbox/shipment/facade/KafkaEventConsumer.java) đọc Kafka; [MessageLog.java](repositories/debezium-examples/outbox/shipment-service/src/main/java/io/debezium/examples/outbox/shipment/messagelog/MessageLog.java) minh họa duplicate detection.

Điểm học: business row và outbox row cùng local DB transaction loại dual-write DB/Kafka. CDC publish sau commit và có thể publish lại; consumer idempotency vẫn cần.

Điểm phản biện: `OrderEventHandler.onOrderEvent()` bao cả shipment update và message-log persist trong một `@Transactional`; primary key của `ConsumedMessage` là lớp dedupe cuối. Tuy nhiên hai delivery đồng thời vẫn có thể cùng vượt qua `alreadyProcessed()` rồi một transaction thua khi commit. Claim bằng unique insert sớm giúp fail sớm hơn; mọi side effect ngoài DB vẫn cần idempotency riêng vì DB rollback không hoàn tác được remote call.

## 3. Piomin Spring Microservices Advanced

- Upstream/commit: `piomin/sample-spring-microservices-advanced` — `c5ebfde55440`
- Source hiện tại: Spring Boot 4.1.1, Spring Cloud 2025.1.2, Java 25.
- Module: account, customer, product, transfer, gateway và Eureka discovery.
- Đọc root [pom.xml](repositories/sample-spring-microservices-advanced/pom.xml), sau đó [gateway application.yml](repositories/sample-spring-microservices-advanced/gateway-service/src/main/resources/application.yml) và [discovery application.yml](repositories/sample-spring-microservices-advanced/discovery-service/src/main/resources/application.yml).

Điểm phản biện: README còn mô tả lịch sử Zuul/Swagger2/JDK8 trong khi `pom.xml` đã lên Boot 4/Java 25. Luôn xem code/commit, không suy stack từ mô tả cũ. Repo phù hợp học service decomposition/gateway/discovery; không phải repo chính cho Kafka/OpenTelemetry như mô tả ban đầu.

## 4. Miaosha

- Upstream/commit: `qiurunze123/miaosha` — `e58017658e54`
- Stack chính: Spring Boot 2.6.1, Java 8 target, MyBatis, Redis, RabbitMQ; repo gồm nhiều generation/module.
- Luồng flash sale dễ theo nhất nằm tại [MiaoshaController.java](repositories/miaosha/miaosha-v1/src/main/java/com/geekq/miaosha/controller/MiaoshaController.java): dynamic path/captcha, preload Redis stock, decrement, enqueue và polling result.
- DB có conditional decrement tại [GoodsDao.java](repositories/miaosha/miaosha-v1/src/main/java/com/geekq/miaosha/dao/GoodsDao.java).
- Queue ở [MQSender.java](repositories/miaosha/miaosha-v1/src/main/java/com/geekq/miaosha/rabbitmq/MQSender.java) và [MQReceiver.java](repositories/miaosha/miaosha-v1/src/main/java/com/geekq/miaosha/rabbitmq/MQReceiver.java).
- Lua rate limit version v2 tại [limit.lua](repositories/miaosha/miaosha-v2/miaosha-service/src/main/resources/limit.lua).

Điểm phản biện quan trọng:

- `decr` rồi kiểm tra `< 0` có thể để counter âm; cần policy reset/atomic reservation rõ.
- check duplicate trước enqueue vẫn race nếu thiếu unique `(user_id, goods_id)` ở DB.
- Redis decrement + Rabbit publish là dual-write; crash giữa hai bước cần reservation ledger/relay/reconciliation.
- local `goodsOver` map chỉ là optimization theo JVM, không phải truth.
- đây là repo học thuật/legacy; audit security/dependency trước mọi reuse.

## 5. Loom vs WebFlux Benchmarks

- Replacement upstream/commit: `chrisgleissner/loom-webflux-benchmarks` — `2532cc8ca4cb`
- Lý do thay: URL `maciejwalkowiak/spring-boot-virtual-threads-benchmark` hiện trả repository not found.
- Stack: Spring Boot 4.1, Gradle 9.6.1, Java 21+, H2/JPA/Caffeine; tùy chọn PostgreSQL.
- Bốn profile ở [application files](repositories/loom-webflux-benchmarks/src/main/resources): `platform-tomcat`, `loom-tomcat`, `loom-netty`, `webflux-netty`.
- Shared Hikari/virtual-thread settings ở [application.yaml](repositories/loom-webflux-benchmarks/src/main/resources/application.yaml).
- Scenario là code/data trong [scenarios](repositories/loom-webflux-benchmarks/src/main/resources/scenarios); report đã công bố ở [results](repositories/loom-webflux-benchmarks/results).

Điểm học: so sánh phải giữ endpoint/workload/config tương đương và quan sát tail latency/resource metrics. H2 rất hữu ích để cô lập overhead framework nhưng không đại diện lock/I/O/network của production RDBMS; phải chạy thêm profile PostgreSQL.

Điểm phản biện: kết quả benchmark phụ thuộc OS/network tuning và scenario. `application.yaml` hiện đặt Hikari `maximumPoolSize=500` và `connectionTimeout=60s` như benchmark input; đây không phải production recommendation. Tests kỳ vọng Linux epoll nên một phần fail trên Windows NIO; benchmark scripts cũng hướng tới Bash/Linux/k6/sysstat.

## 6. Spring Petclinic Microservices

- Upstream/commit: `spring-petclinic/spring-petclinic-microservices` — `3858f9c630cf`
- Stack: Spring Boot 4.0.1, Spring Cloud 2025.1.0, Java 17; Gateway, Config, Eureka, Resilience4j, Micrometer/OpenTelemetry và Spring AI.
- Route/retry/circuit breaker: [gateway application.yml](repositories/spring-petclinic-microservices/spring-petclinic-api-gateway/src/main/resources/application.yml).
- Programmatic circuit breaker/time limiter: [ApiGatewayApplication.java](repositories/spring-petclinic-microservices/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/ApiGatewayApplication.java).
- API composition/fallback: [ApiGatewayController.java](repositories/spring-petclinic-microservices/spring-petclinic-api-gateway/src/main/java/org/springframework/samples/petclinic/api/boundary/web/ApiGatewayController.java).
- Observability annotations: `OwnerResource`, `PetResource`, `VisitResource`; infrastructure trong [docker-compose.yml](repositories/spring-petclinic-microservices/docker-compose.yml).

Điểm phản biện: Petclinic là reference/community sample dễ đọc, không chứng minh service boundary tối ưu cho mọi domain. Retry ở gateway chỉ an toàn cho idempotent operation hoặc operation có idempotency key; fallback không được che giấu data corruption.

## Bài lab đề xuất

1. Vẽ Create Order Saga từ FTGO thành state/step/compensation table; thêm failure ở mọi boundary.
2. Với Debezium, truy dấu một `eventId` từ transaction tới Kafka header rồi tới message log.
3. Với Miaosha, viết failure matrix cho Redis decrement ↔ Rabbit publish và đề xuất reservation outbox.
4. Với Loom benchmark, chạy H2 smoke trên Linux rồi PostgreSQL; so Hikari pending/p99, không chỉ RPS.
5. Với Petclinic, tắt một downstream và quan sát timeout/retry/circuit breaker/traces; kiểm tra retry amplification.
