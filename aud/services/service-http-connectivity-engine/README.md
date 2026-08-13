# Graph Engine Service (`service-http-connectivity-engine`)

## Overview

The Graph Engine Service is a Quarkus-based microservice responsible for building and serving the AUD connectivity graph
model. It periodically fetches AUD requirement versions from Azure Blob Storage (via the Governance API), constructs an
graph of nodes and edges, and exposes a REST API for querying graph data (nodes, edges, statistics, versions).

The service supports two database backends for graph persistence:

|    Backend     |                 Description                  |  Profile   |
|----------------|----------------------------------------------|------------|
| **In-Memory**  | Default — no external database required      | `inmemory` |
| **PostgreSQL** | Relational storage via Hibernate ORM/Panache | `postgres` |

## Prerequisites

- Java 25
- Maven 3.9.+ (or [mvnd](https://github.com/apache/maven-mvnd) for faster builds)
- [just](https://github.com/casey/just) command runner (recommended)
- Docker / Podman (required for PostgreSQL dev services)

## Setup

### Environment Variables

Copy the example file from the API service module and fill in the required values:

```sh
cp ../service-http-apis/.env.example .env.local
```

Key environment variables:

|           Variable            |               Description                |
|-------------------------------|------------------------------------------|
| `AZURE_TENANT_ID`             | Azure AD tenant ID                       |
| `AZURE_CLIENT_ID`             | Azure AD app client ID                   |
| `AZURE_CLIENT_SECRET`         | Azure AD app client secret               |
| `AUD_GOVERNANCE_SCOPES`       | Scopes for the Governance API            |
| `AZURE_STORAGE_BLOB_ENDPOINT` | Azure Storage Blob endpoint for AUD data |

### Build the Project

From the `aud/` directory:

```sh
# Full build (all modules)
mvn clean install -DskipTests

# Build only service-connectivity-engine and its dependencies
mvn --also-make --projects services/service-http-connectivity-engine clean package -DskipTests
```

## Running the Application

### Dev Mode (recommended)

From the `aud/` directory, use the `just` recipes:

```sh
# In-memory backend (default — no external DB needed)
just start-engine-dev

# With PostgreSQL backend (start the local Docker container first; see below)
just start-engine-dev postgres
```

Or using Maven directly:

```sh
mvn --also-make --projects services/service-http-connectivity-engine quarkus:dev \
  --define "quarkus.profile=inmemory" \
  --define "graph.database.type=inmemory"
```

### Local PostgreSQL with Docker

To run the PostgreSQL-backed graph storage locally, start a container:

```sh
docker rm -f yggy-postgres 2>/dev/null || true
docker run -d \
  --name yggy-postgres \
  --restart unless-stopped \
  -p "${POSTGRES_PORT:-5432}:5432" \
  -e POSTGRES_DB=yggy_graph_db \
  -e POSTGRES_USER=yggy_graph_user \
  -e POSTGRES_PASSWORD=yggy_graph_password \
  postgres:16-alpine
```

Default connection settings:

|      Variable       |        Default        |
|---------------------|-----------------------|
| `POSTGRES_HOST`     | `localhost`           |
| `POSTGRES_PORT`     | `5432`                |
| `POSTGRES_DB`       | `yggy_graph_db`       |
| `POSTGRES_USER`     | `yggy_graph_user`     |
| `POSTGRES_PASSWORD` | `yggy_graph_password` |

These are local development defaults, not credentials for any deployed database. If `5432` is already in use on your
machine, set `POSTGRES_PORT` before starting the container; the `postgres` Quarkus profile honors that value.

Once PostgreSQL is healthy, run the service with the `postgres` profile:

```sh
cd aud
mvn --also-make --projects services/service-http-connectivity-engine quarkus:dev \
  -Dquarkus.profile=postgres \
  -Dgraph.database.type=postgres
```

The `postgres` profile uses Hibernate schema management (`update`) in development, so the `graphs`, `nodes`, and `edges`
tables are created automatically on first startup.

To stop the local database:

```sh
docker rm -f yggy-postgres
```

### Verify the Service

Once started, you can confirm the service is running:

- Health checks:
  - http://localhost:8081/q/health/live
  - http://localhost:8081/q/health/ready
  - http://localhost:8081/q/health/started
- OpenAPI documentation:
  - http://localhost:8081/q/openapi
  - http://localhost:8081/q/openapi-ui

## REST API

The API is exposed under `/api/v1` and includes the following endpoints:

| Method |                         Path                         |                      Description                       |
|--------|------------------------------------------------------|--------------------------------------------------------|
| `GET`  | `/api/v1/versions`                                   | List all available graph versions                      |
| `POST` | `/api/v1/versions/{version}/graph`                   | Initialize graph for a specific version                |
| `GET`  | `/api/v1/versions/{version}/graph/init/status`       | Get graph initialization status for a specific version |
| `GET`  | `/api/v1/versions/{version}/graph`                   | Get graph for a specific version                       |
| `GET`  | `/api/v1/versions/{version}/graph/nodes`             | Get nodes (with pagination and filtering)              |
| `GET`  | `/api/v1/versions/{version}/graph/edges`             | Get edges (with pagination and filtering)              |
| `GET`  | `/api/v1/versions/{version}/graph/edges/by-node-ids` | Get edges by source/destination node IDs               |
| `GET`  | `/api/v1/versions/{version}/graph/statistics`        | Get pre-computed graph statistics                      |

### Query Parameters

Nodes and edges endpoints support the following query parameters:

- `page` — Page number (default: `0`)
- `size` — Page size (default: `100`)
- `landingZone` — Filter by landing zone(s)
- `platform` — Filter by platform(s)
- `target` — Filter by target(s)
- `direction` — Edge direction filter: `ALL`, `INBOUND`, `OUTBOUND` (edges only)

## Configuration

Key configuration properties (in `application.properties`):

|                         Property                         |  Default   |                Description                |
|----------------------------------------------------------|------------|-------------------------------------------|
| `quarkus.http.port`                                      | `8081`     | HTTP port                                 |
| `graph.database.type`                                    | `inmemory` | Database backend (`inmemory`, `postgres`) |
| `quarkus.cache.caffeine."version-cache".maximum-size`    | `8`        | Max cached graph versions                 |
| `quarkus.cache.caffeine."graph-init-cache".maximum-size` | `12`       | Max cached graph init entries             |

