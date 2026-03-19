# =============================================================================
# Workload Identity Federation for GitHub Actions
# =============================================================================
# Allows GitHub Actions to authenticate to GCP without storing a service
# account JSON key. GitHub provides a short-lived OIDC token per workflow run.

# Enable required API
resource "google_project_service" "sts" {
  project            = var.project_id
  service            = "sts.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# WIF Pool — one pool for the whole project
# -----------------------------------------------------------------------------
resource "google_iam_workload_identity_pool" "github" {
  project                   = var.project_id
  workload_identity_pool_id = "github-actions-pool"
  display_name              = "GitHub Actions"
  description               = "WIF pool for GitHub Actions CI/CD"

  depends_on = [google_project_service.iam]
}

# -----------------------------------------------------------------------------
# WIF Provider — maps GitHub OIDC claims to Google attributes
# -----------------------------------------------------------------------------
resource "google_iam_workload_identity_pool_provider" "github" {
  project                            = var.project_id
  workload_identity_pool_id          = google_iam_workload_identity_pool.github.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-actions-provider"
  display_name                       = "GitHub Actions OIDC"

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }

  # Map GitHub token claims to Google attributes
  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.actor"      = "assertion.actor"
    "attribute.repository" = "assertion.repository"
    "attribute.ref"        = "assertion.ref"
  }

  # Only allow tokens from this specific repository
  attribute_condition = "assertion.repository == \"top-leader-cz/top-leader-be\""

  depends_on = [google_iam_workload_identity_pool.github]
}

# -----------------------------------------------------------------------------
# Bind deploy-service SA to the WIF provider
# Allows any workflow run from top-leader-cz/top-leader-be to impersonate it
# -----------------------------------------------------------------------------
resource "google_service_account_iam_member" "wif_deploy" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/deploy-service@${var.project_id}.iam.gserviceaccount.com"
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github.name}/attribute.repository/top-leader-cz/top-leader-be"
}
