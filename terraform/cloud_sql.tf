# =============================================================================
# VPC Network for Private Cloud SQL
# =============================================================================

resource "google_compute_network" "main" {
  name                    = "top-leader-vpc"
  project                 = var.project_id
  auto_create_subnetworks = true

  depends_on = [google_project_service.compute]
}

resource "google_compute_global_address" "private_ip_range" {
  name          = "top-leader-private-ip"
  project       = var.project_id
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.main.id

  depends_on = [google_project_service.servicenetworking]
}

resource "google_service_networking_connection" "private_vpc" {
  network                 = google_compute_network.main.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_range.name]

  depends_on = [google_project_service.servicenetworking]
}

# =============================================================================
# Cloud SQL PostgreSQL Instance
# =============================================================================

resource "google_sql_database_instance" "main" {
  name                = "top-leader-db"
  project             = var.project_id
  region              = var.region
  database_version    = "POSTGRES_15"
  deletion_protection = true

  settings {
    tier              = "db-custom-2-3840"
    availability_type = "ZONAL"
    edition           = "ENTERPRISE"
    disk_type         = "PD_SSD"
    disk_size         = 10
    disk_autoresize   = true

    location_preference {
      zone = var.zone
    }

    backup_configuration {
      enabled                        = true
      start_time                     = "09:00"
      location                       = "eu"
      point_in_time_recovery_enabled = true
      backup_retention_settings {
        retained_backups = 7
        retention_unit   = "COUNT"
      }
      transaction_log_retention_days = 7
    }

    # maintenance_window is not set in GCP (day=0 means any day)
    # Terraform requires day 1-7, so we skip this block

    deletion_protection_enabled = true

    ip_configuration {
      ipv4_enabled    = false
      private_network = google_compute_network.main.id
      ssl_mode        = "ENCRYPTED_ONLY"
    }

    insights_config {
      query_insights_enabled  = true
      query_plans_per_minute  = 5
      query_string_length     = 1024
    }
  }

  depends_on = [google_service_networking_connection.private_vpc]

  lifecycle {
    ignore_changes = [settings[0].maintenance_window]
  }
}
