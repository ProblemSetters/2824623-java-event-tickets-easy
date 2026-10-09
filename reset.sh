#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cat > "$SCRIPT_DIR/data/reservations.json" <<'EOF'
{
  "reservations":[
    {"person":"sam","qty":4},
    {"person":"riley","qty":2},
    {"person":"jordan","qty":4},
    {"person":"casey","qty":1},
    {"person":"taylor","qty":4},
    {"person":"quinn","qty":3},
    {"person":"morgan","qty":4},
    {"person":"drew","qty":3}
  ]
}
EOF

echo "Restored data/reservations.json to its initial state"
