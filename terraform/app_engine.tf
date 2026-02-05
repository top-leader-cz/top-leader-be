# App Engine Application
# Note: App Engine app cannot be deleted once created, only disabled
resource "google_app_engine_application" "app" {
  project       = var.project_id
  location_id   = var.region
  database_type = "CLOUD_DATASTORE_COMPATIBILITY"

  feature_settings {
    split_health_checks = true
  }
}

# Note: App Engine NEG removed - PROD now uses Cloud Run (see cloud_run.tf)
