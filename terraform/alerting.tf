# =============================================================================
# Cloud Monitoring Alerting
# =============================================================================
# This file manages alerting policies and notification channels.

# -----------------------------------------------------------------------------
# Notification Channels
# -----------------------------------------------------------------------------

resource "google_monitoring_notification_channel" "email" {
  for_each     = toset(var.alert_emails)
  display_name = "topleader-alerts-${replace(each.value, "@", "-at-")}"
  type         = "email"
  project      = var.project_id

  labels = {
    email_address = each.value
  }

  depends_on = [google_project_service.monitoring]
}

resource "google_monitoring_notification_channel" "sms" {
  count        = var.alert_sms_number != null ? 1 : 0
  display_name = "topleader-alerts-sms"
  type         = "sms"
  project      = var.project_id

  labels = {
    number = var.alert_sms_number
  }

  depends_on = [google_project_service.monitoring]
}

locals {
  all_email_channels = [for ch in google_monitoring_notification_channel.email : ch.name]
}

# -----------------------------------------------------------------------------
# Existing Alert: Topleader PROD Errors (from logs)
# -----------------------------------------------------------------------------

resource "google_monitoring_alert_policy" "prod_error" {
  display_name = "Topleader-prod error"
  project      = var.project_id
  combiner     = "OR"
  severity     = "ERROR"

  conditions {
    display_name = "Logging Bucket - logging/user/topleader-metrics"

    condition_threshold {
      filter          = "resource.type = \"logging_bucket\" AND metric.type = \"logging.googleapis.com/user/topleader-metrics\""
      comparison      = "COMPARISON_GT"
      threshold_value = 0
      duration        = "0s"

      aggregations {
        alignment_period   = "300s"
        per_series_aligner = "ALIGN_MAX"
      }

      trigger {
        count = 1
      }
    }
  }

  alert_strategy {
    auto_close = "1800s"
  }

  notification_channels = local.all_email_channels

  documentation {
    content   = "There is an error on topleader prod\n\n$${message}"
    mime_type = "text/markdown"
    subject   = "topleader prod error"
  }

  depends_on = [google_project_service.monitoring, google_project_service.logging]
}

# -----------------------------------------------------------------------------
# Cloud Scheduler Job Failure Alerts
# -----------------------------------------------------------------------------

resource "google_monitoring_alert_policy" "scheduler_job_failed" {
  display_name = "Cloud Scheduler Job Failed"
  project      = var.project_id
  combiner     = "OR"
  severity     = "ERROR"

  conditions {
    display_name = "Cloud Scheduler job execution failed"

    condition_matched_log {
      filter = <<-EOT
        resource.type="cloud_scheduler_job"
        severity>=ERROR
      EOT

      label_extractors = {
        "job_name" = "EXTRACT(resource.labels.job_id)"
      }
    }
  }

  alert_strategy {
    notification_rate_limit {
      period = "300s"
    }
    auto_close = "1800s"
  }

  notification_channels = local.all_email_channels

  documentation {
    content   = <<-EOT
      ## Cloud Scheduler Job Failed

      A Cloud Scheduler job has failed to execute.

      ### Troubleshooting Steps:
      1. Check Cloud Scheduler logs in GCP Console
      2. Verify the target endpoint is accessible
      3. Check authentication credentials (JOB_TRIGGER_PASSWORD in Secret Manager)
      4. Review Cloud Run / App Engine logs for errors

      ### Quick Commands:
      ```bash
      # Check job status
      gcloud scheduler jobs describe JOB_NAME --location=europe-west3

      # View recent logs
      gcloud logging read 'resource.type="cloud_scheduler_job"' --limit=20

      # Test endpoint manually
      curl -X POST "https://qa.topleaderplatform.io/api/protected/jobs/ENDPOINT" \
        -H "Authorization: Basic $(echo -n 'job-trigger:PASSWORD' | base64)"
      ```

      ### Common Issues:
      - Password mismatch between Scheduler and target service
      - Target service is down or unresponsive
      - Network/firewall issues
    EOT
    mime_type = "text/markdown"
    subject   = "Cloud Scheduler Job Failed"
  }

  depends_on = [google_project_service.monitoring, google_project_service.logging]
}

