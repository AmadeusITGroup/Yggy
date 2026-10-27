<!-- AUTO-GENERATED FILE TEMPLATE: Do not edit generated docs/ADRs.md directly.
Edit this template (docs/templates/ADRs.index.template.md) and run one of:
Rows are injected where the placeholder ADR_ROWS appears. -->

# Global Architecture Decision Records

These ADRs apply across the entire repository (all modules). Module-specific ADRs (e.g., API-focused decisions) remain
in their respective module `docs/ADRs` directories.

|                             ADR                             |                                   Title                                    |  Status  |
|-------------------------------------------------------------|----------------------------------------------------------------------------|----------|
| [ADR-001](ADRs/ADR-001-compiler-warnings-as-errors.md)      | Compiler Warnings Treated as Errors                                        | Accepted |
| [ADR-002](ADRs/ADR-002-performance-stream-usage.md)         | Performance Guidance – Default Ban on Java Streams (Exception-Only Policy) | Accepted |
| [ADR-003](ADRs/ADR-003-naming-conventions.md)               | Naming Conventions (Code and Serialized Formats)                           | Accepted |
| [ADR-004](ADRs/ADR-004-nullability-policy.md)               | Nullability Policy (Implicit Non-Null, Explicit @Nullable)                 | Accepted |
| [ADR-005](ADRs/ADR-005-dependency-management-minimalism.md) | Dependency Management Minimalism                                           | Accepted |
| [ADR-006](ADRs/ADR-006-test-classification.md)              | Test Classification (Unit vs Integration) and Naming Policy                | Accepted |
| [ADR-007](ADRs/ADR-007-constructor-dependency-injection.md) | Constructor-Based Dependency Injection Policy                              | Accepted |
| [ADR-008](ADRs/ADR-008-design-pattern-selection.md)         | Design Pattern Selection – Service vs Builder vs Factory                   | Accepted |
| [ADR-009](ADRs/ADR-009-avoid-var-keyword.md)                | Prohibit Use of `var` (Local Variable Type Inference)                      | Accepted |
| [ADR-010](ADRs/ADR-010-unit-tests-formats-and-libraries.md) | Unit-tests formats and libraries                                           | Accepted |
| [ADR-011](ADRs/ADR-011-http-service-three-layer-split.md)   | HTTP Service Three-Layer Module Split                                      | Accepted |
| [ADR-012](ADRs/ADR-012-maven-module-split.md)               | Maven Module Split                                                         | Accepted |

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

