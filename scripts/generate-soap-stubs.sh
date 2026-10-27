#!/usr/bin/env bash
# Génère les stubs JAX-WS d'un module à partir des WSDL qu'il embarque.
#
# Usage : scripts/generate-soap-stubs.sh <dossier-du-module> <package-java> [--fetch]
#   --fetch  télécharge d'abord les WSDL depuis le serveur SOAP en marche
# Variables : WILDFLY_HOME (par défaut configuré), SOAP_BASE_URL (défaut http://localhost:8080/client-management-soap)
set -euo pipefail

# Définition automatique de WILDFLY_HOME (chemin adapté pour Git Bash)
WILDFLY_HOME="${WILDFLY_HOME:-/c/Users/Mamoudouba/Documents/serveur_web/wildfly-41.0.1.Final}"

MODULE="$(cd "$1" && pwd)"
PACKAGE="$2"
BASE_URL="${SOAP_BASE_URL:-http://localhost:8080/client-management-soap}"
SERVICES="CustomerWebService GroupWebService MembershipWebService"
WSDL_DIR="$MODULE/src/main/resources/wsdl"
SRC_DIR="$MODULE/src/main/java"
CLASSES_DIR="$(mktemp -d)"

mkdir -p "$WSDL_DIR" "$SRC_DIR"

if [ "${3:-}" = "--fetch" ]; then
  for s in $SERVICES; do
    curl -fsS -o "$WSDL_DIR/$s.wsdl" "$BASE_URL/$s?wsdl"
  done
fi

# Le package généré ne contient que du code généré : on le repart de zéro pour éviter les restes.
rm -rf "$SRC_DIR/${PACKAGE//.//}"

for s in $SERVICES; do
  "$WILDFLY_HOME/bin/wsconsume.sh" -k -n -q \
      -p "$PACKAGE" \
      -w "classpath:wsdl/$s.wsdl" \
      -s "$SRC_DIR" -o "$CLASSES_DIR" \
      "$WSDL_DIR/$s.wsdl"
done
rm -rf "$CLASSES_DIR"

echo "Stubs générés dans $SRC_DIR/${PACKAGE//.//}"