import test from "node:test";
import assert from "node:assert/strict";
import { evaluateDataset, hitRateAtK, ndcgAtK, precisionAtK, recallAtK, reciprocalRank } from "../src/metrics.mjs";

test("computes binary retrieval metrics", () => {
  const results = ["wrong", "right", "also-right"];
  const relevant = ["right", "also-right"];
  assert.equal(precisionAtK(results, relevant, 2), 0.5);
  assert.equal(recallAtK(results, relevant, 2), 0.5);
  assert.equal(hitRateAtK(results, relevant, 1), 0);
  assert.equal(reciprocalRank(results, relevant), 0.5);
  assert.ok(ndcgAtK(results, relevant, 3) > 0.69);
});

test("treats an empty result as correct for no-answer case", () => {
  assert.equal(recallAtK([], [], 3), 1);
  assert.equal(hitRateAtK([], [], 3), 1);
  assert.equal(ndcgAtK([], [], 3), 1);
});

test("deduplicates ranked results", () => {
  assert.equal(recallAtK(["a", "a", "b"], ["a", "b"], 2), 1);
});

test("aggregates dataset rows", () => {
  const report = evaluateDataset([{ id: "one", results: ["a"], relevant: ["a"] }], 1);
  assert.deepEqual({ cases: report.cases, precision: report.precision, recall: report.recall, mrr: report.mrr }, { cases: 1, precision: 1, recall: 1, mrr: 1 });
});
