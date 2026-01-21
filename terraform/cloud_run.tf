# Cloud Run Service - QA
# Note: The service itself is deployed via GitHub Actions
# Domain mapping must be done via gcloud (not supported in europe-west3 via Terraform)

# Serverless NEG for Cloud Run QA service
resource "google_compute_region_network_endpoint_group" "cloudrun_qa" {
  name                  = "top-be-qa-cloudrun"
  network_endpoint_type = "SERVERLESS"
  region                = var.region
  project               = var.project_id

  cloud_run {
    service = "top-leader-qa"
  }
}

# Artifact Registry for Docker images
resource "google_artifact_registry_repository" "top_leader" {
  location      = var.region
  repository_id = "top-leader"
  description   = "Docker images for Top Leader application"
  format        = "DOCKER"
  project       = var.project_id

  depends_on = [google_project_service.artifactregistry]
}

# DNS A record for qa.topleaderplatform.io (pointing to Load Balancer)
resource "google_dns_record_set" "qa_cloudrun" {
  name         = "qa.${google_dns_managed_zone.topleaderplatform.dns_name}"
  type         = "A"
  ttl          = 300
  managed_zone = google_dns_managed_zone.topleaderplatform.name
  project      = var.project_id

  rrdatas = ["34.160.238.170"]
}

# Output the Cloud Run service URL
output "cloudrun_qa_url" {
  value       = "https://qa.topleaderplatform.io"
  description = "Cloud Run QA service URL"
}
