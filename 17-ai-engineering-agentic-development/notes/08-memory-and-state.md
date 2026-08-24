# 08 — Memory và state

“Memory” không phải một vector database chứa toàn bộ chat. Hãy tách loại state, owner và retention.

| Loại | Ví dụ | Storage phù hợp |
|---|---|---|
| Turn state | tool calls của lượt hiện tại | request/checkpoint |
| Conversation state | messages gần nhất, summary | conversation store |
| Working memory | plan, assumptions, unresolved items | structured task state |
| Episodic | task trước và outcome | event/task history |
| Semantic | preference/fact đã xác minh | fact/profile store |
| Procedural | policy, skill, workflow | versioned code/config |

## Nguyên tắc

- Canonical business state nằm ở system of record, không nằm trong model context.
- Summary là lossy cache: giữ source pointers và rebuild được.
- Fact memory cần provenance, confidence, `valid_from/to`, owner và consent.
- Đừng ghi mọi model statement thành fact; cần extraction schema + validation.
- Namespace theo user/tenant/task và enforce ACL khi đọc, không chỉ khi ghi.
- Retention/deletion phải lan tới transcript, embedding, cache, trace và backup theo policy.

## Context assembly

Mỗi run chọn context theo task:

1. System/developer policy.
2. Current request và structured task state.
3. Relevant verified facts.
4. Retrieved evidence.
5. Recent tool observations cần thiết.

Dùng token budget theo section, deduplicate, ưu tiên recency/authority và ghi lý do một memory được chọn. Context caching giảm latency/cost cho prefix ổn định, nhưng không thay thế access control hoặc freshness check.

## Memory failure modes

- Poisoning qua prompt injection được lưu thành “preference”.
- Cross-tenant retrieval.
- Summary làm mất constraint quan trọng.
- Fact hết hạn vẫn được dùng.
- Feedback loop: model output được index rồi trở thành “nguồn”.
- Right-to-delete không xóa embedding/cache.

Test gồm: isolation, stale fact, conflicting facts, deletion, poisoned content và no-memory fallback.
