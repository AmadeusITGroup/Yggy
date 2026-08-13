#!/usr/bin/env bash
set -o errexit -o errtrace -o pipefail -o nounset

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ROOT_FOLDER="$(cd -- "$SCRIPT_DIR/.." && pwd)"

# shellcheck source=/dev/null
source "$SCRIPT_DIR/common.sh"

function main() {
  cd "$ROOT_FOLDER"

  blue '## Reading .env.xxx files'
  load_api_env_files

  blue '## Starting service-http-apis as quarkus:dev'
  mvn --also-make --projects services/service-http-apis quarkus:dev
}

main
