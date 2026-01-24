# Local variable for HTTP Basic Auth header
locals {
  job_auth_header = "Basic ${base64encode("job-trigger:${var.job_trigger_password}")}"
}

# Service Account for Cloud Scheduler
# Note: Not currently used with app_engine_http_target
# resource "google_service_account" "scheduler" {
#   account_id   = "cloud-scheduler-jobs"
#   display_name = "Cloud Scheduler Jobs Service Account"
#   description  = "Service account for Cloud Scheduler to invoke App Engine job endpoints"
#   project      = var.project_id
# }

# ============================================================================
# QA Environment Jobs
# ============================================================================

# Job 1: Process not displayed messages
resource "google_cloud_scheduler_job" "message_undisplayed_qa" {
  name             = "message-undisplayed-qa"
  description      = "Send user notification that message wan not displayed for period of time"
  schedule         = "0 0 1 1 *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/displayedMessages"

    app_engine_routing {
      service = "qa"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 2: Trigger feedback notifications
resource "google_cloud_scheduler_job" "feedback_notifications_qa" {
  name             = "feedback-notifications-qa"
  description      = "Trigger feedback notification process QA"
  schedule         = "0 0 1 1 *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/feedback-notification"

    app_engine_routing {
      service = "qa"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 3: Mark pending sessions as no-show
resource "google_cloud_scheduler_job" "complete_session_qa" {
  name             = "comple-session-qa"
  description      = "complte seession afeter 24 hour"
  schedule         = "0 0 1 1 *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 1
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/mark-session-completed"

    app_engine_routing {
      service = "qa"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 4: Remind users to schedule sessions
resource "google_cloud_scheduler_job" "unscheduled_session_reminder_qa" {
  name             = "unscheduled_session_reminder"
  description      = "unscheduled _session_reminder_qa"
  schedule         = "0 0 1 1 *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/remind-sessions"

    app_engine_routing {
      service = "qa"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 5: Process payments
resource "google_cloud_scheduler_job" "payment_process_job_qa" {
  name             = "payment_process_job"
  description      = "Trigger payment processing"
  schedule         = "0 0 1 1 *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/payments"

    app_engine_routing {
      service = "qa"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# ============================================================================
# PROD Environment Jobs
# ============================================================================

# Job 1: Process not displayed messages
resource "google_cloud_scheduler_job" "message_undisplayed_prod" {
  name             = "message-undisplayed-prod"
  description      = "Send user notification that message wan not displayed for period of time"
  schedule         = "0 */4 * * *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/displayedMessages"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 2: Trigger feedback notifications
resource "google_cloud_scheduler_job" "feedback_notifications_prod" {
  name             = "feedback-notifications-prod"
  description      = "Trigger feedback notification process PROD"
  schedule         = "0 0 * * *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/feedback-notification"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 3: Mark pending sessions as no-show (PROD needs to be created)
resource "google_cloud_scheduler_job" "complete_session_prod" {
  name             = "complete-session-prod"
  description      = "Mark pending sessions older than 48h as completed/no-show (PROD)"
  schedule         = "0 2 * * *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 1
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/mark-session-completed"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 4: Remind users to schedule sessions
resource "google_cloud_scheduler_job" "unscheduled_session_reminder_prod" {
  name             = "unscheduled_session_reminder_prod"
  description      = "Send reminders to users with no scheduled sessions (PROD)"
  schedule         = "0 7 * * *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/remind-sessions"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}

# Job 5: Process payments
resource "google_cloud_scheduler_job" "payment_process_job_prod" {
  name             = "payment_process_job_prod"
  description      = "prod process payment job"
  schedule         = "0 */2 * * *"
  time_zone        = "Etc/UTC"
  attempt_deadline = "320s"
  region           = var.region
  project          = var.project_id

  retry_config {
    retry_count          = 0
    max_retry_duration   = "0s"
    min_backoff_duration = "5s"
    max_backoff_duration = "3600s"
    max_doublings        = 5
  }

  app_engine_http_target {
    http_method = "POST"
    relative_uri = "/api/protected/jobs/payments"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }
}
