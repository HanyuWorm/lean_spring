# 02 — Node.js Runtime, Event Loop và Streams

## Thành phần runtime

- **V8** parse/JIT JavaScript và quản lý heap/GC.
- **Node core** cung cấp HTTP, filesystem, crypto, streams, process APIs.
- **libuv** cung cấp event loop, cross-platform async I/O và worker pool cho một số operation.
- **OS/kernel** xử lý network readiness và nhiều asynchronous operations.

“Single-threaded” chỉ mô tả JavaScript callback execution trong một isolate thông thường. Process vẫn có GC/JIT/libuv threads và có thể tạo Worker Threads.

## Event loop

Event loop chạy các phase/tác vụ như timers, pending callbacks, poll, check và close callbacks. `process.nextTick` queue và Promise microtasks được xử lý với priority semantics riêng; recursive microtask/nextTick có thể starve I/O.

```ts
function starve(): void {
  queueMicrotask(starve);
}
starve(); // event loop không quay lại xử lý I/O bình thường
```

Điều cần nhớ không phải thuộc lòng phase diagram mà là: callback phải ngắn, bounded và yield/đẩy CPU work ra worker khi cần.

## I/O-bound và CPU-bound

Node mạnh với nhiều I/O concurrent khi callback nhẹ. JSON parse/stringify khổng lồ, regex pathological, compression/crypto sync hoặc vòng lặp CPU dài block mọi request trên cùng isolate.

Giải pháp CPU-bound:

- tối ưu/bound input;
- chunk và yield nếu latency cho phép;
- Worker Thread pool cho JavaScript CPU work;
- service/process chuyên dụng;
- native/other platform khi workload phù hợp.

Worker Threads không làm I/O thông thường nhanh hơn. Tạo worker cho mỗi request cũng đắt; dùng pool và bound queue.

## libuv worker pool

Một số filesystem, DNS, crypto và zlib APIs dùng libuv thread pool. Nếu tasks nặng chiếm pool, operation khác cùng pool bị head-of-line blocking. `UV_THREADPOOL_SIZE` không phải tuning button tùy tiện; đo workload và CPU trước.

## Timers không phải deadline tuyệt đối

`setTimeout(fn, 100)` nghĩa callback đủ điều kiện sau ít nhất khoảng thời gian đó; event-loop blockage làm nó chạy trễ. Dùng monotonic duration, deadline budget và `AbortSignal.timeout()`/controller cho I/O hỗ trợ cancellation.

Timeout không đồng nghĩa operation đã dừng. Nếu chỉ `Promise.race`, loser có thể tiếp tục dùng socket/DB và giữ memory.

## Streams và backpressure

Stream xử lý dữ liệu theo chunk thay vì materialize toàn bộ. Writable trả `false` khi internal buffer đạt high-water mark; producer phải chờ `drain`. Dùng `pipeline` để nối stream và propagate error/cleanup.

```ts
await pipeline(
  createReadStream(input),
  createGzip(),
  createWriteStream(output),
);
```

Không bỏ qua backpressure bằng gọi `write()` liên tục. High-water mark là threshold, không hard memory cap; số pipeline concurrent và transform expansion vẫn phải bounded.

## Buffers

`Buffer` đại diện binary data và có thể dùng memory ngoài V8 heap theo cách accounting của Node/V8. Slice/view có thể giữ toàn backing buffer. Copy nhỏ từ buffer 100 MB hoặc decode/base64 nhiều lần dễ làm RSS/heap tăng.

## Worker Thread, child process và replicas

| Cơ chế | Isolation | Dùng cho |
|---|---|---|
| Worker Thread | cùng process, isolate riêng; có thể transfer/share buffer | CPU parallelism |
| Child process | process/address space riêng, IPC | isolation, external command |
| Nhiều container/process | failure/resource isolation tốt | scale production HTTP |

Cluster module không thay thế orchestrator/load balancer trong mọi deployment. Với Kubernetes, thường một process chính/container và scale replicas dễ budget hơn.

## Async context

`AsyncLocalStorage` truyền correlation/tenant/request context qua async chain, tương tự MDC/ThreadLocal về mục đích nhưng runtime semantics khác. Store chỉ giữ ID nhỏ; disable/cleanup lifecycle đúng và không dùng nó thay explicit domain parameter.

## Process lifecycle

- `SIGTERM`: dừng nhận request, mark unready, chờ in-flight với deadline, đóng pool/client rồi exit.
- `uncaughtException`: state có thể không đáng tin; log/flush tối thiểu và restart, không tiếp tục phục vụ tùy tiện.
- `unhandledRejection`: coi nghiêm túc; luôn owner Promise.
- `beforeExit`/`exit`: không phải nơi làm arbitrary async cleanup.

## Built-in test runner và diagnostics

Node hiện đại có `node:test`, mock/timer/watch/coverage capabilities theo version. Runtime có inspector, CPU/heap profiles, diagnostic report, `perf_hooks` event-loop delay/utilization. Chọn công cụ theo hypothesis, không bật profiler nặng vô thời hạn trong production.

## Checklist

- Endpoint có CPU synchronous path theo input size?
- Promise concurrency và worker queue bounded?
- Timeout có cancellation thật?
- Stream có `pipeline`, backpressure và cleanup?
- Event-loop lag/utilization có dashboard?
- SIGTERM shutdown đã test với request đang chạy?
