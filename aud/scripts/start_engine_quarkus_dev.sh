#!/usr/bin/env bash
set -o errexit -o errtrace -o pipefail -o nounset

# shellcheck source=/dev/null
source ./scripts/common.sh

function main() {
  local profile="${1:-inmemory}"
  local force_version="${2:-}"

  blue '## Reading .env.xxx files'
  load_api_env_files

  local extra_args=()
  if [[ -n "${force_version}" ]]; then
    blue "## Forcing graph version: ${force_version}"
    extra_args+=("--define" "graph.init.force-version=${force_version}")
  fi

  if [[ "${profile}" == "inmemory" ]]; then
    blue '## In-memory mode: excluding Postgres repositories from Arc bean discovery'
    extra_args+=("--define" "quarkus.arc.exclude-types=com.amadeus.aud.libraries.engine.graph.services.storage.postgres.*")
  fi

  blue "## Starting service-http-connectivity-engine as quarkus:dev with profile '${profile}'"
  mvn --also-make --projects services/service-http-connectivity-engine quarkus:dev \
    --define "quarkus.profile=${profile}" \
    --define "graph.database.type=${profile}" \
    "${extra_args[@]+${extra_args[@]}}"
}

main "$@"