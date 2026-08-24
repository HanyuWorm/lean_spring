import { performance, monitorEventLoopDelay } from "node:perf_hooks";
import { runFibonacciInWorker } from "./worker-client.js";

const delay = monitorEventLoopDelay({ resolution: 10 });
delay.enable();

const started = performance.now();
const value = await runFibonacciInWorker(40);
const elapsed = performance.now() - started;

await new Promise((resolve) => setTimeout(resolve, 20));
delay.disable();

console.log({
  fibonacci40: value,
  workerElapsedMs: Math.round(elapsed),
  eventLoopMeanDelayMs: Number.isNaN(delay.mean)
    ? 0
    : Math.round(delay.mean / 1_000_000),
});
