# Architecture & Distributed Systems Case Studies

Workspace này chứa source upstream được clone nguyên trạng trong `repositories/` và note/build report do chúng ta viết ở ngoài repository. Không sửa source upstream trừ khi một bài lab sau này yêu cầu rõ.

## Repository map

| Folder | Upstream | Chủ đề chính |
|---|---|---|
| [`ftgo-application`](repositories/ftgo-application) | `microservices-patterns/ftgo-application` | Saga orchestration, transactional messaging, CQRS, Event Sourcing |
| [`debezium-examples`](repositories/debezium-examples) | `debezium/debezium-examples` | Outbox, CDC, Kafka, PostgreSQL |
| [`sample-spring-microservices-advanced`](repositories/sample-spring-microservices-advanced) | `piomin/sample-spring-microservices-advanced` | Spring Cloud microservices; dùng để phân tích cả legacy decisions |
| [`miaosha`](repositories/miaosha) | `qiurunze123/miaosha` | Flash sale, Redis/Lua/queue/idempotency |
| [`loom-webflux-benchmarks`](repositories/loom-webflux-benchmarks) | replacement cho URL benchmark không tồn tại | Virtual Threads vs WebFlux benchmark hiện đại |
| [`spring-petclinic-microservices`](repositories/spring-petclinic-microservices) | `spring-petclinic/spring-petclinic-microservices` | Spring Cloud reference architecture/community project |

## Trạng thái URL benchmark ban đầu

`https://github.com/maciejwalkowiak/spring-boot-virtual-threads-benchmark` trả `Repository not found` khi xác minh ngày 2026-08-24. Replacement được chọn là `https://github.com/chrisgleissner/loom-webflux-benchmarks`, vì bao phủ trực tiếp Spring Boot 4.1, Virtual Threads và WebFlux với scripts/results có thể tái lập.

## Học theo thứ tự

1. Đọc [Repository Guide](REPOSITORY_GUIDE.md) để biết file/class nào đáng xem.
2. Đọc bốn bài toán trong [`notes`](notes/README.md).
3. Xem [Build Report](BUILD_REPORT.md) trước khi chạy vì một số repo cần Docker hoặc JDK cũ.
4. Không dùng benchmark/repo demo làm production blueprint nếu chưa đánh giá version, failure model và operational gaps.

## Kết quả nhanh

- Build pass: Debezium Outbox, Piomin với Java 23 override, Miaosha v1, Loom compile và Petclinic package.
- Build bị giới hạn: FTGO cần JDK cũ; Miaosha full reactor có lỗi module v2; Loom test phụ thuộc Linux epoll/Docker.
- Full Compose chưa chạy vì Docker daemon hiện không hoạt động. Chi tiết và lệnh tái lập nằm trong [Build Report](BUILD_REPORT.md).
