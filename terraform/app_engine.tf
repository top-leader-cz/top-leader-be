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

# Serverless NEG for QA App Engine service
resource "google_compute_region_network_endpoint_group" "appengine_qa" {
  name                  = "top-be-qa"
  network_endpoint_type = "SERVERLESS"
  region                = var.region
  project               = var.project_id

  app_engine {
    service = "qa"
  }
}

# Serverless NEG for PROD App Engine service
resource "google_compute_region_network_endpoint_group" "appengine_prod" {
  name                  = "top-be-prod"
  network_endpoint_type = "SERVERLESS"
  region                = var.region
  project               = var.project_id

  app_engine {
    service = "prod"
  }
}
