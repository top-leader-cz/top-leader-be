# Local variable for HTTP Basic Auth header
locals {
  job_auth_header = "Basic ${base64encode("job-trigger:${var.job_trigger_password}")}"
}

# Service Account for Cloud Scheduler to invoke Cloud Run
resource "google_service_account" "scheduler" {
  account_id   = "cloud-scheduler-jobs"
  display_name = "Cloud Scheduler Jobs Service Account"
  description  = "Service account for Cloud Scheduler to invoke Cloud Run job endpoints"
  project      = var.project_id
}

# Grant Cloud Run Invoker role to scheduler service account
# NOTE: Uncomment after Cloud Run service is deployed via GitHub Actions
# resource "google_cloud_run_service_iam_member" "scheduler_invoker_qa" {
#   location = var.region
#   project  = var.project_id
#   service  = "top-leader-qa"
#   role     = "roles/run.invoker"
#   member   = "serviceAccount:${google_service_account.scheduler.email}"
#
#   depends_on = [google_project_service.run, google_project_service.scheduler]
# }

# ============================================================================
# QA Environment Jobs (Cloud Run)
# ============================================================================

resource "google_cloud_scheduler_job" "message_undisplayed_qa" {
  name             = "message-undisplayed-qa"
  description      = "Send user notification that message was not displayed for period of time"
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

  http_target {
    http_method = "POST"
    uri         = "${var.cloud_run_url_qa}/api/protected/jobs/displayedMessages"

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}

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

  http_target {
    http_method = "POST"
    uri         = "${var.cloud_run_url_qa}/api/protected/jobs/feedback-notification"

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}

resource "google_cloud_scheduler_job" "complete_session_qa" {
  name             = "complete-session-qa"
  description      = "Complete session after 24 hours"
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

  http_target {
    http_method = "POST"
    uri         = "${var.cloud_run_url_qa}/api/protected/jobs/mark-session-completed"

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}

resource "google_cloud_scheduler_job" "unscheduled_session_reminder_qa" {
  name             = "unscheduled-session-reminder-qa"
  description      = "Remind users to schedule sessions QA"
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

  http_target {
    http_method = "POST"
    uri         = "${var.cloud_run_url_qa}/api/protected/jobs/remind-sessions"

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}


# ============================================================================
# PROD Environment Jobs (App Engine)
# ============================================================================

resource "google_cloud_scheduler_job" "message_undisplayed_prod" {
  name             = "message-undisplayed-prod"
  description      = "Send user notification that message was not displayed for period of time"
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
    http_method  = "POST"
    relative_uri = "/api/protected/jobs/displayedMessages"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}

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
    http_method  = "POST"
    relative_uri = "/api/protected/jobs/feedback-notification"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}

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
    http_method  = "POST"
    relative_uri = "/api/protected/jobs/mark-session-completed"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}

resource "google_cloud_scheduler_job" "unscheduled_session_reminder_prod" {
  name             = "unscheduled-session-reminder-prod"
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
    http_method  = "POST"
    relative_uri = "/api/protected/jobs/remind-sessions"

    app_engine_routing {
      service = "prod"
    }

    headers = {
      "Authorization" = local.job_auth_header
    }
  }

  depends_on = [google_project_service.scheduler, google_project_service.run]
}


