# Changelog

All notable changes to this project will be documented in this file.

## [v2026-10-27]

### Added

- Provided data validity checker library
- OpenTelemetry tracing is now available in the Connectivity Service
- Added simple Json Schema Http Service
- Added Connectivity Service /health endpoint

### Changed

- Upgraded to Quarkus 3.40.1 and dependencies
- Enhanced edge/node entity indexing and loading
- Split lib-graph-engine to improve parallelism of the build
- Added lib-data-engine-api to improve parallelism of the build
- ScopeVersionDiff response time improvement
- Removed unversioned endpoint from LandingZoneResource and FulllistResource

### Fixed

- Added a Scope Mixin

## [v2026-09-11]

### Added

- Provided example dataset - aud/example-data - Github Issue #3
- First RBAC draft for connectivity engine

### Changed

- Speeded up unit tests
- Upgraded to Quarkus 3.39.2
- Updated Dependencies
- Restructured and enhance benchmarking scenarios

### Fixed

- No startup crash anymore when using an empty dataset - Github Issue #4
- Incorrect OpenAPI annotations and use common 401, 404 and 500 definitions
- Record correct start line number for YAML objects

## [v2026-08-13]

### Added

- Initial Commit
- Provides access to the Azure NSG Service.

### Changed

### Fixed
