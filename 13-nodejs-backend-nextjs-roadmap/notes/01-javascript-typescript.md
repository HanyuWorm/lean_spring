# 01 — JavaScript và TypeScript cho Backend

## JavaScript khác Java ở đâu?

JavaScript là dynamically typed, prototype-based và có function/closure là giá trị hạng nhất. TypeScript bổ sung static analysis nhưng type bị erase khi chạy; runtime vẫn là JavaScript. Vì vậy request JSON luôn là `unknown` cho tới khi được runtime validation.

## Giá trị, reference và mutation

Primitive gồm string, number, bigint, boolean, undefined, symbol và null. Object/array/function được truyền bằng một giá trị reference; gán biến mới không deep-copy object.

```ts
const original = { nested: { count: 1 } };
const shallow = { ...original };
shallow.nested.count = 2; // original.nested.count cũng là 2
```

Spread là shallow copy. Dữ liệu domain nên ưu tiên immutable update; `readonly` của TypeScript chủ yếu là compile-time và cũng có thể chỉ shallow.

## Scope, closure và lifetime

`let`/`const` có block scope; `var` có function scope và hoisting dễ gây lỗi. Closure giữ lexical variables kể cả outer function đã return. Điều này rất mạnh cho factory/middleware, đồng thời có thể giữ payload lớn qua callback/future/queue.

```ts
function makeHandler(largeBuffer: Buffer) {
  return () => largeBuffer.length; // buffer sống bằng handler
}
```

## `this`

`this` phụ thuộc cách gọi function; arrow function capture lexical `this`. Khi truyền method làm callback, có thể mất receiver. Trong backend hiện đại, module function thuần và explicit dependency thường rõ hơn class state + bind phức tạp.

## Promise và async/await

`async` luôn trả Promise. `await` suspend async function và xếp continuation vào microtask; nó không block OS thread khi đang chờ asynchronous I/O, cũng không tự chuyển CPU work sang thread khác.

```ts
const [user, orders] = await Promise.all([
  loadUser(id),
  loadOrders(id),
]);
```

Chỉ parallelize các operation độc lập và đặt concurrency limit. `Promise.all` fail-fast nhưng không tự cancel operation còn lại; truyền chung `AbortSignal` khi API hỗ trợ.

Các lỗi phổ biến:

- `array.forEach(async x => ...)` không await callbacks;
- `map(async ...)` tạo mảng Promise nhưng quên `await Promise.all`;
- `Promise.all` trên hàng triệu item tạo unbounded concurrency;
- `new Promise(async resolve => ...)` là anti-pattern;
- quên handle rejection hoặc fire-and-forget không có owner.

## ESM và CommonJS

Code mới dùng ESM với `"type": "module"`, `import`/`export`. CommonJS dùng `require`/`module.exports`. Interop, default export, file extension và package `exports` có nhiều edge case; không trộn tùy hứng.

Package public nên khai báo `exports`, type declarations và supported Node version. Deep import vào internal path của dependency là coupling không được bảo đảm.

## TypeScript strict essentials

### `unknown` thay vì `any`

`any` tắt type checking và lan truyền. `unknown` bắt buộc narrow:

```ts
function message(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
```

### Union và narrowing

```ts
type PaymentResult =
  | { kind: "approved"; paymentId: string }
  | { kind: "rejected"; reason: string };

function status(result: PaymentResult): number {
  switch (result.kind) {
    case "approved": return 201;
    case "rejected": return 422;
    default: return assertNever(result);
  }
}

function assertNever(value: never): never {
  throw new Error(`Unexpected variant: ${JSON.stringify(value)}`);
}
```

Discriminated union gần sealed hierarchy/record trong Java và phù hợp cho state/result.

### Structural typing

TypeScript tương thích theo shape, không theo nominal identity. Hai type có cùng members có thể assign được. Dùng opaque/branded type nếu không muốn nhầm `UserId` và `OrderId`, nhưng nhớ đây vẫn chỉ compile-time.

### Generic vừa đủ

Generic tốt khi giữ quan hệ giữa input/output. Generic phức tạp chỉ để “clever” làm error message và maintenance tệ hơn. Domain type rõ thường tốt hơn framework-like abstraction.

## Runtime validation

TypeScript không validate JSON, env var, DB row hay message. Boundary phải parse bằng JSON Schema/Ajv, TypeBox, Zod hoặc validator phù hợp. Sau parse mới tin type.

```text
untrusted bytes -> parse -> validate/normalize -> typed DTO -> domain
```

Không dùng type assertion `payload as CreateOrder` thay validation.

## Checklist

- `strict`, `noUncheckedIndexedAccess` và explicit boundary types được cân nhắc?
- Không có `any`/unsafe assertion ở external boundary?
- Async collection có await và concurrency limit?
- Promise có timeout/cancellation/owner?
- Closure có capture graph lớn hoặc request object?
- ESM/CJS và package export nhất quán?
