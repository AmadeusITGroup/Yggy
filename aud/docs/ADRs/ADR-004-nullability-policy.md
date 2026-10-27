# ADR-004: Nullability Policy (Implicit Non-Null, Explicit @Nullable)

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Null handling defects are a common source of runtime failures (`NullPointerException`). The project adopts a clear
discipline: everything is non-null unless explicitly marked otherwise using `jakarta.annotation.Nullable`. Automatic
formatting removes stray `@Nonnull` to avoid redundant noise.

## Problem / Forces

- Over-annotation causes cognitive load and PR churn.
- Under-annotation conceals optional semantics, shifting failures to runtime.
- Need interoperable annotations for potential static analysis later.

## Decision

1. All parameters, return values, and fields are assumed non-null by default.
2. Apply `@Nullable` wherever `null` is a legitimate, modelled value.
3. Avoid combining `Optional<T>` with `@Nullable`; prefer one semantic (only return `Optional<T>` when call-site
   chaining benefits outweigh allocation).
4. Wrap / document library boundaries that may yield `null` at the earliest adaptation point.
5. @jakarta.validation.constraints.NotNull can be kept, but only for hibernate validation (exceptional cases)

## Rationale

- Keeps the common (non-null) case lightweight.
- Makes optionality deliberate and reviewable.
- Eases future adoption of null-safety tooling.

## Consequences

Positive:

- Reduced unexpected NPEs.
- Clearer API contracts at boundaries.
- Easier static auditing.

Negative / Risks:

- Potential misuse where empty container preferable.
- Requires occasional audits to ensure consistent application.

## Alternatives Considered

1. Explicit `@Nonnull` everywhere – verbosity & maintenance cost.
2. Universal `Optional<T>` for nullable returns – allocation + awkward for fields.
3. No policy – inconsistent semantics across modules.

## Implementation Plan

- [x] Policy documented in global ADR set.
- [] Add static analysis rule (future) for `Optional` + `@Nullable` misuse.
- [] Provide examples in developer wiki.

## Rollback Plan

If a future null-safety framework mandates explicit non-null annotation, supersede with migration ADR.

## Metrics / Validation

- Decrease/stability of NPE occurrences in logs.
- Sampling audits show low omission rate for expected `@Nullable` spots.

## Open Issues

- Evaluate JetBrains / ErrorProne nullability tooling integration.

## References

- `jakarta.annotation.Nullable` Javadoc

