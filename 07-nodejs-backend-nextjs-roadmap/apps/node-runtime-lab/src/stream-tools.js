import { Transform } from "node:stream";
import { pipeline } from "node:stream/promises";

export function createUppercaseTransform() {
  return new Transform({
    transform(chunk, _encoding, callback) {
      callback(null, chunk.toString().toUpperCase());
    },
  });
}

export async function uppercasePipeline(readable, writable) {
  await pipeline(readable, createUppercaseTransform(), writable);
}
