# ADR-012: Maven Module Split

- Status: Accepted
- Date: 2026-10-05
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

Maven daemon parallelizes the compilation and test of modules based on their dependencies.

A proper split of interfaces and implementations allows better parallelism and clean code throughout.

This is an extension of [ADR-011](ADR-011-http-service-three-layer-split.md).

## Problem / Forces

- Monolithic service modules accumulate unrelated dependencies and slow down builds.
- Big modules slow down compilation and tests
- Big modules hide the real dependencies needed throughout

## Decision

Every module should be split into 2, when it makes sense:

- `lib-xxx-api`, containing the interfaces and the models required by the interfaces only;
- `lib-xxx`, should depend on `lib-xxx-api`, and implement the required interfaces.

Then, each module that need `xxx` should depend on `lib-xxx-api` at compile time only (default scope):

```xml
<dependency>
  <groupId>com.amadeus.aud.libraries</groupId>
  <artifactId>lib-xxx-api</artifactId>
  <version>${project.version}</version>
</dependency>
```

`lib-xxx` should only be depended on with scope `runtime`, or (in the rarest of cases), at `compile` time if another
library cannot do otherwise (This should almost never be needed).

Eg:

```xml
<dependency>
  <groupId>com.amadeus.aud.libraries</groupId>
  <artifactId>lib-xxx</artifactId>
  <version>${project.version}</version>
  <scope>runtime</scope>
</dependency>
```

## Consequences

Positive:

- Smaller, focused modules with faster incremental builds (and more parallelism),
- Clearer dependencies between modules, and smaller dependencies in each module
- dependency on a non `-api` module immediately raises suspicion

Negative / Risks:

- More Maven modules increase reactor complexity.
  - Mitigation: the naming convention makes navigation predictable; the two modules always appear as a cohesive group.
- Old module may be difficult to split due to poor architecture
- Extending implementations from a `lib-xxx` may require its implementation, and thus break this requirement.
  - Mitigation: do not extend classes for behaviour, get an instance instead via CDI, and use it instead (aka, compose,
    do not inherit).

## Alternatives Considered

Status-quo

