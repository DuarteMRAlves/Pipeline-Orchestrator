#!/bin/bash

# This script waits for all tcp ports for all pipeline containers to be available

usage() {
  echo "Usage: ./wait-for-all.sh urls_file"
  echo "  urls_file: file with the urls that should be waited for"
}

if [ -z "$1" ]
  then
    echo "Missing argument: hosts_file"
    echo
    usage
    exit 1
fi

# Assume the the wait-for-it script is in the same directory as this script
SCRIPT_PATH="$( cd "$(dirname ""${BASH_SOURCE[0]}"")" >/dev/null 2>&1 || exit 1 ; pwd -P )"

filename="$1"
while IFS= read -r target;
do

  echo "Waiting for $target"

  # shellcheck source=wait-for-it.sh
  bash "${SCRIPT_PATH}"/wait-for-it.sh -t 0 "$target";

  echo "$target available"

done < "$filename"

echo "Finished waiting"
