# ADR-006: Test Classification (Unit vs Integration) and Naming Policy

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

The build distinguishes between fast, isolated unit tests (Surefire, `mvn test`) and slower environment / wiring
integration tests (Failsafe, `mvn verify`). Consistent classification protects inner-loop velocity while retaining
confidence from higher-fidelity scenarios.

## Problem / Forces

- Need sub-minute feedback for logic changes.
- Integration tests may start Quarkus, spin containers, or exercise HTTP endpoints.
- Misplaced integration tests inflate routine build time and flakiness.
- Over-mocked unit tests lose realism; under-scoped integration tests hurt diagnosability.

## Decision

1. Unit Tests: Pure JVM; no external network (loopback ephemeral allowed), no sleeping-for-eventual-consistency, no
   container startup. Typical class runtime < 250 ms.
2. Integration Tests: Suffix `*IT.java` AND `@QuarkusIntegrationTest` (or appropriate marker) exercising serialization,
   HTTP calls, CDI wiring, dev services, etc.
3. Avoid mixing unit + integration logic in one class. Split clearly.
4. Shared fixtures/utilities centralized (future `testFixtures` style) to reduce duplication.

## Rationale

- Predictable build duration and CI sizing.
- Clear separation clarifies failure domains (logic vs environment).
- Encourages layered confidence (unit correctness precedes integration wiring).

## Consequences

Positive:

- Faster iteration cycles.
- Easier isolation of flaky behavior.
- Improved signal quality in CI dashboards.

Negative / Risks:

- Borderline cases require reviewer judgment.
- Potential fragmentation if logic never receives integration coverage.

## Alternatives Considered

1. Single unified test phase – slower feedback, noisy failures.
2. Quarkus integration tests for everything – unnecessary overhead.
3. Heavy contract tests only – poor defect localization.

## Implementation Plan

- [x] Policy documented via global ADRs.

## Rollback Plan

If integration overhead becomes negligible (pure in-memory architecture), consider merging phases and superseding this
ADR.

## Metrics / Validation

- Stable / decreasing median unit test phase time.
- Reduced flaky test count after reclassification.

## Open Issues

- Define standard annotation / tag for external-call tests.
- Evaluate TestContainers scope boundaries.

## References

- Surefire / Failsafe plugin docs
- Quarkus testing guide

