# DNS Zone for toplead.app
resource "google_dns_managed_zone" "toplead" {
  name        = "toplead"
  dns_name    = "toplead.app."
  description = "DNS zone for domain: toplead.app"
  project     = var.project_id
  visibility  = "public"
}

# DNS Zone for topleaderplatform.io
resource "google_dns_managed_zone" "topleaderplatform" {
  name        = "topleaderplatform-io"
  dns_name    = "topleaderplatform.io."
  description = "DNS zone for domain: topleaderplatform.io"
  project     = var.project_id
  visibility  = "public"

  dnssec_config {
    state = "on"
    default_key_specs {
      algorithm  = "rsasha256"
      key_length = 2048
      key_type   = "keySigning"
    }
    default_key_specs {
      algorithm  = "rsasha256"
      key_length = 1024
      key_type   = "zoneSigning"
    }
  }
}

# Output nameservers
output "toplead_nameservers" {
  value       = google_dns_managed_zone.toplead.name_servers
  description = "Nameservers for toplead.app"
}

output "topleaderplatform_nameservers" {
  value       = google_dns_managed_zone.topleaderplatform.name_servers
  description = "Nameservers for topleaderplatform.io"
}
