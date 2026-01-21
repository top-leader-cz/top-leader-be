# =============================================================================
# IAM Configuration
# =============================================================================
# This file manages IAM bindings and service account permissions.

# -----------------------------------------------------------------------------
# Service Account Data Sources
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
# Secret Manager Access
# -----------------------------------------------------------------------------

# Grant Secret Manager access to App Engine service account
resource "google_project_iam_member" "appengine_secretmanager" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${data.google_app_engine_default_service_account.default.email}"

  depends_on = [google_project_service.secretmanager]
}

# Grant Secret Manager access to Cloud Run compute service account
resource "google_project_iam_member" "cloudrun_secretmanager" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${data.google_compute_default_service_account.default.email}"

  depends_on = [google_project_service.secretmanager]
}

# -----------------------------------------------------------------------------
# Cloud SQL Access
# -----------------------------------------------------------------------------

# Grant Cloud SQL Client role to App Engine service account
resource "google_project_iam_member" "appengine_cloudsql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${data.google_app_engine_default_service_account.default.email}"

  depends_on = [google_project_service.sqladmin]
}

# Grant Cloud SQL Client role to Cloud Run compute service account
resource "google_project_iam_member" "cloudrun_cloudsql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${data.google_compute_default_service_account.default.email}"

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

# -----------------------------------------------------------------------------
# Outputs
# -----------------------------------------------------------------------------

output "appengine_service_account" {
  value       = data.google_app_engine_default_service_account.default.email
  description = "App Engine default service account email"
}

output "cloudrun_compute_service_account" {
  value       = data.google_compute_default_service_account.default.email
  description = "Cloud Run compute service account email"
}

output "scheduler_service_account" {
  value       = google_service_account.scheduler.email
  description = "Cloud Scheduler service account email"
}
