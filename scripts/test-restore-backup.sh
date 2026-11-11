#!/usr/bin/env bash
# Test automatisé du script de restauration SQLite (succès + refus).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
D="$(mktemp -d)"
trap 'rm -rf "$D"' EXIT

printf 'db-content' > "$D/src.db"
sha256sum "$D/src.db" | awk '{print $1"  src.db"}' > "$D/src.db.sha256"

printf 'old-target' > "$D/target.db"
"$ROOT/scripts/restore-sqlite-backup.sh" "$D/src.db" "$D/target.db" --confirm >/dev/null
cmp "$D/src.db" "$D/target.db"

printf 'corruption' >> "$D/src.db"
printf 'untouched' > "$D/target.db"
if "$ROOT/scripts/restore-sqlite-backup.sh" "$D/src.db" "$D/target.db" --confirm >/dev/null 2>&1; then
  echo "ERREUR : restauration corrompue acceptée" >&2
  exit 1
fi
test "$(cat "$D/target.db")" = "untouched"
echo "restore-sqlite-backup: OK (succès + refus)"
