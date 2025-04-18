#!/bin/bash

# This script generates a summary of README files from each repository
# in the velolabs_repos directory, and outputs the summary to README_SUMMARY.md.
# For each repository, if a README.md exists, it includes the first 10 lines of it,
# otherwise it notes that no README.md was found.

SUMMARY_FILE="README_SUMMARY.md"
REPOS_DIR="./velolabs_repos"

# Create or clear the summary file
echo "# README Summary for Lattis Legacy Repositories" > "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Iterate over each repository directory in REPOS_DIR
for repo in "$REPOS_DIR"/*/; do
    repo_name=$(basename "$repo")
    echo "## $repo_name" >> "$SUMMARY_FILE"
    
    if [ -f "$repo/README.md" ]; then
         echo "### Excerpt from README.md:" >> "$SUMMARY_FILE"
         head -n 10 "$repo/README.md" >> "$SUMMARY_FILE"
    else
         echo "No README.md file found." >> "$SUMMARY_FILE"
    fi
    
    echo -e "\n---\n" >> "$SUMMARY_FILE"
done 