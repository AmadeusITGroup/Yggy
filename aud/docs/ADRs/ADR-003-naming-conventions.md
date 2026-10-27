# ADR-003: Naming Conventions (Code and Serialized Formats)

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Consistent naming improves readability, onboarding speed, and reduces subtle integration bugs (e.g., casing mismatches
across API boundaries). The monorepo spans Java source, generated OpenAPI contracts, YAML/JSON payloads, and
configuration. Existing external payloads use `snake_case`; migrating them now would introduce churn and risk.

## Problem / Forces

- Need predictability for identifier style across layers.
- Prevent accidental mixed casing in payloads.
- Align with broadly adopted Java style internally.
- Avoid unnecessary breaking changes for existing clients.

## Decision

1. Java Source: Google Java Style – `PascalCase` types, `camelCase` members/methods, `UPPER_SNAKE_CASE` constants.
2. Serialized JSON/YAML: Continue `snake_case` for all new and existing fields.
3. New fields MUST follow `snake_case` for backward consistency.
4. Mapping layers (DTO ↔ domain) may use explicit annotations; prefer clarity to reflection ambiguity.

## Rationale

- Preserves external contract stability.
- Minimizes cognitive switching for Java developers internally.
- Avoids hybrid payload naming that causes parsing discrepancies.

## Consequences

Positive:

- Reduced PR debate on casing.
- No client migrations required.
- Predictable serialization mapping.

Negative / Risks:

- Divergence between Java property names and serialized names requires vigilance.
- Future migration (if ever desired) still a discrete project.

## Alternatives Considered

1. Immediate migration to `camelCase` – high churn, breaking risk.
2. Use `snake_case` in Java – violates ecosystem norms and tool assumptions.
3. Opportunistic mixed approach – leads to long-term inconsistency.

## Implementation Plan

- [x] Convention documented.
- [] Linter/static rule to flag accidental `camelCase` in serialized names (Optional).

## Rollback Plan

If a future major API version introduces contract versioning with automated field renaming, supersede with a migration
ADR.

## Metrics / Validation

- Zero accidental casing diffs in OpenAPI snapshots.
- Reduced style-related review comments over time.

## Open Issues

- Generate mapping report for large DTO sets (backlog).

## References

- Google Java Style Guide
- Existing OpenAPI history

