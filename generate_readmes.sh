#!/bin/bash

# This script iterates over each subdirectory in 'velolabs_repos' and generates a README.md
# file from README_TEMPLATE.md if one does not exist.

REPOS_DIR="./velolabs_repos"

# Check if README_TEMPLATE.md exists
if [ ! -f "README_TEMPLATE.md" ]; then
  echo "README_TEMPLATE.md not found in the current directory. Exiting."
  exit 1
fi

# Loop over each directory in REPOS_DIR
for repo in "$REPOS_DIR"/*/; do
  # If README.md does not exist in repo
  if [ ! -f "$repo/README.md" ]; then
    echo "Generating README for $repo"
    cp README_TEMPLATE.md "$repo/README.md"
  else
    echo "README already exists for $repo"
  fi
done 