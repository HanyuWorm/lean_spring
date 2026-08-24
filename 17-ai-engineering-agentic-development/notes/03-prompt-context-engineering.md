# 03 — Prompt và context engineering

## Prompt contract

Một instruction tốt nêu: role/capability cần thiết, objective, authoritative context, constraints, tools/permissions, output schema, evidence/citations, ambiguity policy và stop/success criteria.

```text
Goal: review migration for data-loss risks.
Authoritative inputs: architecture.md + migration.sql.
Constraints: do not edit; cite file/line; max 5 findings.
Evidence: every finding needs exact mechanism and reproduction/verification.
Output: JSON matching Finding[].
Stop: if required file missing, return BLOCKED with missing paths.
```

Prompt dài không mặc định tốt. Nói mỗi rule một lần, tool descriptions ngắn/chính xác và giữ examples chỉ khi encode product requirement hoặc sửa measured failure.

## Context assembly

Phân lớp:

1. Stable policy/system instructions.
2. Product/domain spec liên quan.
3. Task packet hiện tại.
4. Retrieved evidence.
5. Working state/tool observations.
6. Output contract.

Stable prefix giúp cache; dynamic tail chứa task. Context budget theo relevance, authority, recency và diversity. Không dump toàn repo, full DB schema hay toàn chat khi chỉ cần một module.

## Context injection defense

Retrieved page/file/tool result là **data không tin cậy**, không được tự trở thành instruction. Tách instruction khỏi quoted content, allowlist sources/tools, propagate provenance và không cho text trong document mở rộng permission.

## Long-running tasks

Dùng canonical artifacts, compact summaries có facts/decisions/open issues/evidence, task IDs và state machine. Summary cũng có thể sai nên critical facts phải trỏ về source. Context window lớn không thay information architecture.
