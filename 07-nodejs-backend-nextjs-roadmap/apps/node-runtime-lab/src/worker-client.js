import { Worker } from "node:worker_threads";

export function runFibonacciInWorker(n) {
  if (!Number.isInteger(n) || n < 0 || n > 45) {
    return Promise.reject(new RangeError("n must be an integer between 0 and 45"));
  }

  return new Promise((resolve, reject) => {
    const worker = new Worker(new URL("./fibonacci-worker.js", import.meta.url), {
      workerData: n,
    });

    worker.once("message", resolve);
    worker.once("error", reject);
    worker.once("exit", (code) => {
      if (code !== 0) reject(new Error(`Worker stopped with exit code ${code}`));
    });
  });
}
