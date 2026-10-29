#!/usr/bin/env bash
# Simulacia odpojenia uzla od siete: backend sa odpoji od lan_net.
# Lokalny frontend a databaza ostavaju funkcne -> uzol pracuje lokalne, zmeny sa ukladaju do outboxu.
# (Alternativa na urovni VM: vypni sietovy adapter VM v hypervisore alebo `sudo ip link set <iface> down`.)
set -euo pipefail

CID=$(docker ps -q \
  --filter "label=com.docker.compose.project=dt-node" \
  --filter "label=com.docker.compose.service=backend")

if [ -z "$CID" ]; then
  echo "Backend kontajner nebezi (projekt dt-node)." >&2
  exit 1
fi

docker network disconnect dt-node_lan_net "$CID"
echo "Uzol je odpojeny od siete. Pripojenie spat: scripts/rejoin-node.sh"
