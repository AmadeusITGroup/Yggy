<!-- AUTO-GENERATED FILE TEMPLATE: Do not edit generated docs/ADRs.md directly.
Edit this template (docs/templates/ADRs.index.template.md) and run one of:
Rows are injected where the placeholder ADR_ROWS appears. -->

# Global Architecture Decision Records

These ADRs apply across the entire repository (all modules). Module-specific ADRs (e.g., API-focused decisions) remain
in their respective module `docs/ADRs` directories.

| ADR | Title | Status |
|-----|-------|--------|

{{ADR_ROWS}}

## Usage

1. For a new cross-module decision: copy [ADR-Template](./templates/ADR-000-template.md) and use the next sequential
   number.
2. Draft with `Status: Proposed` until reviewed.
3. After acceptance, update any superseded ADR header.
4. Module-local decisions should remain in module scope unless elevated to global relevance.

## Status Definitions

- Draft - Early work in progress.
- Proposed - Under review / pending acceptance.
- Accepted - Authoritative and in force.
- Deprecated - Still in use but planned for retirement.
- Superseded - Fully replaced by a newer ADR.

## References

- This index is the authoritative source for global coding, style, and architectural policies.
- Module-level ADR indices (e.g., `api/docs/ADRs.md`) for domain-specific decisions.

