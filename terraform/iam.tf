# =============================================================================
# IAM Configuration
# =============================================================================
# This file manages IAM bindings and service account permissions.

# -----------------------------------------------------------------------------
# Dedicated Service Accounts for Cloud Run
# -----------------------------------------------------------------------------

resource "google_service_account" "qa" {
  account_id   = "top-leader-qa-sa"
  display_name = "Top Leader QA Cloud Run"
  description  = "Service account for QA Cloud Run service"
  project      = var.project_id
}

resource "google_service_account" "prod" {
  account_id   = "top-leader-prod-sa"
  display_name = "Top Leader PROD Cloud Run"
  description  = "Service account for PROD Cloud Run service"
  project      = var.project_id
}

# -----------------------------------------------------------------------------
# Secret Manager Access
# -----------------------------------------------------------------------------

# Grant Secret Manager access to QA service account
resource "google_project_iam_member" "qa_secretmanager" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.qa.email}"

  depends_on = [google_project_service.secretmanager]
}

# Grant Secret Manager access to PROD service account
resource "google_project_iam_member" "prod_secretmanager" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.prod.email}"

  depends_on = [google_project_service.secretmanager]
}

# -----------------------------------------------------------------------------
# Cloud SQL Access
# -----------------------------------------------------------------------------

# Grant Cloud SQL Client role to QA service account
resource "google_project_iam_member" "qa_cloudsql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.qa.email}"

  depends_on = [google_project_service.sqladmin]
}

# Grant Cloud SQL Client role to PROD service account
resource "google_project_iam_member" "prod_cloudsql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.prod.email}"

  depends_on = [google_project_service.sqladmin]
}

# -----------------------------------------------------------------------------
# Deploy Service Account (GitHub Actions)
# -----------------------------------------------------------------------------

# Grant Cloud Run Developer role to deploy service account for GitHub Actions deployments.
# Narrower than run.admin — allows deploying revisions and managing services/revisions but
# NOT setIamPolicy. Public access (allUsers → run.invoker) is managed by the
# google_cloud_run_service_iam_member resources below, not by the CI workflow.
resource "google_project_iam_member" "deploy_cloudrun_developer" {
  project = var.project_id
  role    = "roles/run.developer"
  member  = "serviceAccount:deploy-service@${var.project_id}.iam.gserviceaccount.com"

  depends_on = [google_project_service.run]
}

# -----------------------------------------------------------------------------
# Public (unauthenticated) access to Cloud Run services
# -----------------------------------------------------------------------------
# Previously these bindings were re-asserted on every CI deploy via
# `gcloud run services add-iam-policy-binding`. That required deploy SA to have
# run.admin. Moved here so the deploy SA only needs run.developer.

resource "google_cloud_run_service_iam_member" "qa_allow_unauth" {
  location = var.region
  project  = var.project_id
  service  = "top-leader-qa"
  role     = "roles/run.invoker"
  member   = "allUsers"
}

resource "google_cloud_run_service_iam_member" "prod_allow_unauth" {
  location = var.region
  project  = var.project_id
  service  = "top-leader-prod"
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# Grant Service Account User role to deploy service account (needed to act as Cloud Run service account)
resource "google_project_iam_member" "deploy_service_account_user" {
  project = var.project_id
  role    = "roles/iam.serviceAccountUser"
  member  = "serviceAccount:deploy-service@${var.project_id}.iam.gserviceaccount.com"
}

# Allow deploy SA to act as QA service account
resource "google_service_account_iam_member" "deploy_act_as_qa" {
  service_account_id = google_service_account.qa.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:deploy-service@${var.project_id}.iam.gserviceaccount.com"
}

# Allow deploy SA to act as PROD service account
resource "google_service_account_iam_member" "deploy_act_as_prod" {
  service_account_id = google_service_account.prod.name
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:deploy-service@${var.project_id}.iam.gserviceaccount.com"
}
