#!/usr/bin/env bash
# Construit une image applicative autonome (Linux ou Windows) avec jpackage.
# Prérequis : JDK 25 (jpackage) et le jar "with-dependencies" déjà construit.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

JAR="$(find target -maxdepth 1 -name 'Kazisafex-*-jar-with-dependencies.jar' -print -quit)"
if [[ -z "${JAR}" ]]; then
  echo "Jar with-dependencies introuvable : lancez d'abord mvn -DskipTests package" >&2
  exit 1
fi

INPUT="target/jpackage-input"
DEST="target/jpackage"
rm -rf "$INPUT" "$DEST"
mkdir -p "$INPUT"
cp "$JAR" "$INPUT/"

jpackage \
  --type app-image \
  --name Kazisafex \
  --input "$INPUT" \
  --main-jar "$(basename "$JAR")" \
  --main-class com.endeleya.kazisafex.Main \
  --java-options '--enable-native-access=ALL-UNNAMED' \
  --dest "$DEST"

echo "Image applicative : $DEST/Kazisafex"
