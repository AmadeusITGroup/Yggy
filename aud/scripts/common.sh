#!/usr/bin/env bash
set -o errexit -o errtrace -o pipefail -o nounset

function blue() {
  message="$1"
  if [[ $TERM != dumb ]]; then
      blue=$(tput setaf 4)
      reset=$(tput sgr0)
  else
      blue=''
      reset=''
  fi
  echo "${blue}${message}${reset}"
}

function warning() {
  message="$1"
  if [[ $TERM != dumb ]]; then
      yellow=$(tput setaf 3)
      reset=$(tput sgr0)
  else
      yellow=''
      reset=''
  fi
  echo "${yellow}[WARNING] ${message}${reset}"
}

function error() {
  message="$1"
  if [[ $TERM != dumb ]]; then
      yellow=$(tput setaf 1)
      reset=$(tput sgr0)
  else
      yellow=''
      reset=''
  fi
  echo "${yellow}[ERROR] ${message}${reset}"
}

function green() {
  message="$1"
  if [[ $TERM != dumb ]]; then
      green=$(tput setaf 2)
      reset=$(tput sgr0)
  else
      green=''
      reset=''
  fi
  echo "${green}${message}${reset}"
}

function load_api_env_files() {
  if [[ -f 'services/service-http-apis/.env.local' ]]; then
    blue "### Loading environment variables from services/service-http-apis/.env.local..."
    set -o allexport
    # shellcheck source=/dev/null
    source 'services/service-http-apis/.env.local'
    set +o allexport
  else
    warning "No environment file found with name 'services/service-http-apis/.env.local'. Skipping..."
  fi
}
