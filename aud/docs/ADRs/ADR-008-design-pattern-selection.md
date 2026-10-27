# ADR-008: Design Pattern Selection – Service vs Builder vs Factory

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Contributors require clear guidance on when to apply Service, Builder, or Factory patterns. Inconsistent or premature
pattern selection leads to over-abstraction, reduced readability, and maintenance drag. This ADR establishes global
criteria across modules.

## Problem / Forces

- Overuse of builders for simple aggregates inflates code.
- Ad hoc factories duplicate DI/container capabilities.
- Service classes risk growing beyond cohesive responsibility.
- Need a shared mental model for reviewers.

## Decision

Selection criteria:

1. Service Pattern - Orchestrates multi-entity or cross-boundary operations (IO coordination, policy enforcement). -
   MUST remain stateless - Naming: `<Noun>Service` describing domain action (`NetworkRuleAssemblyService`). Avoid vague
   suffixes (`Manager`, `Helper`).
2. Builder Pattern
   - Use for constructing complex immutable objects with ≥ 4 optional parameters OR mutually exclusive configuration
     branches.
   - Avoid for simple aggregates where a clear constructor or factory suffices.
   - Result objects MUST be immutable post-build.
   - Prefer static nested `Builder` inside target type unless reused widely.
3. Factory Pattern
   - Use when instantiation depends on runtime polymorphism (type discriminator, feature flag, provider selection) and
     would otherwise produce branching at call sites.
   - Return interface/abstract type; do not introduce factories wrapping a single trivial `new`.
   - Prefer strategy maps / enums over long conditional chains when feasible.
4. Anti-Patterns
   - Factories that just call `new` with fixed args (inline instead).
   - Builders for DTOs mirroring external payloads without complexity.
   - Services exceeding cohesion (soft signal: >7 injected dependencies ⇒ refactor discussion; see ADR-007 Constructor
     DI policy).

## Rationale

- Encourages deliberate abstraction vs defaulting to patterns.
- Reduces boilerplate and improves maintainability.
- Aligns naming and responsibilities across modules.

## Consequences

Positive:

- Faster, clearer code reviews.
- Lower accidental complexity.
- Predictable layering boundaries.

Negative / Risks:

- Threshold debates (requires team calibration).
- Under-abstraction risk if future variability emerges (mitigated by willingness to refactor; pattern adoption remains
  incremental).

## Alternatives Considered

1. No formal criteria – style drift and inconsistent abstractions.
2. Mandatory builders for all aggregates – excessive ceremony.
3. Universal factories behind interfaces – proliferation of trivial types.

## Implementation Plan

- [x] Codify criteria (this ADR in global set).
- [] Add reviewer checklist snippet in internal wiki.
- [] Provide example pattern implementations (`examples/patterns/`) (future).

## Rollback Plan

If future language/framework features (records, pattern matching) simplify construction/orchestration, revisit
thresholds and possibly supersede with revised guidance.

## Metrics / Validation

- Ratio of builders to genuinely complex immutable types ≈ 1:1.
- <10% of factories with single unconditional branch (target for refactor or removal).
- Constructor parameter count distribution monitored for service cohesion signals.

## Open Issues

- ArchUnit rule to flag services exceeding dependency threshold.
- Evaluate records for reducing builder need.

## References

- ADR-007 Constructor-Based Dependency Injection
- API ADR-002 (example of caching rationale referenced for selective state)

