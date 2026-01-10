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
