# API Service (`service-http-apis`)

The v1 HTTP API service. It serves the AUD model over REST and GraphQL, backed by the data engine and the in-memory or
file-backed graph.

## Running it

See the [module README](../../README.md) for the prerequisites and the two supported ways to start it:

- `just start-api-dev` — Quarkus dev mode, needs Azure Storage credentials (copy `.env.example` to `.env.local` first)
- `just start-api-local <data-dir>` — container build reading from a local data directory, no credentials needed

Once started, the service is on <http://localhost:8080> and exposes:

- OpenAPI document: <http://localhost:8080/openapi>
- Swagger UI: <http://localhost:8080/openapi-ui>
- Health checks: <http://localhost:8080/q/health>

