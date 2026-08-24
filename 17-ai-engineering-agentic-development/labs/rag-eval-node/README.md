# RAG Retrieval Eval Node

Lab tách retrieval quality khỏi LLM. Dataset fixture chứa ranked document IDs và relevant IDs; chương trình tính Precision@k, Recall@k, Hit Rate@k, MRR và nDCG@k.

Trong lab, no-answer case trả danh sách rỗng được chấm `1`; production nên report thêm abstention/no-answer accuracy thành slice riêng để không làm méo retrieval metric.

```powershell
cd 17-ai-engineering-agentic-development/labs/rag-eval-node
npm test
npm run eval
```

Lý do chạy offline: metric phải deterministic và không bị che bởi answer nghe hợp lý. Trong dự án thật, export top-k từ retriever vào cùng schema rồi đặt release gate theo slice.

## Bài tập

- Thêm graded relevance cho nDCG thay vì binary.
- Report theo tag như `exact-id`, `semantic`, `no-answer`, `tenant-acl`.
- So sánh lexical, dense, hybrid và reranker trên cùng query set.
- Fail CI khi Recall@5 critical slice giảm quá ngưỡng.
