# ADR-009: Prohibit Use of `var` (Local Variable Type Inference)

- Status: Accepted
- Date: 2025-09-28
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Java 10 introduced `var` for local variable type inference. While it can reduce verbosity, it also hides explicit type
information. Our codebase emphasizes readability, clarity of intent, explicitness around nullability (see
[ADR-004](ADR-004-nullability-policy.md)), and consistency to aid code review and static analysis. Increasing use of
`var` can lead to: - Reduced readability for newcomers and cross-team reviewers. - Harder reasoning about generic types
when reading code without IDE tooling. - Ambiguity in overloading / numeric types (e.g., `var x = 0;`). - Inconsistent
style—some modules use explicit types, others rely on inference.

We value long-term maintainability and auditability over brevity. Explicit types also surface unintended widening /
narrowing and improve searchability (e.g., finding all `Map<String, Object>` usages).

## Problem / Forces

- Desire for concise code vs. need for explicit intent.
- Developer convenience vs. reviewer clarity across distributed teams.
- Reliance on IDE tooling vs. readability in diffs, code reviews, and security audits.
- Potential future static analysis / code generation tools that depend on explicit types.

## Decision

Disallow the use of `var` for local variable declarations in production source (`src/main`) and test source (`src/test`)
across all Java modules in this repository.

All local variables MUST declare their explicit type (e.g., `List<String> users = ...;`).

## Rationale

- Improves immediate comprehensibility without IDE hover or navigation.
- Prevents accidental broad types (e.g., `var list = new ArrayList<>();` losing declared interface intent vs.
  `List<String> list = new ArrayList<>();`).
- Reduces subtle bugs where inferred numeric / generic types differ from expectation.
- Aligns with existing ADRs favoring explicitness ([ADR-003 Naming Conventions](ADR-003-naming-conventions.md),
  [ADR-004 Nullability Policy](ADR-004-nullability-policy.md),
  [ADR-006 Test Classification](ADR-006-test-classification.md)).
- Enhances security / compliance reviews where tooling or offline diff review is common.
- Simplifies onboarding for engineers less familiar with advanced type inference.

## Consequences

Positive:

- Clearer code reviews and diffs.
- Easier onboarding and cross-module navigation.
- Stronger guard rails for maintainability and refactoring safety.
- Improved static analysis precision and future tooling leverage.

Negative / Risks:

- Slightly more verbose code (mitigated by modern IDE templates / live templates).
- Some developers may perceive this as regressive or limiting (mitigated via documentation and rationale).
- Refactors that change the right-hand side type may require updating the left-hand side (mitigated by IDE refactor
  tools).

## Alternatives Considered

1. Allow unrestricted `var` usage.
   - Pros: Less typing, potentially cleaner short expressions.
   - Cons: Inconsistent style, hidden types, harder reviews.
2. Allow `var` only when the type is obvious (e.g., constructor call with full generic signature) or in `for` loops.
   - Pros: Balanced flexibility.
   - Cons: Inconsistent style. Leads to style drift and review debates.
3. Allow `var` only in test code.
   - Pros: Keeps production explicit while giving tests flexibility.
   - Cons: Two styles to learn; test readability still matters.

Option 2 was rejected due to enforcement ambiguity. Option 3 was rejected to maintain a single mental model everywhere.

## Implementation Plan

- [] Add static analysis rule (Checkstyle or Error Prone) to flag `var` usage.
- [] Add a Spotless / formatting ratchet check (if feasible) or custom regex-based verification in CI.
- [] Run repository-wide scan and replace existing `var` occurrences (manual review for correctness).

## References

- [ADR-003 Naming Conventions](ADR-003-naming-conventions.md)
- [ADR-004 Nullability Policy](ADR-004-nullability-policy.md)
- [ADR-006 Test Classification](ADR-006-test-classification.md)
- JEP 286: Local Variable Type Inference (https://openjdk.org/jeps/286)

