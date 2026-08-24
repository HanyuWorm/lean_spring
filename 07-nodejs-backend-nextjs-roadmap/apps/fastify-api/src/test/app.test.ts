import assert from "node:assert/strict";
import { afterEach, test } from "node:test";
import type { FastifyInstance } from "fastify";
import { buildApp } from "../app.js";

let app: FastifyInstance | undefined;

afterEach(async () => {
  await app?.close();
  app = undefined;
});

test("rejects an invalid task at the HTTP boundary", async () => {
  app = buildApp();
  const response = await app.inject({
    method: "POST",
    url: "/tasks",
    headers: { "idempotency-key": "request-0001" },
    payload: { title: "", unexpected: true },
  });

  assert.equal(response.statusCode, 400);
});

test("replays the result for the same idempotency key and payload", async () => {
  app = buildApp();
  const request = {
    method: "POST" as const,
    url: "/tasks",
    headers: { "idempotency-key": "request-0002" },
    payload: { title: "Learn the event loop" },
  };

  const first = await app.inject(request);
  const second = await app.inject(request);

  assert.equal(first.statusCode, 201);
  assert.deepEqual(second.json(), first.json());

  const list = await app.inject({ method: "GET", url: "/tasks" });
  assert.equal(list.json().length, 1);
});

test("rejects reuse of an idempotency key with another payload", async () => {
  app = buildApp();
  const headers = { "idempotency-key": "request-0003" };

  await app.inject({ method: "POST", url: "/tasks", headers, payload: { title: "A" } });
  const conflict = await app.inject({
    method: "POST",
    url: "/tasks",
    headers,
    payload: { title: "B" },
  });

  assert.equal(conflict.statusCode, 409);
  assert.equal(conflict.json().code, "IDEMPOTENCY_CONFLICT");
});
