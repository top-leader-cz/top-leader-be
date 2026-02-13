variable "project_id" {
  description = "GCP Project ID"
  type        = string
  default     = "topleader-394306"
}

variable "region" {
  description = "GCP Region"
  type        = string
  default     = "europe-west3"
}

variable "zone" {
  description = "GCP Zone"
  type        = string
  default     = "europe-west3-a"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "job_trigger_password" {
  description = "Password for Cloud Scheduler jobs to authenticate with /api/protected/jobs endpoints"
  type        = string
  sensitive   = true
}

variable "cloud_run_url_qa" {
  description = "Cloud Run service URL for QA environment"
  type        = string
  default     = "https://qa.topleaderplatform.io"
}

variable "cloud_run_url_prod" {
  description = "Cloud Run service URL for PROD environment"
  type        = string
  default     = "https://topleaderplatform.io"
}

variable "alert_emails" {
  description = "List of email addresses for alert notifications"
  type        = list(string)
}

variable "alert_sms_number" {
  description = "Phone number for SMS alerts (E.164 format, e.g. +4201234yes56789).."
  type        = string
  default     = null
}
