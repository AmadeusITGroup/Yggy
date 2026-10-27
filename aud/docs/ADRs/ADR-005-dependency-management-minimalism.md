# ADR-005: Dependency Management Minimalism

- Status: Accepted
- Date: 2025-09-23
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Excess dependencies increase attack surface, transitive CVE exposure, classpath size, build times, and cognitive
overhead. The project targets a lean runtime footprint relying primarily on Quarkus extensions and a controlled set of
foundational libraries.

## Problem / Forces

- More dependencies ⇒ higher maintenance (updates, CVE triage, licenses).
- Version drift without central management ⇒ inconsistent runtime behavior.
- Large transitive trees for small utilities inflate footprint.
- Desire for rapid, batched dependency updates.

## Decision

1. New third‑party dependency additions require justification in PR (capability gap).
2. All versions defined once in parent `<properties>`; modules reference properties only.
3. Prefer Quarkus extensions over direct libs when available (metrics, caching, REST, JSON).
4. Run `scripts/updateJavaDependencies.java` at least monthly; batch PR with summarized changelog / CVE notes.
5. Avoid overlapping utility libraries (e.g., choose JDK / existing collections helpers).
6. Remove unused dependencies immediately when code removed/refactored.

Good example:

```java
public class MyClass {
  private final MyDependency myDependency;
  private final MyOtherDependency myOtherDependency;

  @Inject
  public MyClass(MyDependency myDependency, MyOtherDependency myOtherDependency) {
    this.myDependency = myDependency;
    this.myOtherDependency = myOtherDependency;
  }
}
```

Bad example:

```java
public class MyClass {
  @Inject MyDependency myDependency;
  @Inject MyOtherDependency myOtherDependency;
}
```

## Rationale

- Central control simplifies coordinated upgrades.
- Shrinks vulnerability surface and cognitive load.
- Predictable build + runtime characteristics.

## Consequences

Positive:

- Smaller, clearer dependency graph.
- Faster patching for security events.
- Reduced classpath conflicts.

Negative / Risks:

- Slight friction adding niche utilities.
- Occasional reimplementation of trivial helpers.

## Alternatives Considered

1. Free-form additions – fast initially, chaotic later.
2. Automated individual Dependabot PRs – PR noise / review fatigue.
3. Lockfile (not standard for Maven) – limited incremental benefit over properties.

## Implementation Plan

- [x] Parent properties block established.
- [] CI script to detect hardcoded versions (future).
- [] Periodic dependency report (`mvn dependency:tree`).

## Rollback Plan

Temporary module-level override allowed ONLY with tracking issue; must revert within one release cycle.

## Metrics / Validation

- Module dependency counts stable or decreasing.
- Patch latency for high CVE severity < 14 days.
- Zero duplicate libraries offering identical utility.

## Open Issues

- Integrate OWASP Dependency Check / Trivy.
- Generate SBOM (CycloneDX) during release.

## References

- `scripts/updateJavaDependencies.java`
- Quarkus extension catalog
- Maven central best practices

