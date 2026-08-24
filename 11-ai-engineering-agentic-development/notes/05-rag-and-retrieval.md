# 05 — RAG và retrieval engineering

RAG không phải “đưa PDF vào vector database”. Nó là pipeline lấy đúng evidence, đóng gói đúng context và buộc câu trả lời bám nguồn.

## Pipeline chuẩn

`source → parse → normalize → chunk → enrich metadata → index → retrieve → filter → rerank → context → answer → citation → eval`

### Ingestion

- Giữ `source_id`, version, owner, ACL, thời gian hiệu lực và checksum.
- Tách theo cấu trúc ngữ nghĩa trước, token window sau. Không cắt bảng/code/API contract tùy tiện.
- Lưu quan hệ parent-child: retrieve đoạn nhỏ nhưng có thể trả parent section đủ nghĩa.
- Xóa hoặc re-index khi tài liệu đổi; stale evidence là lỗi dữ liệu, không phải lỗi prompt.

### Retrieval

- Dense search tốt cho semantic similarity; lexical/BM25 tốt cho mã lỗi, tên class, ID và keyword hiếm.
- Hybrid search thường an toàn hơn chỉ vector. Reranker cải thiện thứ tự trên candidate set nhỏ.
- Filter tenant/ACL **trước hoặc trong retrieval**, không trông chờ model bỏ qua tài liệu trái quyền.
- Query rewrite hữu ích nhưng có thể đổi ý định; giữ original query và trace cả hai.

### Generation

Context packet nên chứa nguồn, ngày/version, đoạn trích và quy tắc khi evidence thiếu. Yêu cầu model chỉ kết luận trong phạm vi evidence, trả nguồn theo `source_id`, và nói rõ “không đủ dữ liệu” thay vì đoán.

## Chọn chunk

Không có chunk size tối ưu chung. Đánh giá theo loại tài liệu và câu hỏi:

| Loại | Điểm bắt đầu | Rủi ro |
|---|---|---|
| API/reference | theo heading/symbol | mất precondition giữa các section |
| Policy/legal | clause + parent heading | bỏ sót ngoại lệ |
| Source code | symbol/class/function | thiếu call graph/config |
| FAQ | một Q&A | duplicate gần nghĩa |
| Bảng | cả bảng hoặc row + schema | mất header/unit |

Overlap lớn làm tăng duplicate và chi phí. Hãy đo Recall@k, MRR/nDCG, answer groundedness và citation correctness.

## Failure taxonomy

1. **Corpus miss:** nguồn đúng chưa được ingest.
2. **Parse miss:** parser làm mất bảng/code/text.
3. **Index miss:** embedding/metadata sai hoặc index stale.
4. **Retrieve miss:** nguồn đúng không vào top-k.
5. **Rerank miss:** nguồn đúng bị đẩy xuống.
6. **Context miss:** truncate/packing làm mất evidence.
7. **Generation miss:** evidence đúng nhưng model suy luận/cite sai.

Tách metric theo stage giúp tránh “prompt tuning” để chữa lỗi ingestion.

## Production checklist

- Tenant isolation, document ACL và deletion propagation.
- Idempotent ingestion, versioning, dead-letter và replay.
- PII/secret detection trước index; encryption và retention.
- Context/token budget, timeout và fallback khi retriever lỗi.
- Golden query set có easy/hard/adversarial/no-answer cases.
- Trace query → candidates → scores → selected chunks → citations.

Lab [rag-eval-node](../labs/rag-eval-node/README.md) minh họa retrieval metrics độc lập với model/provider.
