#!/usr/bin/env bash
# Small pinned Gradle bootstrap for environments without a preinstalled Gradle.
# GitHub Actions uses gradle/actions/setup-gradle and invokes `gradle` directly.
set -euo pipefail

VERSION="8.13"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/gestra-bootstrap"
INSTALL_DIR="$BASE/gradle-$VERSION"
DIST="$BASE/gradle-$VERSION-bin.zip"

if [[ ! -x "$INSTALL_DIR/bin/gradle" ]]; then
  command -v curl >/dev/null 2>&1 || { echo "curl is required." >&2; exit 1; }
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required. Install it in Termux with: pkg install unzip" >&2; exit 1; }
  mkdir -p "$BASE"
  if [[ ! -s "$DIST" ]]; then
    curl --fail --location --retry 3 \
      "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$DIST"
  fi
  unzip -q -o "$DIST" -d "$BASE"
fi

exec "$INSTALL_DIR/bin/gradle" "$@"
