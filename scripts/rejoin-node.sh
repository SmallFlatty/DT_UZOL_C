#!/usr/bin/env bash
# Opatovne pripojenie backendu do lan_net -> outbox sa po par sekundach automaticky odosle.
set -euo pipefail

CID=$(docker ps -q \
  --filter "label=com.docker.compose.project=dt-node" \
  --filter "label=com.docker.compose.service=backend")

if [ -z "$CID" ]; then
  echo "Backend kontajner nebezi (projekt dt-node)." >&2
  exit 1
fi

docker network connect dt-node_lan_net "$CID"
echo "Uzol je znova v sieti. Ak by port 8080 nebol dostupny: docker compose restart backend"
