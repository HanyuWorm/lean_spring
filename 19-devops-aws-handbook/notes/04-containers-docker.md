# 04 — Container và Docker production

## 1. Container không phải VM nhẹ

Container là process được cô lập bằng namespaces, giới hạn/account tài nguyên bằng cgroups và filesystem layers. Nhiều container vẫn dùng chung host kernel. Vì vậy:

- container escape/kernel vulnerability có boundary khác VM;
- image không chứa kernel riêng;
- process vẫn cần signal, user, FD, memory và network đúng;
- “chạy được trên máy tôi” chỉ đúng khi image, config và runtime contract tương đương.

## 2. Image tốt

- Multi-stage build: builder có compiler/tool; runtime chỉ có thứ cần chạy.
- Pin base image theo version và khi cần reproducibility cao thì theo digest.
- Dependency lock và deterministic build.
- Không copy `.git`, secret, test output, local cache vào build context.
- Chạy non-root; filesystem read-only nếu app cho phép.
- Không cài shell/package manager vào runtime image nếu không cần.
- Gắn OCI labels/source revision và sinh SBOM/provenance.

Ví dụ mental model:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 app
USER 10001
COPY --from=build /src/target/app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Trong project thật phải tối ưu cache Maven, chọn đúng artifact và có test/scan; ví dụ trên chỉ minh họa separation.

## 3. Layer và cache

- Layer thay đổi ít đặt trước, source thay đổi nhiều đặt sau.
- `.dockerignore` giảm context và tránh leak.
- Cache là optimization, không là nguồn sự thật.
- Không dùng `latest` làm định danh deploy; dùng digest hoặc immutable tag.

## 4. Runtime contract

- Config từ environment/file mount; secret không bake vào image.
- Log ra stdout/stderr có cấu trúc; không giữ file local vô hạn.
- Expose port chỉ là metadata; publish port là runtime concern.
- App xử lý `SIGTERM`: readiness false, ngừng nhận request, drain, đóng resource, exit trước grace period.
- Health check phải rẻ và có ý nghĩa; không biến liveness thành truy vấn mọi dependency.

## 5. Resource

- CPU limit thường gây throttling, không giết process.
- Memory limit có thể dẫn OOM kill; JVM/container sizing phải chừa native headroom.
- Không đặt limit tùy tiện bằng “request trung bình”. Dùng load test, working set, spike và SLO.
- Kiểm soát PID, FD, ephemeral storage và log growth ngoài CPU/RAM.

## 6. Network và storage

- Container IP thường ephemeral; giao tiếp qua service discovery/network abstraction.
- `localhost` trong container là chính container đó.
- Volume lifecycle độc lập container khi cần persistence; nhưng backup/restore vẫn là yêu cầu riêng.
- Không lưu database production trong writable layer của container.

## 7. Security checklist

- minimal trusted base image và rebuild định kỳ;
- non-root, drop Linux capabilities, no privileged;
- read-only root filesystem và writable mount tối thiểu;
- secret qua runtime identity/store;
- scan OS packages + application dependencies;
- verify signature/provenance trước deploy;
- runtime/network policy và audit.

## 8. Debug checklist

1. Image digest có đúng artifact đã test không?
2. Entrypoint/command và working directory đúng không?
3. Process có chạy và listen đúng interface/port không?
4. DNS/network route/policy đến dependency có đúng không?
5. Config/secret mount và permission có đúng không?
6. Bị OOM kill, CPU throttle, disk/FD limit hay probe kill không?
7. Signal/graceful shutdown có làm request bị cắt không?

Nguồn: [Docker Build](https://docs.docker.com/build/), [Docker multi-stage builds](https://docs.docker.com/build/building/multi-stage/).

