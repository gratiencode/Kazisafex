#!/usr/bin/env bash
# Garde-fou de non-régression P0.
# Échoue si un secret ou un contournement TLS historiquement interdit
# réapparaît dans le code de production.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
src="$root/src/main/java"

if [[ ! -d "$src" ]]; then
  echo "security-regression: source directory not found: $src" >&2
  exit 2
fi

# Motifs volontairement stricts pour ne pas matcher les commentaires explicatifs.
patterns=(
  'a6514fe5'
  'Admin\*21'
  'useSSL=false[&"]'
  'TrustAll'
  'ALLOW_ALL_HOSTNAME_VERIFIER'
  'setDefaultHostnameVerifier'
)

failed=0
for pattern in "${patterns[@]}"; do
  if grep -RInE --include='*.java' -- "$pattern" "$src"; then
    echo "security-regression: forbidden pattern found: $pattern" >&2
    failed=1
  fi
done

if [[ "$failed" -ne 0 ]]; then
  exit 1
fi

echo "security-regression: OK (no known hard-coded secret/TLS bypass)"
