# =============================================================================
# All Terraform Outputs
# =============================================================================

# -----------------------------------------------------------------------------
# IAM
# -----------------------------------------------------------------------------

output "appengine_service_account" {
  value       = data.google_app_engine_default_service_account.default.email
  description = "App Engine default service account email (used by Cloud Run)"
}

output "cloudrun_compute_service_account" {
  value       = data.google_compute_default_service_account.default.email
  description = "Cloud Run compute service account email"
}

output "scheduler_service_account" {
  value       = google_service_account.scheduler.email
  description = "Cloud Scheduler service account email"
}

# -----------------------------------------------------------------------------
# Cloud Run
# -----------------------------------------------------------------------------

output "cloudrun_qa_url" {
  value       = "https://qa.topleaderplatform.io"
  description = "Cloud Run QA service URL"
}

output "cloudrun_prod_url" {
  value       = "https://topleaderplatform.io"
  description = "Cloud Run PROD service URL"
}

# -----------------------------------------------------------------------------
# Load Balancer
# -----------------------------------------------------------------------------

output "qa_http_ip" {
  value       = google_compute_global_forwarding_rule.qa_http.ip_address
  description = "QA HTTP IP address"
}

output "qa_https_ip" {
  value       = google_compute_global_forwarding_rule.qa_https.ip_address
  description = "QA HTTPS IP address"
}

output "prod_http_ip" {
  value       = google_compute_global_forwarding_rule.prod_http.ip_address
  description = "PROD HTTP IP address"
}

output "prod_https_ip" {
  value       = google_compute_global_forwarding_rule.prod_https.ip_address
  description = "PROD HTTPS IP address"
}

# -----------------------------------------------------------------------------
# Cloud SQL
# -----------------------------------------------------------------------------

output "sql_connection_name" {
  value       = google_sql_database_instance.main.connection_name
  description = "Cloud SQL connection name for Cloud Run"
}

output "sql_public_ip" {
  value       = google_sql_database_instance.main.public_ip_address
  description = "Cloud SQL public IP address"
}

# -----------------------------------------------------------------------------
# DNS
# -----------------------------------------------------------------------------

output "toplead_nameservers" {
  value       = google_dns_managed_zone.toplead.name_servers
  description = "Nameservers for toplead.app"
}

output "topleaderplatform_nameservers" {
  value       = google_dns_managed_zone.topleaderplatform.name_servers
  description = "Nameservers for topleaderplatform.io"
}
