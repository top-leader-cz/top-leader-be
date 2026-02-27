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
OS="$(uname -s)"

install_gcloud() {
    case "${OS}" in
        Darwin)
            if ! command -v brew &> /dev/null; then
                echo "Homebrew not found. Installing..."
                /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            fi
            echo "Installing gcloud CLI via Homebrew..."
            brew install --cask google-cloud-sdk
            ;;
        Linux)
            if command -v pacman &> /dev/null; then
                echo "Installing gcloud CLI via pacman..."
                sudo pacman -S google-cloud-cli
            elif command -v apt-get &> /dev/null; then
                echo "Installing gcloud CLI via apt..."
                sudo apt-get install -y apt-transport-https ca-certificates gnupg curl
                curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
                echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee /etc/apt/sources.list.d/google-cloud-sdk.list
                sudo apt-get update && sudo apt-get install -y google-cloud-cli
            else
                echo "Unsupported Linux distribution. Install gcloud manually:"
                echo "  https://cloud.google.com/sdk/docs/install"
                exit 1
            fi
            ;;
        *) echo "Unsupported OS: ${OS}"; exit 1 ;;
    esac
}

install_cloud_sql_proxy() {
    case "${OS}" in
        Darwin)
            if ! command -v brew &> /dev/null; then
                echo "Homebrew not found. Installing..."
                /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            fi
            echo "Installing cloud-sql-proxy via Homebrew..."
            brew install cloud-sql-proxy
            ;;
        Linux)
            echo "Installing cloud-sql-proxy..."
            mkdir -p "${HOME}/.local/bin"
            curl -o "${HOME}/.local/bin/cloud-sql-proxy" https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.15.2/cloud-sql-proxy.linux.amd64
            chmod +x "${HOME}/.local/bin/cloud-sql-proxy"
            if [[ ":${PATH}:" != *":${HOME}/.local/bin:"* ]]; then
                export PATH="${HOME}/.local/bin:${PATH}"
                echo "Added ~/.local/bin to PATH for this session."
                echo "Add to your shell profile: export PATH=\"\${HOME}/.local/bin:\${PATH}\""
            fi
            ;;
        *) echo "Unsupported OS: ${OS}"; exit 1 ;;
    esac
}

# Check if gcloud CLI is installed
if ! command -v gcloud &> /dev/null; then
    echo "gcloud CLI not found."
    install_gcloud
fi

# Check if cloud-sql-proxy is installed
if ! command -v cloud-sql-proxy &> /dev/null; then
    echo "cloud-sql-proxy not found."
    install_cloud_sql_proxy
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
