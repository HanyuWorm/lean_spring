# Pipeline review

## Scope

- Service/repository:
- Owner/on-call:
- Environments/accounts/regions:
- User journey và SLO:

## Source và identity

- Protected branches/CODEOWNERS:
- Trigger và untrusted-input boundary:
- Human/workflow/runtime identities:
- OIDC trust conditions và least privilege:
- Break-glass path:

## Build và artifact

- Reproducible build/dependency lock:
- Artifact version/digest/registry:
- SBOM/signature/provenance:
- Scan/policy gates và exception expiry:
- Commit → artifact traceability:

## Deployment

- Strategy và fault-isolation unit:
- Config/secret delivery:
- Database migration order:
- SLI/business health gates:
- Bake time/sample/missing-data policy:
- Rollback và roll-forward triggers:

## Failure review

- Runner/build dependency unavailable:
- Artifact/KMS/registry deny:
- Partial IaC apply:
- Deployment health false positive/negative:
- Rollback fails:
- Audit/notification unavailable:

## Decision

- Risks accepted:
- Required actions, owner, deadline:
- Evidence cần trước production:

