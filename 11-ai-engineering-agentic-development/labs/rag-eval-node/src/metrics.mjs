const uniqueTopK = (results, k) => [...new Set(results)].slice(0, k);

export function precisionAtK(results, relevant, k) {
  if (k <= 0) throw new RangeError("k must be positive");
  const relevantSet = new Set(relevant);
  if (relevantSet.size === 0) return results.length === 0 ? 1 : 0;
  const hits = uniqueTopK(results, k).filter((id) => relevantSet.has(id)).length;
  return hits / k;
}

export function recallAtK(results, relevant, k) {
  const relevantSet = new Set(relevant);
  if (relevantSet.size === 0) return results.length === 0 ? 1 : 0;
  const hits = uniqueTopK(results, k).filter((id) => relevantSet.has(id)).length;
  return hits / relevantSet.size;
}

export function hitRateAtK(results, relevant, k) {
  if (relevant.length === 0) return results.length === 0 ? 1 : 0;
  const relevantSet = new Set(relevant);
  return uniqueTopK(results, k).some((id) => relevantSet.has(id)) ? 1 : 0;
}

export function reciprocalRank(results, relevant) {
  const relevantSet = new Set(relevant);
  if (relevantSet.size === 0) return results.length === 0 ? 1 : 0;
  const rank = results.findIndex((id) => relevantSet.has(id));
  return rank < 0 ? 0 : 1 / (rank + 1);
}

export function ndcgAtK(results, relevant, k) {
  const relevantSet = new Set(relevant);
  const dcg = uniqueTopK(results, k).reduce(
    (score, id, index) => score + (relevantSet.has(id) ? 1 / Math.log2(index + 2) : 0),
    0
  );
  const idealHits = Math.min(relevantSet.size, k);
  if (idealHits === 0) return results.length === 0 ? 1 : 0;
  const idcg = Array.from({ length: idealHits }, (_, index) => 1 / Math.log2(index + 2))
    .reduce((sum, score) => sum + score, 0);
  return dcg / idcg;
}

export function evaluateDataset(dataset, k = 3) {
  const rows = dataset.map((item) => ({
    id: item.id,
    precision: precisionAtK(item.results, item.relevant, k),
    recall: recallAtK(item.results, item.relevant, k),
    hitRate: hitRateAtK(item.results, item.relevant, k),
    mrr: reciprocalRank(item.results, item.relevant),
    ndcg: ndcgAtK(item.results, item.relevant, k)
  }));
  const average = (key) => rows.reduce((sum, row) => sum + row[key], 0) / rows.length;
  return { k, cases: rows.length, precision: average("precision"), recall: average("recall"), hitRate: average("hitRate"), mrr: average("mrr"), ndcg: average("ndcg"), rows };
}
