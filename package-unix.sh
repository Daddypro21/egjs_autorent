#!/bin/bash
# ============================================================
#  EGJS AutoRent — Packaging Linux/macOS
# ============================================================
set -e

echo "=== EGJS AutoRent — Création package natif ==="

# Étape 1 : JAR
echo "[1/3] Compilation..."
./gradlew jar
echo "✓ JAR créé"

# Étape 2 : Détection OS
OS="$(uname -s)"
case "$OS" in
    Darwin*)  TYPE="dmg" ;;
    Linux*)   TYPE="deb" ;;
    *)        TYPE="exe" ;;
esac

echo "[2/3] Création package $TYPE..."
jpackage \
  --input build/libs \
  --name "EGJS AutoRent" \
  --app-version "1.0.0" \
  --vendor "EGJS" \
  --main-jar egjs-autorent-1.0.0.jar \
  --main-class cg.egjs.autorent.app.MainApp \
  --type $TYPE \
  --dest build/installer \
  --java-options "--add-opens java.base/java.lang=ALL-UNNAMED"

echo "[3/3] Terminé !"
echo "✓ Package créé dans build/installer/"
