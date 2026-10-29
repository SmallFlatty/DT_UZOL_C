#!/usr/bin/env bash
# Automaticke nasadenie na VM: ak je v gite nova verzia, stiahne ju a prebuilduje stack tohto uzla.
# Funguje, ak je tato zlozka sucastou naklonovaneho git repozitara.
#
# Cron (kazde 2 min):
#   */2 * * * * /home/<user>/dt/scripts/auto-deploy.sh >> /var/log/dt-deploy.log 2>&1
set -euo pipefail

cd "$(dirname "$0")/.."

git fetch --quiet
if [ "$(git rev-parse HEAD)" = "$(git rev-parse '@{u}')" ]; then
  exit 0   # ziadna zmena
fi

echo "[$(date -Is)] nova verzia -> nasadzujem"
git pull --ff-only
docker compose up -d --build --remove-orphans
docker image prune -f >/dev/null
echo "[$(date -Is)] hotovo"
