# Example Data

This directory contains the YAML fixtures used by Yggy.
The subdirectories group related descriptors by scope and by model area.

## Directory layout

- `Scope/`
  - Contains the source `Scope` objects for the repository example data.
  - Each document uses `kind: Scope` and its `metadata.name` is the scope name.
  - The scope name is also used to decide whether a matching fixture directory should be included when generating `network-data`.

- `Application-1/` and `Application-2/`
  - Application-level fixtures.
  - These files describe application descriptors and their related instances.
  - They are grouped by the scope name defined in `Scope/`.

- `GlobalDefinitions/`
  - Shared reference data used by multiple scopes.
  - This folder contains global descriptors such as regions, providers, roles, and other common model objects.

- `Infra_Azure/` and `Infra_GCP/`
  - Network and infrastructure fixtures for cloud-specific network scopes.
  - These files describe objects such as landing zones, targets, subnets, and zone instances.
  - The folder names are scope directories; infrastructure object `metadata.scope` values can be provider-specific (for example `Network_Azure` and `Network_GCP`).

- `network-data/`
  - Generated output used as an aggregated view of the fixtures.
  - This directory is not meant to be edited by hand.
  - It is recreated by the generator script described below.

## Generated files

### `network-data/metadata.yml`

This file contains `ScopeVersionMapping` documents generated from the scope names discovered in `Scope/`.

Each document maps a scope to the network-data version information, for example:

```yaml
---
api_version: v1
kind: ScopeVersionMapping
metadata:
  name: Application-1_1.0.1
  scope: Application-1
spec:
  scope_version: 1.0.1
  network_version: 1.0.1
  version_datetime: 2026-07-02T12:00:00Z
```

Rules:

- One document is written per scope that has a matching directory under `example-data/`.
- The file is rebuilt on each run of the generator script.
- Existing entries are not meant to be edited manually.
- `version_datetime` is generated at runtime in UTC.

### `network-data/api.yml`

This file is an aggregated YAML stream built from the fixture files inside each scope directory.

Rules:

- The generator reads every `.yml` and `.yaml` file inside each scope directory.
- All YAML documents from those files are copied into `api.yml`.
- The file is rebuilt on each run of the generator script.
- It is a generated artifact and should not be edited manually.

### `network-data/release.yml`

This file contains `ScopeReleaseNote` documents generated from the scope names discovered in `Scope/`.

Each document maps a scope version to information related to the included commits and pull requests, for example:

```yaml
api_version: v1
kind: ScopeReleaseNote
metadata:
  name: Infra_Azure
  scope: Infra_Azure
spec:
  version_datetime: 2026-07-29T13:08:12Z
  scope_version: 1.0.1
  commits:
  - commitid: 42
    message: initial commit
  prs:
  - '1'
  impacted_devices: []
```

Rules:

- One document is written containing all scopes listed inside of `example-data/Scope`.
- The file is rebuilt on each run of the generator script.
- Existing entries are not meant to be edited manually.
- `version_datetime` is generated at runtime in UTC.

### `network-data/requirements.yml`

This file contains a single list of requirements generated from the scope names discovered in `Scope/`.

Each document maps a scope version to information related to the included files and the originating git repository
inside of a fictitious project, for example:

```yaml
requirements:
  - name: Application-1
    scope: Application-1
    comment: Application-1
    repo: projects/YGGY/repos/auds-Application-1/raw/
    files:
      - Application-1.yml
    version: 1.0.1
```

Rules:

- One document is written containing fictitious requirements data for all scopes listed inside of `example-data/Scope`.
- The file is rebuilt on each run of the generator script.
- Existing entries are not meant to be edited manually.

## How to generate `network-data`

Run the generator from the `aud/` directory, naming this directory:

```bash
just generate-network-data example-data
```

What the script does:

1. Reads all `Scope` documents from `Scope/`.
2. Extracts the scope names from `metadata.name`.
3. Keeps only scopes that have a matching directory under `example-data/`.
4. Creates or refreshes `network-data/release.yml`, `network-data/requirements.yml`, `network-data/metadata.yml`.
5. Rebuilds `network-data/api.yml` from the matching scope directories.

## Notes

- Do not edit files under `network-data/` by hand unless you are intentionally changing the generator output.
- If you add a new scope, make sure there is a matching directory under `example-data/` so it is included in the generated output.
- If you add or rename fixture files inside a scope directory, rerun the generator script so `api.yml` stays in sync.
