# Node Runtime Lab

Lab không dependency để thấy trực tiếp Node core:

- bounded concurrency thay `Promise.all` vô hạn;
- cancellation bằng `AbortSignal`;
- stream pipeline/backpressure;
- Worker Thread cho CPU-bound Fibonacci;
- built-in `node:test`.

```powershell
npm test --workspace @learning/node-runtime-lab
npm run demo --workspace @learning/node-runtime-lab
```

`runFibonacciInWorker` tạo worker mỗi lần chỉ để minh họa API. Production workload nhiều task phải dùng worker pool bounded; worker-per-request có startup/memory overhead.

Thử sửa `demo.js` để chạy Fibonacci synchronous trên main thread, đồng thời đặt timer 10 ms. So sánh timer delay với worker version.
