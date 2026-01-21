#!/bin/bash

# Script: setup-cloud-run-qa.sh
# Description: Setup Cloud Run infrastructure for QA environment
# Creates: Artifact Registry, Service Account, IAM permissions
#
# ⚠️  DEPRECATED: Use Terraform instead!
# ⚠️  This script is kept for backward compatibility only.
# ⚠️  Recommended: cd terraform && terraform apply
#
# See: terraform/cloud_run_infrastructure.tf
# See: terraform/README.md

set -e  # Exit on error
set -u  # Exit on undefined variable

echo "⚠️  WARNING: This script is DEPRECATED"
echo "⚠️  Please use Terraform for infrastructure management:"
echo "⚠️  cd terraform && terraform apply"
echo ""
read -p "Continue anyway? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted. Use Terraform instead."
    exit 1
fi
echo ""

# --- CONFIGURATION ---
PROJECT_ID="topleader-394306"
REGION="europe-west3"
ARTIFACT_REPO_NAME="cloud-run"
SERVICE_ACCOUNT_NAME="cloud-run-sa"
SERVICE_ACCOUNT_DISPLAY_NAME="Cloud Run Service Account"

# List of secrets that need Secret Manager access
SECRETS=(
  "SPRING_DATASOURCE_PASSWORD_QA"
  "SPRING_MAIL_PASSWORD"
  "TOP_LEADER_CALENDLY_CLIENT_SECRETS"
  "GOOGLE_CLIENT_CLIENT_SECRET"
  "SPRING_AI_OPENAI_APIKEY"
  "GRAFANA_OTLP_TOKEN"
  "JOB_TRIGGER_PASSWORD"
)

# --- COLOR CODES ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# --- TRACKING VARIABLES ---
CREATED_ITEMS=()
SKIPPED_ITEMS=()
FAILED_ITEMS=()

# --- HELPER FUNCTIONS ---

print_header() {
  echo -e "\n${BLUE}========================================${NC}"
  echo -e "${BLUE}$1${NC}"
  echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
  echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
  echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
  echo -e "${RED}✗${NC} $1"
}

print_info() {
  echo -e "${BLUE}ℹ${NC} $1"
}

# --- PREREQUISITE CHECKS ---

check_prerequisites() {
  print_header "Checking Prerequisites"

  # Check if gcloud is installed
  if ! command -v gcloud &> /dev/null; then
    print_error "gcloud CLI is not installed"
    echo "Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
  fi
  print_success "gcloud CLI is installed"

  # Check if gcloud is configured
  if ! gcloud config get-value project &> /dev/null; then
    print_error "gcloud is not configured"
    echo "Run: gcloud auth login && gcloud config set project ${PROJECT_ID}"
    exit 1
  fi
  print_success "gcloud is configured"

  # Verify correct project
  CURRENT_PROJECT=$(gcloud config get-value project 2>/dev/null)
  if [ "$CURRENT_PROJECT" != "$PROJECT_ID" ]; then
    print_warning "Current project is $CURRENT_PROJECT, switching to $PROJECT_ID"
    gcloud config set project "$PROJECT_ID"
  fi
  print_success "Project set to $PROJECT_ID"

  # Test API access
  if ! gcloud projects describe "$PROJECT_ID" &> /dev/null; then
    print_error "Cannot access project $PROJECT_ID"
    echo "Check your permissions or authentication"
    exit 1
  fi
  print_success "Project access verified"
}

# --- ARTIFACT REGISTRY SETUP ---

setup_artifact_registry() {
  print_header "Setting up Artifact Registry"

  # Check if repository exists
  if gcloud artifacts repositories describe "$ARTIFACT_REPO_NAME" \
      --location="$REGION" \
      --project="$PROJECT_ID" &> /dev/null; then
    print_warning "Artifact Registry repository '$ARTIFACT_REPO_NAME' already exists"
    SKIPPED_ITEMS+=("Artifact Registry: $ARTIFACT_REPO_NAME")
  else
    print_info "Creating Artifact Registry repository '$ARTIFACT_REPO_NAME'..."

    if gcloud artifacts repositories create "$ARTIFACT_REPO_NAME" \
        --repository-format=docker \
        --location="$REGION" \
        --description="Cloud Run container images" \
        --project="$PROJECT_ID"; then
      print_success "Created Artifact Registry repository '$ARTIFACT_REPO_NAME'"
      CREATED_ITEMS+=("Artifact Registry: $ARTIFACT_REPO_NAME")
    else
      print_error "Failed to create Artifact Registry repository"
      FAILED_ITEMS+=("Artifact Registry: $ARTIFACT_REPO_NAME")
      return 1
    fi
  fi

  # Display repository URL
  REPO_URL="${REGION}-docker.pkg.dev/${PROJECT_ID}/${ARTIFACT_REPO_NAME}"
  print_info "Repository URL: $REPO_URL"
}

# --- SERVICE ACCOUNT SETUP ---

setup_service_account() {
  print_header "Setting up Service Account"

  SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

  # Check if service account exists
  if gcloud iam service-accounts describe "$SERVICE_ACCOUNT_EMAIL" \
      --project="$PROJECT_ID" &> /dev/null; then
    print_warning "Service account '$SERVICE_ACCOUNT_NAME' already exists"
    SKIPPED_ITEMS+=("Service Account: $SERVICE_ACCOUNT_NAME")
  else
    print_info "Creating service account '$SERVICE_ACCOUNT_NAME'..."

    if gcloud iam service-accounts create "$SERVICE_ACCOUNT_NAME" \
        --display-name="$SERVICE_ACCOUNT_DISPLAY_NAME" \
        --project="$PROJECT_ID"; then
      print_success "Created service account '$SERVICE_ACCOUNT_NAME'"
      CREATED_ITEMS+=("Service Account: $SERVICE_ACCOUNT_NAME")
    else
      print_error "Failed to create service account"
      FAILED_ITEMS+=("Service Account: $SERVICE_ACCOUNT_NAME")
      return 1
    fi
  fi

  print_info "Service account email: $SERVICE_ACCOUNT_EMAIL"
}

# --- IAM PERMISSIONS SETUP ---

assign_cloud_sql_permission() {
  print_header "Assigning Cloud SQL Client Role"

  SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

  print_info "Assigning roles/cloudsql.client to $SERVICE_ACCOUNT_EMAIL..."

  if gcloud projects add-iam-policy-binding "$PROJECT_ID" \
      --member="serviceAccount:$SERVICE_ACCOUNT_EMAIL" \
      --role="roles/cloudsql.client" \
      --condition=None \
      > /dev/null; then
    print_success "Assigned Cloud SQL Client role"
    CREATED_ITEMS+=("IAM: Cloud SQL Client role")
  else
    print_error "Failed to assign Cloud SQL Client role"
    FAILED_ITEMS+=("IAM: Cloud SQL Client role")
    return 1
  fi
}

assign_secret_permissions() {
  print_header "Assigning Secret Manager Permissions"

  SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

  for SECRET_NAME in "${SECRETS[@]}"; do
    print_info "Processing secret: $SECRET_NAME"

    # Check if secret exists
    if ! gcloud secrets describe "$SECRET_NAME" \
        --project="$PROJECT_ID" &> /dev/null; then
      print_warning "Secret '$SECRET_NAME' does not exist in Secret Manager (will skip)"
      print_info "  → Create it with: echo -n 'value' | gcloud secrets create $SECRET_NAME --data-file=-"
      SKIPPED_ITEMS+=("Secret permission: $SECRET_NAME (secret not found)")
      continue
    fi

    # Add IAM policy binding
    if gcloud secrets add-iam-policy-binding "$SECRET_NAME" \
        --member="serviceAccount:$SERVICE_ACCOUNT_EMAIL" \
        --role="roles/secretmanager.secretAccessor" \
        --project="$PROJECT_ID" \
        > /dev/null 2>&1; then
      print_success "  ✓ Granted access to $SECRET_NAME"
      CREATED_ITEMS+=("Secret permission: $SECRET_NAME")
    else
      print_warning "  ⚠ Permission may already exist for $SECRET_NAME"
      SKIPPED_ITEMS+=("Secret permission: $SECRET_NAME")
    fi
  done
}

# --- SUMMARY REPORT ---

print_summary() {
  print_header "Setup Summary"

  if [ ${#CREATED_ITEMS[@]} -gt 0 ]; then
    echo -e "\n${GREEN}Created:${NC}"
    for item in "${CREATED_ITEMS[@]}"; do
      echo -e "  ${GREEN}✓${NC} $item"
    done
  fi

  if [ ${#SKIPPED_ITEMS[@]} -gt 0 ]; then
    echo -e "\n${YELLOW}Skipped (already exists):${NC}"
    for item in "${SKIPPED_ITEMS[@]}"; do
      echo -e "  ${YELLOW}⊘${NC} $item"
    done
  fi

  if [ ${#FAILED_ITEMS[@]} -gt 0 ]; then
    echo -e "\n${RED}Failed:${NC}"
    for item in "${FAILED_ITEMS[@]}"; do
      echo -e "  ${RED}✗${NC} $item"
    done
  fi

  echo -e "\n${BLUE}Next Steps:${NC}"
  echo "1. Verify secrets exist in Secret Manager:"
  echo "   gcloud secrets list --project=$PROJECT_ID"
  echo ""
  echo "2. Configure Docker authentication for Artifact Registry:"
  echo "   gcloud auth configure-docker $REGION-docker.pkg.dev"
  echo ""
  echo "3. Deploy to Cloud Run:"
  echo "   make deploy-qa"
  echo ""

  if [ ${#FAILED_ITEMS[@]} -gt 0 ]; then
    exit 1
  fi
}

# --- MAIN EXECUTION ---

main() {
  print_header "Cloud Run QA Infrastructure Setup"
  echo "Project: $PROJECT_ID"
  echo "Region: $REGION"
  echo ""

  # Run setup steps
  check_prerequisites
  setup_artifact_registry
  setup_service_account
  assign_cloud_sql_permission
  assign_secret_permissions

  # Print summary
  print_summary

  print_success "Cloud Run infrastructure setup complete!"
}

# Execute main function
main
