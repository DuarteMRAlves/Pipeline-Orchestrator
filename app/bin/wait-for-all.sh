#!/bin/bash

# This script waits for all tcp ports for all pipeline containers to be available

usage() {
  echo "Usage: ./wait-for-all.sh stages_file"
}

if [ -z "$1" ]
  then
    echo "Missing argument: stages_file"
    echo
    usage
    exit 1
fi

SCRIPT_PATH="$( cd "$(dirname ""${BASH_SOURCE[0]}"")" >/dev/null 2>&1 || exit 1 ; pwd -P )"

filename="$1"
while IFS=, read -ra lineArr;
do

  target="${lineArr[1]}:${lineArr[2]}"

  echo "Waiting for $target"

  # shellcheck source=wait-for-it.sh
  bash "${SCRIPT_PATH}"/wait-for-it.sh -t 0 "$target";

  echo "$target available"

done < "$filename"
