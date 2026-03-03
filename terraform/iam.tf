# =============================================================================
# IAM Configuration
# =============================================================================
# This file manages IAM bindings and service account permissions.

# -----------------------------------------------------------------------------
# Service Account Data Sources (legacy — kept for backward compatibility)
# -----------------------------------------------------------------------------

# App Engine default service account
data "google_app_engine_default_service_account" "default" {
  project = var.project_id
}

# Cloud Run compute service account
data "google_compute_default_service_account" "default" {
  project = var.project_id
}

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

# Grant Secret Manager access to App Engine service account (legacy — remove after migration verified)
resource "google_project_iam_member" "appengine_secretmanager" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${data.google_app_engine_default_service_account.default.email}"

  depends_on = [google_project_service.secretmanager]
}

# Grant Secret Manager access to Cloud Run compute service account (legacy — remove after migration verified)
resource "google_project_iam_member" "cloudrun_secretmanager" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${data.google_compute_default_service_account.default.email}"

  depends_on = [google_project_service.secretmanager]
}

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

# Grant Cloud SQL Client role to App Engine service account (legacy — remove after migration verified)
resource "google_project_iam_member" "appengine_cloudsql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${data.google_app_engine_default_service_account.default.email}"

  depends_on = [google_project_service.sqladmin]
}

# Grant Cloud SQL Client role to Cloud Run compute service account (legacy — remove after migration verified)
resource "google_project_iam_member" "cloudrun_cloudsql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${data.google_compute_default_service_account.default.email}"

  depends_on = [google_project_service.sqladmin]
}

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

# Grant Cloud Run Admin role to deploy service account for GitHub Actions deployments
resource "google_project_iam_member" "deploy_cloudrun_admin" {
  project = var.project_id
  role    = "roles/run.admin"
  member  = "serviceAccount:deploy-service@${var.project_id}.iam.gserviceaccount.com"

  depends_on = [google_project_service.run]
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
