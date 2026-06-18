#!/usr/bin/env bash
set -euo pipefail

echo "Esperando al coordinador ${DB_HOST}:${DB_PORT}…"
until PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -U "${DB_USER}" -d "${DB_NAME}" -c "SELECT 1;" >/dev/null 2>&1; do
  sleep 2
done

echo "==> Ejecutando 02_citus.sql (extensiones, nodos, reference)…"
PGPASSWORD="${DB_PASSWORD}" psql -v ON_ERROR_STOP=1 \
  -h "${DB_HOST}" -U "${DB_USER}" -d "${DB_NAME}" \
  -f /init/02_citus.sql

if [[ "${APPLY_DISTRIBUTION:-true}" == "true" ]]; then
  echo "==> Ajustando parámetros de sharding…"
  # OJO: en Citus 13.2 es shard_replication_factor (no replication_factor)
  PGPASSWORD="${DB_PASSWORD}" psql -v ON_ERROR_STOP=1 -h "${DB_HOST}" -U "${DB_USER}" -d "${DB_NAME}" \
    -c "SET citus.shard_count = ${SHARD_COUNT:-32};"
  PGPASSWORD="${DB_PASSWORD}" psql -v ON_ERROR_STOP=1 -h "${DB_HOST}" -U "${DB_USER}" -d "${DB_NAME}" \
    -c "SET citus.shard_replication_factor = ${REPLICATION_FACTOR:-1};"

  echo "==> Ejecutando 03_distribute_by_center.sql (migración + distribución)…"
  PGPASSWORD="${DB_PASSWORD}" psql -v ON_ERROR_STOP=1 \
    -h "${DB_HOST}" -U "${DB_USER}" -d "${DB_NAME}" \
    -f /init/03_distribute_by_center.sql
else
  echo "APPLY_DISTRIBUTION=false → se omitió 03_distribute_by_center.sql"
fi

echo "Bootstrap Citus OK."
