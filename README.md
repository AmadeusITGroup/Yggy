# Yggy

**Yggy** is an open-source framework for managing network connectivity in hybrid
and multi-cloud environments through abstract connectivity descriptors. Instead
of manually configuring disparate infrastructure components, Yggy lets you
declare your connectivity intent — and makes that declaration the source of
truth for your entire network.

## The Problem

Modern infrastructure spans multiple cloud platforms (AWS, Azure, Google Cloud),
private data centers, and edge devices — each with its own configurations,
protocols, and security policies. Managing connectivity across this
heterogeneous landscape is complex, error-prone, and difficult to scale.
Documentation lags behind implementation, configurations drift, and there is no
unified model to reason about cross-platform connectivity.

## Key Features

- **Descriptor-Driven Model** — Abstract representations of network resources,
  configurations, security policies, and connectivity intent that are
  infrastructure-independent.
- **Multi-Cloud Support** — A unified model for Azure VNets/NSGs, Google
  VPC/Firewalls, Palo Alto Firewalls, OpenShift OVN Network Policies, and more.
- **Intent-Based Networking** — Express desired connectivity outcomes
  (performance, availability, security) and let the system translate intent into
  platform-specific configurations.
- **Declarative Connectivity** — The descriptor IS the documentation. Flip the
  script: instead of action followed by documentation, make the documentation
  the action.
- **Integration Ecosystem** — Designed to plug into Kratix, Humanitec, Backstage,
  Terraform, and other automation orchestration tools.
- **Microservice Architecture** — Modular, extensible API built with Quarkus,
  with a pluggable provider model for new infrastructure types.

> [!NOTE]
> The list above describes the architecture Yggy is built towards. **This release
> ships one provider integration — Azure network security groups — and no
> orchestrator integrations.** What is published is the v1 codebase in
> [`aud/`](./aud): the model and data libraries, the Azure NSG provider, and two
> Quarkus services. See
> [Architecture](https://AmadeusITGroup.github.io/Yggy/system-design/architecture)
> for the full scope note.

## Documentation

📖 **[Open the Wiki](https://AmadeusITGroup.github.io/Yggy/)** — Full
documentation hosted on GitHub Pages.

| Document | Description |
|----------|-------------|
| [Why Yggy?](https://AmadeusITGroup.github.io/Yggy/understanding/why-yggy) | The problem Yggy solves and the declarative connectivity approach |
| [Core Concepts](https://AmadeusITGroup.github.io/Yggy/understanding/concepts) | Descriptors, intent, topology, and declarative connectivity |
| [Architecture](https://AmadeusITGroup.github.io/Yggy/system-design/architecture) | System architecture, services, storage, and integration points |
| [Change Flow](https://AmadeusITGroup.github.io/Yggy/system-design/change-flow) | How changes flow through the system end-to-end |

## V1

The code in the `aud` directory represents the first version used to showcase
those concepts, and is what this release contains.

A simpler and improved second version is under active development. It is not part
of this release and will be published progressively.

## Getting started

Requirements, build commands and the two ways to run the services locally are in
[`aud/README.md`](./aud/README.md). The commands are recipes run by
[`just`](https://github.com/casey/just), so install that first — there is a package
for it on
[every common platform](https://just.systems/man/en/packages.html). In short, from
the `aud` directory:

```shell
just setup   # install the expected Java and Maven versions via sdkman
just test    # run the unit tests
```

The documentation above describes the architecture Yggy is built towards; see the
scope note on the
[Architecture](https://AmadeusITGroup.github.io/Yggy/system-design/architecture)
page for what this release actually contains.

## License

This project is licensed under the Apache License Version 2.0. See [LICENSE](./LICENSE).

## Contributing

> [!IMPORTANT]
> This repository is source-available but **not open to pull requests**. Development
> happens in a private repository and this one is a read-only mirror of it, so an
> external commit cannot be merged here.

**Bug reports are welcome and are triaged by the team** — please
[open an issue](https://github.com/AmadeusITGroup/Yggy/issues). For a suspected
vulnerability, follow [SECURITY.md](./SECURITY.md) instead of opening a public
issue.

See [CONTRIBUTING.md](./CONTRIBUTING.md) for what to include in a report and what
the Apache-2.0 licence still allows you to do with this code.
