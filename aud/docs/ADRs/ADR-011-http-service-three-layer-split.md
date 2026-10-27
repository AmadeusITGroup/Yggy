# ADR-011: HTTP Service Three-Layer Module Split

- Status: Accepted
- Date: 2026-08-31
- Deciders: AUD Engineering Team
- Supersedes: None

## Context

HTTP-based features in the AUD platform were initially developed inside the monolithic `service-http-apis` module. As
the codebase grew, this created a single large artifact with mixed concerns: domain models, business logic, GraphQL
providers, caching, and JAX-RS endpoints all lived together. Testing a service required booting the full Quarkus
application context, and reuse across services (e.g., the connectivity engine) was impossible without pulling in the
entire HTTP API module.

The Azure NSG feature was the first to be extracted into a clean three-layer split: `lib-azure-nsg-api`,
`lib-azure-nsg-service`, and `service-azure-nsg-http`. This ADR formalises that pattern as the standard for all new HTTP
features and for extracting existing ones.

## Problem / Forces

- Monolithic service modules accumulate unrelated dependencies and slow down builds.
- Business logic tangled with HTTP concerns cannot be reused by other services or consumed as a library.
- Unit-testing a service class should not require a running Quarkus container.
- Port interfaces allow multiple adapters (REST, GraphQL, CLI) to share the same service layer.
- Too many layers add ceremony; too few create coupling.

## Decision

Every HTTP feature area MUST be split into three Maven modules following this naming convention:

|      Layer       |      Module name      | Parent aggregator |                                                                                                 Contains                                                                                                 |
|------------------|-----------------------|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Ports**        | `lib-<name>-api`      | `libraries/`      | Public models (records, value objects) and port interfaces. Zero business logic, zero framework coupling beyond Mutiny `Uni` return types.                                                               |
| **Service**      | `lib-<name>-service`  | `libraries/`      | Business logic, caching, GraphQL providers, and service implementations of the port interfaces. May depend on `lib-<name>-api` and other `lib-*` modules. Indexed by Jandex for CDI discovery.           |
| **HTTP adapter** | `service-<name>-http` | `services/`       | Quarkus JAX-RS resources, OpenAPI annotations, and HTTP-specific mapping. Depends on both `lib-<name>-api` (for models) and `lib-<name>-service` (for implementations). This is the deployable artifact. |

Dependency direction is strictly **ports ← service ← http**. The HTTP adapter never leaks into libraries. The ports
module never depends on the service module.

### Reference implementation

The Azure NSG split serves as the canonical example:

```
lib-azure-nsg-api
├── models/          # NetworkConfig, NetworkSecurity, NetworkRule, …
└── ports/           # NetworkConfigPort, LandingZoneNetworkSecurityPort

lib-azure-nsg-service
├── services/        # NsgService, NetworkService, LandingZoneService, LandingZoneUtils
└── graphql/         # LandingZoneNetworkSecurityGraphQLProvider, NetworkSecurityFilter, …

service-azure-nsg-http
└── http/            # NetworkResource, LandingZoneResource (JAX-RS endpoints)
```

### Rules

1. **Port interfaces** live in `lib-<name>-api`. They define the contract using domain models from the same module.
   Return types are `Uni<T>` where `T` is a model from the api module.
2. **Service implementations** live in `lib-<name>-service`. They implement the port interfaces and contain all business
   logic, caching, fault tolerance, and GraphQL providers. This module declares a Jandex index so CDI discovers its
   beans. No Http classes in here.
3. **HTTP resources** live in `service-<name>-http`. They inject port interfaces, delegate to them, and handle HTTP
   mapping (OpenAPI annotations, URL rewriting, response wrapping). They MUST NOT contain business logic.
4. A service module MAY depend on other `lib-*` modules (e.g., `lib-data-engine`, `lib-graph-engine`) but MUST NOT
   depend on any `service-*` module.
5. The api module SHOULD have minimal dependencies: domain models, `Uni`, and shared libraries like `lib-common` or
   `lib-models`.

## Rationale

- **API isolation** means the public API contract is defined once and can be consumed by multiple adapters without
  pulling in implementation details.
- **Service as a library** enables reuse: another Quarkus service can depend on `lib-<name>-service` without inheriting
  HTTP endpoints.
- **HTTP as a thin adapter** keeps endpoint code focused on REST concerns (routing, serialisation, OpenAPI docs) and
  trivially testable with `@QuarkusTest`.
- **Jandex indexing** in the service module ensures CDI discovers beans when the library is included transitively.
- Three layers is the right trade-off: two layers would merge ports and service (losing reusability of the contract),
  four layers would add unnecessary ceremony.

## Consequences

Positive:

- Smaller, focused modules with faster incremental builds (and more parallelism),
- Business logic is unit-testable without a Quarkus container.
- Port interfaces enable multiple consumers (REST, GraphQL, CLI or gRPC adapters).
- Clear dependency direction prevents circular or accidental coupling.
- Existing monolith modules can be extracted incrementally, one feature at a time.

Negative / Risks:

- More Maven modules increase reactor complexity.
  - Mitigation: the naming convention makes navigation predictable; the three modules always appear as a cohesive group.
- Extracting existing features from `service-http-apis` requires careful refactoring.
  - Mitigation: extract one feature at a time, validate with `just test`, and keep the old code until the new modules
    are wired in.
- Ports module could become a dumping ground for unrelated models.
  - Mitigation: one ports module per feature area; do not share ports modules across unrelated features.

## Alternatives Considered

1. **Keep everything in `service-http-apis`** – simplest, but the module keeps growing and nothing is reusable as a
   library. Rejected because it was already causing build and testing pain.
2. **Two layers only (lib + service)** – merge ports and service into a single library. Simpler, but loses the ability
   to depend on just the contract without pulling in all implementation dependencies (caching, fault tolerance,
   GraphQL). Rejected for coupling reasons.
3. **Four layers (ports, domain, service, http)** – separate domain logic from service orchestration. Adds a module for
   limited benefit given the current codebase size. Can be reconsidered if a domain layer grows complex enough to
   justify it.
4. **Hexagonal with dedicated adapter modules per protocol** – full ports-and-adapters with separate GraphQL and REST
   adapter modules. Over-engineered for the current scale where each feature has at most two adapters. Does not work
   well with GraphQL annotations-based APIs.

## Implementation Plan

- [x] Extract Azure NSG into `lib-azure-nsg-api`, `lib-azure-nsg-service`, `service-azure-nsg-http`.
- [] Apply the same split to the next feature extracted from `service-http-apis`.
- [] Document the pattern in the repository README under "Repository layout".
- [] All new HTTP features follow this pattern from day one.

## Rollback Plan

If the three-layer split proves too granular for a specific feature (e.g., a feature with a single endpoint and no
reuse), the service and ports modules can be merged back into a two-layer structure (`lib-<name>` +
`service-<name>-http`) without breaking consumers, since the HTTP adapter already depends on both.

## Metrics / Validation

- Each new HTTP feature area produces exactly three modules following the naming convention.
- No `service-*` module appears as a dependency of a `lib-*` module.
- Port interfaces are the only compile-time dependency between HTTP resources and business logic.
- Unit tests in `lib-<name>-service` run without `@QuarkusTest`.

## Open Issues

- Determine the extraction order and granularity for features currently in `service-http-apis`.
- Evaluate whether `lib-backend-apis` and `lib-graphql` should follow this pattern or remain shared.

## References

- Azure NSG modules: `lib-azure-nsg-api`, `lib-azure-nsg-service`, `service-azure-nsg-http`
- ADR-005 Dependency Management Minimalism
- ADR-007 Constructor-Based Dependency Injection
- ADR-008 Design Pattern Selection

