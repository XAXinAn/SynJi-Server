#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker not found, installing from Ubuntu apt repository..."
  apt-get update
  apt-get install -y docker.io docker-compose-v2 || apt-get install -y docker.io docker-compose
  systemctl enable --now docker
fi

mkdir -p /etc/docker
cat >/etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://hub-mirror.c.163.com"
  ]
}
EOF

systemctl daemon-reload
systemctl restart docker

if ! docker compose version >/dev/null 2>&1 && ! command -v docker-compose >/dev/null 2>&1; then
  echo "docker compose is not available"
  exit 1
fi

if [ ! -f .env ]; then
  cp .env.example .env
  echo ".env was created from .env.example"
  echo "Edit .env first, then re-run this script."
  exit 1
fi

if grep -q "replace_with_real_silicon_api_key" .env; then
  echo "Please set SILICON_API_KEY in .env first"
  exit 1
fi

if grep -q "change_this_db_password" .env || grep -q "change_this_root_password" .env; then
  echo "Please set DB_PASSWORD and DB_ROOT_PASSWORD in .env first"
  exit 1
fi

COMPOSE_BIN="docker compose"
if ! docker compose version >/dev/null 2>&1; then
  COMPOSE_BIN="docker-compose"
fi

attempt=1
max_attempt=5
while [ "$attempt" -le "$max_attempt" ]; do
  echo "Deploy attempt ${attempt}/${max_attempt} ..."
  if COMPOSE_BAKE=false ${COMPOSE_BIN} --env-file .env up -d --build; then
    echo "Deploy succeeded."
    ${COMPOSE_BIN} --env-file .env ps
    curl -fsS http://127.0.0.1:${APP_PORT:-8080}/api/ping || true
    exit 0
  fi
  echo "Deploy failed, retrying in $((attempt * 10)) seconds..."
  sleep $((attempt * 10))
  attempt=$((attempt + 1))
done

echo "Deploy failed after ${max_attempt} attempts."
exit 1
