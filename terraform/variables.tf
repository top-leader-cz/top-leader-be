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

variable "sql_authorized_networks" {
  description = "List of authorized networks for Cloud SQL"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

# Temporarily commented out - will be configured later
# variable "job_trigger_password" {
#   description = "Password for Cloud Scheduler jobs to authenticate with /api/protected/jobs endpoints"
#   type        = string
#   sensitive   = true
# }
