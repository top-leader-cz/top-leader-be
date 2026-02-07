# Cloud SQL PostgreSQL Instance
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
      ipv4_enabled = true
      ssl_mode     = "ENCRYPTED_ONLY"

      dynamic "authorized_networks" {
        for_each = var.sql_authorized_networks
        content {
          name  = authorized_networks.value.name
          value = authorized_networks.value.value
        }
      }
    }

    insights_config {
      query_insights_enabled  = true
      query_plans_per_minute  = 5
      query_string_length     = 1024
    }
  }

  lifecycle {
    ignore_changes = [settings[0].maintenance_window]
  }
}
