# ADR-001: Compiler Warnings Treated as Errors

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Code quality and long-term maintainability degrade when compiler warnings accumulate (unused code paths, deprecated API
usage, unchecked operations, accidental boxing, etc.). Allowing warnings to persist creates noise, hides real issues,
and delays remediation until refactors are risky. The Maven build configuration enables warnings-as-errors by default
for new modules via the `maven.compiler.failOnWarning` flag.

## Problem / Forces

- Silent technical debt growth when warnings are ignored.
- Risk that important warnings (e.g., raw types, nullness hints) are lost in noise.
- Delayed cleanup increases merge conflict and refactor cost.
- Developers sometimes tempted to globally disable warnings for short-term velocity.

## Decision

All Java modules MUST keep compiler warnings failing the build (`maven.compiler.failOnWarning=true`). Disabling this is
only permitted as a narrowly scoped, temporary exception with justification in the module POM and a linked follow-up
ticket.

## Rationale

- Forces immediate feedback loop; issues are cheap to fix when introduced.
- Maintains a consistently clean signal surface ("no warnings" invariant).
- Encourages deliberate use of generics, nullability, and API evolution.

## Consequences

Positive:

- Higher baseline code hygiene.
- Reduced likelihood of latent runtime issues tied to ignored warnings.
- Easier upgrades (fewer legacy deprecated usages).

Negative / Risks:

- Short-term friction when introducing experimental code (mitigated via suppression on narrow scope, not global flag).
- Potential false positives from toolchain quirks (mitigated by pinning compiler version and documenting suppress
  patterns).

## Alternatives Considered

1. Allow warnings indefinitely – increases entropy, weakens quality bar.
2. Periodic warning cleanup sprints – defers cost, context loss.
3. Selective categories only – introduces inconsistency and debate overhead.

## Implementation Plan

- [x] Default parent POM sets `failOnWarning` true.
- [] Provide examples of acceptable `@SuppressWarnings` usage (follow-up doc section).

## Rollback Plan

If a critical dependency update produces unavoidable compiler noise across modules, temporarily disable for the isolated
module with an issue reference; reinstate within one release cycle.

## Metrics / Validation

- CI build remains warning-free.
- Track occurrences of temporary overrides (target: zero sustained > 1 release).

## Open Issues

- Define approved `@SuppressWarnings` categories catalogue (Pending).

## References

- Maven Compiler Plugin docs
- Parent `pom.xml` configuration

