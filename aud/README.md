# AUD

This is Yggy v1, also called AUD for historical reasons.

## Development requirements

- java 25
- latest maven or maven daemon 3.9.x

Install your environment with `just setup`.

Ensure your environment works with:

```shell
mvnd test
```

## Development startup

You'll need an azure storage account (for now).

The first time, prepare your environment variables:

```sh
cp services/service-http-apis/.env.example services/service-http-apis/.env.local
# update services/service-http-apis/.env.local with your own credentials
```

Once ready, run:

```shell
just start-api-dev
```

and head to http://localhost:8080

## Running locally against a data directory

The container build needs no credentials. It reads aggregated network data from a directory you mount at
`/tmp/yggy-data/<scope-version>/`:

```shell
docker build . -f local.dockerfile -t aud-local
docker run --rm -p 8088:8080 -v <your-data-dir>:/tmp/yggy-data/1.0.1/ aud-local:latest
```

`curl http://localhost:8088/v2/versions` should then show 1 version.

Or in one step: `just start-api-local <your-data-dir>`.

The directory must contain the four aggregated files the API expects:

|        File        |                        Contents                         |
|--------------------|---------------------------------------------------------|
| `api.yml`          | every model object, concatenated as multi-document YAML |
| `metadata.yml`     | one `ScopeVersionMapping` per scope                     |
| `requirements.yml` | the scope list with source repository and file names    |
| `release.yml`      | one `ScopeReleaseNote` per scope                        |

A ready-made fixture set is not part of this release, so you supply the descriptors and build those four files yourself:

```shell
just generate-network-data <your-descriptor-dir>
```

The descriptor directory needs a `Scope/` folder of scope YAML documents plus one folder per scope, and the aggregated
output is written to `<your-descriptor-dir>/network-data` — which is the path to mount or pass to `just
start-api-local`. See [`scripts/generateNetworkData.java`](./scripts/generateNetworkData.java) for the layout it walks.
