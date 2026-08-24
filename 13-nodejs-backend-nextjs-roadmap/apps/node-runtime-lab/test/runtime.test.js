import assert from "node:assert/strict";
import { Readable, Writable } from "node:stream";
import test from "node:test";
import { abortableDelay, mapWithConcurrency } from "../src/async-tools.js";
import { uppercasePipeline } from "../src/stream-tools.js";
import { runFibonacciInWorker } from "../src/worker-client.js";

test("mapWithConcurrency preserves order and bounds in-flight work", async () => {
  let inFlight = 0;
  let maximum = 0;

  const result = await mapWithConcurrency([1, 2, 3, 4, 5], 2, async (value) => {
    inFlight += 1;
    maximum = Math.max(maximum, inFlight);
    await new Promise((resolve) => setTimeout(resolve, 5));
    inFlight -= 1;
    return value * 2;
  });

  assert.deepEqual(result, [2, 4, 6, 8, 10]);
  assert.equal(maximum, 2);
});

test("abortableDelay observes cancellation", async () => {
  const controller = new AbortController();
  controller.abort();
  await assert.rejects(abortableDelay(1_000, controller.signal), {
    name: "AbortError",
  });
});

test("stream pipeline transforms chunks without collecting all input", async () => {
  let output = "";
  const destination = new Writable({
    write(chunk, _encoding, callback) {
      output += chunk.toString();
      callback();
    },
  });

  await uppercasePipeline(Readable.from(["node", " ", "streams"]), destination);
  assert.equal(output, "NODE STREAMS");
});

test("CPU work can run in a Worker Thread", async () => {
  assert.equal(await runFibonacciInWorker(10), 55);
});
