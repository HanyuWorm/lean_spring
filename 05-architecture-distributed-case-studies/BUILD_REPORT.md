# Build Report

Ngày kiểm tra: 2026-08-24 trên Windows/PowerShell.

## Toolchain

- Maven 3.9.9
- JDK đã cài: 17, 21, 23; không có JDK 8/11/25
- Docker CLI/Compose có, nhưng Docker Desktop Linux daemon chưa chạy
- Node.js 24.15.0

Mọi source upstream được giữ nguyên. Các override dưới đây chỉ là command-line property, không sửa `pom.xml`/Gradle files.

## Kết quả

| Repository | Build đã thử | Kết quả |
|---|---|---|
| FTGO | `gradlew.bat assemble` với JDK 17 | Fail sớm: Gradle 6.9.1/Groovy không đọc được class major version 61. Cần JDK cũ tương thích (repo target Java 8), sau đó còn cần infra cho integration/full run. |
| Debezium `outbox` | `mvn -DskipTests package` với JDK 21 | Pass; build cả `order-service` và `shipment-service`. |
| Piomin advanced | `mvn -DskipTests package` với JDK 23 | Fail đúng cấu hình vì project yêu cầu release 25. Build compatibility `-Djava.version=23` pass. |
| Miaosha root reactor | `mvn -DskipTests package` với JDK 17 | Fail tại Spring Boot repackage của library module không có main class. Bỏ repackage vẫn lộ lỗi source v2 thiếu `com.geekq.api.entity.GoodsVoOrder`. |
| Miaosha v1 | chạy package riêng tại `miaosha-v1` | Pass với JDK 17; đây là module nên dùng cho flow flash-sale chính. |
| Loom/WebFlux benchmark | `gradlew.bat classes` | Pass với JDK 21. |
| Loom/WebFlux benchmark | `gradlew.bat build` | Compile pass; test chạy 119, fail 29. Nguyên nhân chính: test kỳ vọng Linux `epoll` nhưng Windows dùng Netty `nio`, và hai PostgreSQL Testcontainers test không kết nối được Docker. |
| Loom/WebFlux H2 smoke | build `bootJar`, chạy profile `loom-netty` ở port 18080 | Pass: Spring Boot 4.1.1/Java 21 khởi động với H2 + Hikari, `/actuator/health` trả `UP`; sau đó shutdown graceful, process exit 0. |
| Petclinic microservices | `mvnw.cmd -DskipTests package` | Pass với JDK 21 cho toàn reactor. |

## Lệnh tái lập

### Debezium Outbox

```powershell
cd I:\Dev\DS\05-architecture-distributed-case-studies\repositories\debezium-examples\outbox
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -DskipTests package
```

Full demo cần Docker daemon:

```powershell
docker compose --env-file ..\.env up --build
```

### Piomin

Đúng upstream cần JDK 25. Override dưới đây chỉ để compile trên máy hiện tại:

```powershell
cd I:\Dev\DS\05-architecture-distributed-case-studies\repositories\sample-spring-microservices-advanced
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-23'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -DskipTests "-Djava.version=23" package
```

### Miaosha v1

```powershell
cd I:\Dev\DS\05-architecture-distributed-case-studies\repositories\miaosha\miaosha-v1
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -DskipTests package
```

Runtime cần MySQL, Redis và RabbitMQ theo cấu hình của repo. Không dùng credential/config demo cho môi trường thật.

### Loom/WebFlux benchmark

```powershell
cd I:\Dev\DS\05-architecture-distributed-case-studies\repositories\loom-webflux-benchmarks
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat classes --no-daemon
```

Smoke run H2 đã được xác minh bằng profile `loom-netty`; không cần external database. Cấu hình repo dùng Hikari pool/timeout lớn để phục vụ thí nghiệm benchmark, không nên copy nguyên sang production.

Full benchmark được thiết kế cho Linux/Bash với k6, Python libs, `sysstat` và host tuning. Nên chạy trong Linux/WSL2; PostgreSQL scenarios còn cần Docker. Không chạy high-load/soak trên máy dev nếu chưa cô lập tài nguyên.

### Petclinic

```powershell
cd I:\Dev\DS\05-architecture-distributed-case-studies\repositories\spring-petclinic-microservices
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package
```

Để chạy toàn stack, bật Docker Desktop rồi dùng profile build image theo README upstream; cần cấp đủ RAM cho nhiều service.

### FTGO

FTGO target Java 8 và wrapper Gradle 6.9.1. Trên máy này không có JDK tương thích. Sau khi cài một JDK phù hợp, thực hiện theo upstream:

```powershell
.\gradlew.bat buildContracts
.\gradlew.bat assemble
```

Không tự nâng wrapper/dependency trong clone vì thay đổi đó có thể kéo theo migration Spring/Eventuate và làm sai mục tiêu đọc case study.

## Chưa chạy và lý do

- Docker Compose stacks: Docker daemon không hoạt động.
- FTGO assemble/tests: thiếu JDK tương thích với Gradle 6.9.1.
- Piomin build đúng Java 25: máy chỉ có tối đa JDK 23.
- Loom high-load benchmark: Windows không phải host được scripts/epoll expectations nhắm tới; chưa có Docker/k6/sysstat environment hoàn chỉnh.
- Integration tests cần Kafka/PostgreSQL/MySQL/Redis/RabbitMQ: tránh báo “pass” giả khi dependency chưa khởi động.
