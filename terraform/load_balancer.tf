# =============================================================================
# QA ENVIRONMENT LOAD BALANCER
# =============================================================================

# Backend Service for QA App Engine
resource "google_compute_backend_service" "qa_backend" {
  name                            = "top-be"
  project                         = var.project_id
  protocol                        = "HTTPS"
  port_name                       = "http"
  timeout_sec                     = 30
  connection_draining_timeout_sec = 0
  load_balancing_scheme           = "EXTERNAL_MANAGED"
  locality_lb_policy              = "ROUND_ROBIN"
  enable_cdn                      = false

  backend {
    group           = google_compute_region_network_endpoint_group.appengine_qa.id
    balancing_mode  = "UTILIZATION"
    capacity_scaler = 1
  }

  log_config {
    enable = false
  }
}

# URL Map for QA
resource "google_compute_url_map" "qa" {
  name            = "topleader-qa-lbc"
  project         = var.project_id
  default_service = google_compute_backend_bucket.frontend_qa.id

  host_rule {
    hosts        = ["qa.topleaderplatform.io", "www.qa.topleaderplatform.io", "34.36.149.115", "34.160.238.170"]
    path_matcher = "path-matcher-1"
  }

  path_matcher {
    name            = "path-matcher-1"
    default_service = google_compute_backend_bucket.frontend_qa.id

    path_rule {
      paths   = ["/api/*", "/login", "/swagger-ui/*", "/v3/api-docs/*", "/v3/*", "/login/google", "/login/calendly"]
      service = google_compute_backend_service.qa_backend.id
    }
  }
}

# HTTP Target Proxy for QA (redirects to HTTPS)
resource "google_compute_target_http_proxy" "qa" {
  name    = "topleader-qa-lbc-target-proxy"
  project = var.project_id
  url_map = google_compute_url_map.qa.id
}

# Managed SSL Certificate for QA
resource "google_compute_managed_ssl_certificate" "qa" {
  name    = "qa-topleaderplatform-io-cert2"
  project = var.project_id

  managed {
    domains = ["qa.topleaderplatform.io"]
  }
}

# HTTPS Target Proxy for QA
resource "google_compute_target_https_proxy" "qa" {
  name             = "topleader-qa-lbc-target-proxy-2"
  project          = var.project_id
  url_map          = google_compute_url_map.qa.id
  ssl_certificates = [google_compute_managed_ssl_certificate.qa.id]
}

# HTTP Forwarding Rule for QA
resource "google_compute_global_forwarding_rule" "qa_http" {
  name                  = "topleader-qa-lbc-forwarding-rule"
  project               = var.project_id
  target                = google_compute_target_http_proxy.qa.id
  ip_address            = "34.36.149.115"
  ip_version            = "IPV4"
  port_range            = "80"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  ip_protocol           = "TCP"
}

# HTTPS Forwarding Rule for QA
resource "google_compute_global_forwarding_rule" "qa_https" {
  name                  = "topleader-qa-lbc-forwarding-rule-2"
  project               = var.project_id
  target                = google_compute_target_https_proxy.qa.id
  ip_address            = "34.160.238.170"
  ip_version            = "IPV4"
  port_range            = "443"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  ip_protocol           = "TCP"
}

# =============================================================================
# PROD ENVIRONMENT LOAD BALANCER
# =============================================================================

# Backend Service for PROD App Engine
resource "google_compute_backend_service" "prod_backend" {
  name                            = "top-be-prod"
  project                         = var.project_id
  protocol                        = "HTTPS"
  port_name                       = "http"
  timeout_sec                     = 30
  connection_draining_timeout_sec = 0
  load_balancing_scheme           = "EXTERNAL_MANAGED"
  locality_lb_policy              = "ROUND_ROBIN"
  enable_cdn                      = false

  backend {
    group           = google_compute_region_network_endpoint_group.appengine_prod.id
    balancing_mode  = "UTILIZATION"
    capacity_scaler = 1
  }

  log_config {
    enable = false
  }
}

# URL Map for PROD
resource "google_compute_url_map" "prod" {
  name            = "topleader-prod-lob"
  project         = var.project_id
  default_service = google_compute_backend_bucket.frontend_prod.id

  host_rule {
    hosts        = ["topleaderplatform.io", "www.topleaderplatform.io", "34.144.216.127", "34.128.134.109"]
    path_matcher = "path-matcher-1"
  }

  path_matcher {
    name            = "path-matcher-1"
    default_service = google_compute_backend_bucket.frontend_prod.id

    path_rule {
      paths   = ["/api/*", "/login", "/swagger-ui/*", "/v3/*", "/login/google", "/login/calendly"]
      service = google_compute_backend_service.prod_backend.id
    }
  }
}

# HTTP Target Proxy for PROD
resource "google_compute_target_http_proxy" "prod" {
  name    = "topleader-prod-lob-target-proxy"
  project = var.project_id
  url_map = google_compute_url_map.prod.id
}

# Managed SSL Certificate for PROD (only topleaderplatform.io - www is separate)
resource "google_compute_managed_ssl_certificate" "prod" {
  name    = "topleaderplatform-io-cert"
  project = var.project_id

  managed {
    domains = ["topleaderplatform.io"]
  }
}

# HTTPS Target Proxy for PROD
resource "google_compute_target_https_proxy" "prod" {
  name             = "topleader-prod-lob-target-proxy-2"
  project          = var.project_id
  url_map          = google_compute_url_map.prod.id
  ssl_certificates = [google_compute_managed_ssl_certificate.prod.id]
}

# HTTP Forwarding Rule for PROD
resource "google_compute_global_forwarding_rule" "prod_http" {
  name                  = "prod-rule-http"
  project               = var.project_id
  target                = google_compute_target_http_proxy.prod.id
  ip_address            = "34.144.216.127"
  ip_version            = "IPV4"
  port_range            = "80"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  ip_protocol           = "TCP"
}

# HTTPS Forwarding Rule for PROD
resource "google_compute_global_forwarding_rule" "prod_https" {
  name                  = "prod-rule-https"
  project               = var.project_id
  target                = google_compute_target_https_proxy.prod.id
  ip_address            = "34.128.134.109"
  ip_version            = "IPV4"
  port_range            = "443"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  ip_protocol           = "TCP"
}

# =============================================================================
# OUTPUTS
# =============================================================================

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
