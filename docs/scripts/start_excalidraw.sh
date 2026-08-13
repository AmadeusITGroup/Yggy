#!/usr/bin/env bash
#
# start_excalidraw.sh
# Starts a local Excalidraw instance using Docker or Podman.
#
# Usage:
#   ./start_excalidraw.sh [--engine docker|podman] [--port PORT] [--name NAME]
#
# Options:
#   --engine   Force a specific container engine (docker or podman).
#              If not specified, the script auto-detects the available engine.
#   --port     Host port to bind (default: 5000).
#   --name     Container name (default: excalidraw).
#
# Examples:
#   ./start_excalidraw.sh
#   ./start_excalidraw.sh --port 8080
#   ./start_excalidraw.sh --engine docker --port 3000 --name my-excalidraw
#

set -euo pipefail

# --- Defaults ---
IMAGE="excalidraw/excalidraw:latest"
CONTAINER_NAME="excalidraw"
HOST_PORT="5000"
CONTAINER_PORT="80"
ENGINE=""

# --- Parse arguments ---
while [[ $# -gt 0 ]]; do
  case "$1" in
    --engine)
      ENGINE="$2"
      shift 2
      ;;
    --port)
      HOST_PORT="$2"
      shift 2
      ;;
    --name)
      CONTAINER_NAME="$2"
      shift 2
      ;;
    -h|--help)
      head -n 20 "$0" | tail -n +2 | sed 's/^# \?//'
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      exit 1
      ;;
  esac
done

# --- Detect container engine ---
detect_engine() {
  if command -v podman &>/dev/null; then
    echo "podman"
  elif command -v docker &>/dev/null; then
    echo "docker"
  else
    echo ""
  fi
}

if [[ -n "$ENGINE" ]]; then
  # Validate user-specified engine
  if ! command -v "$ENGINE" &>/dev/null; then
    echo "Error: '$ENGINE' is not installed or not in PATH." >&2
    exit 1
  fi
else
  ENGINE=$(detect_engine)
  if [[ -z "$ENGINE" ]]; then
    echo "Error: No container engine found. Please install Docker or Podman." >&2
    exit 1
  fi
  echo "Auto-detected container engine: $ENGINE"
fi

# --- Remove existing container (if any) ---
# The `if` condition is exempt from `set -e`, so a non-zero exit from
# `container inspect` (container not found) simply skips the block.
if "$ENGINE" container inspect "$CONTAINER_NAME" &>/dev/null; then
  echo "Stopping existing '$CONTAINER_NAME' container..."
  "$ENGINE" stop "$CONTAINER_NAME" &>/dev/null || true
  # rm may fail if the container was started with --rm (auto-removed on stop)
  "$ENGINE" rm "$CONTAINER_NAME" &>/dev/null || true
fi

# --- Start container ---
echo "Starting Excalidraw on http://localhost:${HOST_PORT} using $ENGINE..."
"$ENGINE" run --rm -dit \
  --name "$CONTAINER_NAME" \
  -p "${HOST_PORT}:${CONTAINER_PORT}" \
  "$IMAGE"

echo ""
echo "✅ Excalidraw is running at: http://localhost:${HOST_PORT}"
echo "   Container name: $CONTAINER_NAME"
echo "   Engine: $ENGINE"
echo ""
echo "To stop: $ENGINE stop $CONTAINER_NAME"

