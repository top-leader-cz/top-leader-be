# Cloud Run Service - QA
# Note: The service itself is deployed via GitHub Actions
# This file only manages domain mapping and DNS records

# Domain mapping for qa.topleaderplatform.io
resource "google_cloud_run_domain_mapping" "qa" {
  name     = "qa.topleaderplatform.io"
  location = var.region
  project  = var.project_id

  metadata {
    namespace = var.project_id
    labels = {
      environment = "qa"
      managed-by  = "terraform"
    }
  }

  spec {
    route_name = "top-leader-qa"
  }

  # Cloud Run service must exist before mapping
  # Service is created by GitHub Actions deployment
  lifecycle {
    ignore_changes = [
      metadata[0].annotations,
    ]
  }
}

# DNS CNAME record for qa.topleaderplatform.io pointing to Cloud Run
resource "google_dns_record_set" "qa_cloudrun" {
  name         = "qa.${google_dns_managed_zone.topleaderplatform.dns_name}"
  type         = "CNAME"
  ttl          = 300
  managed_zone = google_dns_managed_zone.topleaderplatform.name
  project      = var.project_id

  rrdatas = ["ghs.googlehosted.com."]
}

# Output the Cloud Run service URL
output "cloudrun_qa_url" {
  value       = "https://qa.topleaderplatform.io"
  description = "Cloud Run QA service URL"
}

output "cloudrun_qa_domain_mapping_status" {
  value       = google_cloud_run_domain_mapping.qa.status
  description = "Cloud Run QA domain mapping status"
}
