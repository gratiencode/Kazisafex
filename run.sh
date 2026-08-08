#!/usr/bin/env bash
# Lancement de Kazisafex (fat-jar) sur Temurin 25.
# -Xss32m : debordement de pile dans SQLite natif (thread backfill) sinon
#   -> SIGSEGV (exit 139) ~40-175s apres le demarrage, sans hs_err.
JAVA_HOME=/usr/lib/jvm/temurin-25
JAR=/home/endeleya/NetBeansProjects/Kazisafex/target/Kazisafex-2.0.25-build6-jar-with-dependencies.jar
exec "$JAVA_HOME/bin/java" -Xss32m -jar "$JAR" "$@"
