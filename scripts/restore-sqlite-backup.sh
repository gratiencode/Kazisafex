#!/usr/bin/env bash
# Restauration MANUELLE d'une sauvegarde SQLite Kazisafex.
# Usage: restore-sqlite-backup.sh <backup> <target.db> --confirm
# L'application doit être fermée. Le script vérifie le SHA-256 avant copie.
set -euo pipefail

BACKUP="${1:-}"
TARGET="${2:-}"
CONFIRM="${3:-}"

if [[ -z "$BACKUP" || -z "$TARGET" || "$CONFIRM" != "--confirm" ]]; then
  echo "Usage: $0 <backup> <target.db> --confirm" >&2
  echo "Refus : confirmation explicite --confirm obligatoire." >&2
  exit 2
fi
if [[ ! -f "$BACKUP" || ! -f "$BACKUP.sha256" ]]; then
  echo "Sauvegarde ou empreinte .sha256 absente." >&2
  exit 1
fi
if ! command -v sha256sum >/dev/null 2>&1; then
  echo "sha256sum requis." >&2
  exit 1
fi

expected="$(awk '{print $1}' "$BACKUP.sha256" | head -1)"
actual="$(sha256sum "$BACKUP" | awk '{print $1}')"
if [[ "$expected" != "$actual" ]]; then
  echo "SHA-256 invalide : restauration refusée." >&2
  exit 1
fi

tmp="${TARGET}.restore-tmp"
cp "$BACKUP" "$tmp"
mv "$tmp" "$TARGET"
# Un WAL/SHM antérieur ne doit pas être réutilisé avec une autre base.
rm -f "${TARGET}-wal" "${TARGET}-shm"
echo "Restauration terminée : $TARGET"
