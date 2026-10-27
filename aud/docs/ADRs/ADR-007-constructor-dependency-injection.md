# ADR-007: Constructor-Based Dependency Injection Policy

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

The codebase uses Quarkus CDI for wiring services. Injection styles (field, constructor, method) were inconsistently
applied in prior projects, obscuring required dependencies, enabling non-final mutable fields, and complicating manual
instantiation in tests.

## Problem / Forces

- Field injection hides true dependency set and prevents `final` qualifiers.
- Method injection adds lifecycle complexity and ordering concerns.
- Inconsistent styles raise review friction and reduce readability.
- Explicit test construction is harder when dependencies are implicit.

## Decision

Adopt constructor-only dependency injection:

1. A single constructor annotated with `@Inject` (explicit even if Quarkus infers) lists all required collaborators.
2. Store dependencies in `private final` fields.
3. No field-level `@Inject` (exceptions require justification note in PR).
4. Method injection discouraged except minimal `@PostConstruct` initialization that cannot occur inside the constructor
   (keep trivial).
5. Large constructor parameter counts (>7) trigger refactor discussion (possible responsibility split or aggregate
   parameter object).

## Rationale

- Improves clarity: dependency list is explicit API surface.
- Enables immutability and thread-safety via `final` fields.
- Simplifies isolated unit testing (manual construction + mocks/spies).
- Makes circular dependencies immediately evident at compile time.

## Consequences

Positive:

- Predictable bean structure and simpler review.
- Fewer runtime surprises from late injection.
- Encourages cohesive service boundaries (parameter count heuristic).

Negative / Risks:

- Slight verbosity vs field injection.
- Optional dependencies require design (Provider<T> or separate service) instead of ad hoc nullability.

## Alternatives Considered

1. Field injection – concise but opaque, reduces testability.
2. Mixed styles – increases inconsistency and cognitive burden.
3. Method injection – rarely necessary; adds lifecycle complexity.

## Implementation Plan

- [x] Policy documented via global ADR set.
- [] Add static / ArchUnit rule to flag field injection (future).
- [] Add guidance for handling optional dependencies (Provider / polymorphism).

## Rollback Plan

If future Quarkus versions introduce record-based or auto-constructor DI with enforced immutability, revisit and
potentially simplify (superseding ADR).

## Metrics / Validation

- Zero field injection occurrences in periodic scans.
- Distribution of constructor parameter counts (monitor high outliers).
- Reduced incidence of runtime NPE due to uninitialized injected fields.

## Open Issues

- Formalize parameter count threshold into automated lint (pending).
- Provide internal example module with best-practice patterns.

## References

- Quarkus CDI Guide

