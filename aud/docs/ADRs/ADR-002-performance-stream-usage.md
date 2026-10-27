# ADR-002: Performance Guidance – Default Ban on Java Streams (Exception-Only Policy)

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

The system is latency sensitive and often processes small collections per request (e.g., subnets, relationships, rule
fragments). Micro-benchmarks show naïve Java Stream usage adds avoidable allocation, lambda indirection, and sometimes
inhibits straightforward branch elimination compared to classic loops—especially for small N. Prior guidance allowed
Streams under tightly constrained conditions; in practice this created review ambiguity and recurrent debate.

## Problem / Forces

- Stream pipeline setup overhead dominates for small collections.
- Reduced debuggability vs imperative loops (stack traces & step-through).
- Risk of accidental intermediate materialization increasing GC pressure.
- Inconsistent reviewer interpretation of "allowed" scenarios increases friction.
- Desire for a simple, enforceable rule that protects hot paths by default.

## Decision

Adopt a DEFAULT BAN on the Java Stream API (sequential and parallel) in production code (main sources) across all
modules.

A Stream usage MAY be introduced ONLY via an approved exception recorded in the PR description and meeting ALL the
criteria below:

1. Benchmark Evidence: Provide JMH (or equivalent isolated micro-benchmark) results comparing loop vs proposed Stream
   form, demonstrating ≥ (a) 5% throughput improvement OR (b) measurable allocation reduction (≥ 5%) on realistic input
   sizes. Include benchmark source or link.
2. Input Size Justification: Show representative data volume (N) and why it invalidates the small-N overhead concern.
3. Complexity Justification: Demonstrate that the Stream version materially improves correctness clarity (e.g.,
   eliminates error-prone mutation) beyond cosmetic chain fluency.
4. Safety: No intermediate collection materialization unless justified (e.g., `Collectors.groupingBy`).

Parallel Streams: DISALLOWED unless a separate ADR explicitly authorizes a parallel processing model (none at present).

Any unapproved Stream usage found in code review MUST be refactored to a loop prior to merge.

## Rationale

- Eliminates subjective debates about marginal cases; default = loop.
- Protects performance characteristics for predominantly small-N collections.
- Simplifies code reviews (presence of Stream instantly signals exception check list).
- Encourages disciplined benchmarking culture before accepting abstraction overhead.

## Consequences

Positive:

- Predictable micro-performance (allocation + CPU) on iteration paths.
- Reduced cognitive load in reviews; binary rule.
- Cleaner flame graphs (fewer lambda frames) aiding profiling.
- Encourages explicit naming & intermediate variable inspection in loops.

Negative / Risks: - Potential verbosity for legitimate transformation pipelines (mitigated by documented exception
path). - Might discourage experimentation; mitigated by low-friction benchmark template to be added. - Risk of local
re-implementation of map/filter logic; mitigated by small helper methods (imperative) where needed.

## Alternatives Considered

1. Prior "restrained use" policy – led to recurring ambiguity and uneven enforcement.
2. Total / permanent ban with zero exceptions – overly rigid; could block provable gains for large-N or complex grouping
   cases.
3. Static analysis auto-ban (ErrorProne rule) – deferred until exception annotation mechanism is defined.
4. Case-by-case subjective review without benchmarks – lacks rigor; prone to drift.

## Implementation Plan

- [x] Replace prior conditional allowance with explicit ban (this ADR).
- [] Add JMH benchmark template under `perf/stream-exceptions/`.
- [] Introduce optional build profile to run micro-benchmarks for submitted exceptions.
- [] Add ArchUnit / ErrorProne (future) rule: flag `stream()` / `parallelStream()` outside allowlisted packages or with
  missing PR reference comment.
- [] Create wiki page: "Requesting a Stream Exception" linking to template benchmark.

## Rollback Plan

If future JVM/JIT improvements or profiling evidence show negligible overhead of Streams for small-N typical workloads
(sustained across at least 2 JDK releases), revisit with a new ADR possibly relaxing to the earlier "restrained use"
policy.

## Metrics / Validation

- Count of approved Stream exceptions (target: low & explicitly tracked; add a lightweight script to grep for PR marker
  comments).
- Allocation rate (bytes/op) on key endpoints remains stable or improved release-over-release.
- Benchmark artifacts attached to exceptions are reproducible (spot-audit quarterly).
- Zero unapproved Stream usages detected in periodic scans.

## Open Issues

- Define directory & naming convention for benchmark artifacts (Proposed:
  `benchmarks/streams/<adr002-exception-<feature>>`).
- Decide on automated CI check to reject unapproved Stream usage (pending tool selection).
- Establish rotation & documentation for "performance steward" role.

## References

- [Streams vs. Loops in Java](https://www.baeldung.com/java-streams-vs-loops)
- [Java Stream and For Loop: A Balanced View on Performance](https://medium.com/@ezzeddinebilel/java-stream-and-for-loop-a-balanced-view-on-performance-a38e45f271e5)
- [Speed of Java stream](https://medium.com/@daniel.las/speed-of-java-stream-1cc3a94b44c2)

