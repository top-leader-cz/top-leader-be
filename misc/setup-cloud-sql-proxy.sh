#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# Cloud SQL Auth Proxy - Local Setup
# =============================================================================
# Connects to Cloud SQL via IAM (no public IP needed).
# Use this to access the database from IntelliJ IDEA or psql.
#
# Usage:
#   ./misc/setup-cloud-sql-proxy.sh          # default port 5432
#   ./misc/setup-cloud-sql-proxy.sh 15432    # custom port
# =============================================================================

CONNECTION_NAME="topleader-394306:europe-west3:top-leader-db"
PORT="${1:-5432}"

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo "Homebrew not found. Installing..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

# Check if gcloud CLI is installed
if ! command -v gcloud &> /dev/null; then
    echo "gcloud CLI not found. Installing via Homebrew..."
    brew install --cask google-cloud-sdk
fi

# Check if cloud-sql-proxy is installed
if ! command -v cloud-sql-proxy &> /dev/null; then
    echo "cloud-sql-proxy not found. Installing via Homebrew..."
    brew install cloud-sql-proxy
fi

# Check if gcloud is authenticated
if ! gcloud auth print-access-token &> /dev/null 2>&1; then
    echo "Not authenticated. Running gcloud auth login..."
    gcloud auth login
    gcloud auth application-default login
fi

echo "Starting Cloud SQL Auth Proxy..."
echo "  Connection: ${CONNECTION_NAME}"
echo "  Local:      localhost:${PORT}"
echo ""
echo "Connect from IDEA/psql using: localhost:${PORT}"
echo "Press Ctrl+C to stop."
echo ""

cloud-sql-proxy "${CONNECTION_NAME}" --port "${PORT}"
