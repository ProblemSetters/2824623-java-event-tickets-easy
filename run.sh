#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mvn -q -DskipTests compile 2>&1

echo ""
echo "State files: data/event.json (read-only), data/reservations.json"
echo ""
echo "Usage:"
echo "  ./tickets reserve <person> <qty>"
echo "  ./tickets release <person> <qty>"
echo "  ./tickets transfer <from> <to> <qty>"
echo ""
echo "Reset:"
echo "  bash reset.sh"
