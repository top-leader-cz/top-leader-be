#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# Sync QA Database to Local
# =============================================================================
# Dumps data from QA (via Cloud SQL proxy) and imports into local PostgreSQL.
#
# Prerequisites:
#   - Cloud SQL proxy running on QA_PORT (default 15432):
#       make db-proxy-sync
#   - Local PostgreSQL running (docker-compose up):
#       localhost:5432
#
# Usage:
#   ./misc/sync-from-qa.sh
#   QA_PORT=15432 LOCAL_PORT=5432 ./misc/sync-from-qa.sh
# =============================================================================

# --- Configuration (override via env vars) ---
QA_HOST="${QA_HOST:-localhost}"
QA_PORT="${QA_PORT:-15432}"
QA_DB="${QA_DB:-top_leader}"
QA_USER="${QA_USER:-top-leader}"

LOCAL_HOST="${LOCAL_HOST:-localhost}"
LOCAL_PORT="${LOCAL_PORT:-5432}"
LOCAL_DB="${LOCAL_DB:-top_leader}"
LOCAL_USER="${LOCAL_USER:-root}"
LOCAL_PASS="${LOCAL_PASS:-postgres}"

# BCrypt hash of 'parole1' (cost factor 10)
BCRYPT_HASH='$2b$10$PCZwb1nrn/MQhD8u6cCtyubEnOYyH0pOsy1JI45lcwEIy380DFBHq'

# Tables to exclude from dump (transient/session data)
EXCLUDE_TABLES=(
    spring_session
    spring_session_attributes
    notification
    token
)

DUMP_FILE="/tmp/qa-dump-$(date +%Y%m%d-%H%M%S).sql"

# --- Helpers ---
info()  { echo "==> $*"; }
error() { echo "ERROR: $*" >&2; exit 1; }

# --- Preflight checks ---
info "Checking Cloud SQL proxy (${QA_HOST}:${QA_PORT})..."
if ! pg_isready -h "$QA_HOST" -p "$QA_PORT" -U "$QA_USER" -d "$QA_DB" -q 2>/dev/null; then
    error "Cannot reach QA database at ${QA_HOST}:${QA_PORT}. Is Cloud SQL proxy running? (make db-proxy-sync)"
fi

info "Checking local database (${LOCAL_HOST}:${LOCAL_PORT})..."
if ! PGPASSWORD="$LOCAL_PASS" pg_isready -h "$LOCAL_HOST" -p "$LOCAL_PORT" -U "$LOCAL_USER" -d "$LOCAL_DB" -q 2>/dev/null; then
    error "Cannot reach local database at ${LOCAL_HOST}:${LOCAL_PORT}. Is docker-compose running?"
fi

# --- Build exclude args ---
EXCLUDE_ARGS=()
for table in "${EXCLUDE_TABLES[@]}"; do
    EXCLUDE_ARGS+=(--exclude-table="$table")
done

# --- Dump QA data ---
info "Dumping QA data to ${DUMP_FILE}..."
pg_dump \
    -h "$QA_HOST" -p "$QA_PORT" -U "$QA_USER" -d "$QA_DB" \
    --data-only --no-owner --no-privileges \
    --disable-triggers \
    "${EXCLUDE_ARGS[@]}" \
    -f "$DUMP_FILE"

DUMP_SIZE=$(du -h "$DUMP_FILE" | cut -f1)
info "Dump complete (${DUMP_SIZE})"

# --- Get list of data tables to truncate ---
info "Truncating local data tables..."
TRUNCATE_SQL=$(PGPASSWORD="$LOCAL_PASS" psql -h "$LOCAL_HOST" -p "$LOCAL_PORT" -U "$LOCAL_USER" -d "$LOCAL_DB" -t -A -c "
    SELECT 'TRUNCATE TABLE ' || string_agg(quote_ident(tablename), ', ') || ' CASCADE;'
    FROM pg_tables
    WHERE schemaname = 'public'
      AND tablename NOT IN ('flyway_schema_history')
      AND tablename NOT LIKE 'pg_%';
")

if [ -n "$TRUNCATE_SQL" ] && [ "$TRUNCATE_SQL" != " " ]; then
    PGPASSWORD="$LOCAL_PASS" psql -h "$LOCAL_HOST" -p "$LOCAL_PORT" -U "$LOCAL_USER" -d "$LOCAL_DB" -c "$TRUNCATE_SQL"
fi

# --- Import dump into local ---
info "Importing QA data into local database..."
PGPASSWORD="$LOCAL_PASS" psql -h "$LOCAL_HOST" -p "$LOCAL_PORT" -U "$LOCAL_USER" -d "$LOCAL_DB" \
    --single-transaction \
    -f "$DUMP_FILE"

# --- Reset all passwords to 'parole1' ---
info "Resetting all user passwords to 'parole1'..."
PGPASSWORD="$LOCAL_PASS" psql -h "$LOCAL_HOST" -p "$LOCAL_PORT" -U "$LOCAL_USER" -d "$LOCAL_DB" -c "
    UPDATE users SET password = '${BCRYPT_HASH}';
"

# --- Summary ---
USER_COUNT=$(PGPASSWORD="$LOCAL_PASS" psql -h "$LOCAL_HOST" -p "$LOCAL_PORT" -U "$LOCAL_USER" -d "$LOCAL_DB" -t -A -c "SELECT count(*) FROM users;")
info "Sync complete! ${USER_COUNT} users imported. All passwords set to 'parole1'."

# --- Cleanup ---
rm -f "$DUMP_FILE"
info "Temp dump file removed."
