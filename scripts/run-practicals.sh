#!/usr/bin/env bash
# Runs every single-file Java practical (java File.java) under topics/ and fails if any of them fails.
# Maven-based practicals are built separately with: ./mvnw -f topics/pom.xml verify
set -uo pipefail

cd "$(dirname "$0")/.."
failed=0
count=0

while IFS= read -r file; do
  count=$((count + 1))
  dir=$(dirname "$file")
  name=$(basename "$file")
  args=()
  case "$name" in
    MemoryLeakDemo.java) args=(-Xmx64m) ;;              # demonstrates OutOfMemoryError on a small heap
  esac
  printf '%-90s ' "$file"
  if (cd "$dir" && timeout 180 java "${args[@]}" "$name" < /dev/null > /tmp/practical.log 2>&1); then
    echo "OK"
  else
    echo "FAILED"
    tail -n 30 /tmp/practical.log
    failed=$((failed + 1))
  fi
done < <(find topics -path '*/practical/*.java' -not -path '*/src/*' -not -path '*/modules-demo/*' | sort)

echo "Ran $count practicals, $failed failed."
exit $failed
