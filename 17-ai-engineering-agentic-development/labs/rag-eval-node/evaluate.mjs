import { readFile } from "node:fs/promises";
import { evaluateDataset } from "./src/metrics.mjs";

const dataset = JSON.parse(await readFile(new URL("./dataset.json", import.meta.url), "utf8"));
console.log(JSON.stringify(evaluateDataset(dataset, 3), null, 2));
