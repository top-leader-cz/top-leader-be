# =============================================================================
# Google Cloud APIs
# =============================================================================
# This file manages all API enablement for the project.
# APIs are organized by category for easier maintenance.

# -----------------------------------------------------------------------------
# Compute & Runtime APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "compute" {
  project            = var.project_id
  service            = "compute.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "appengine" {
  project            = var.project_id
  service            = "appengine.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "run" {
  project            = var.project_id
  service            = "run.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Database APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "sqladmin" {
  project            = var.project_id
  service            = "sqladmin.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "sql_component" {
  project            = var.project_id
  service            = "sql-component.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Storage & Artifacts APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "storage" {
  project            = var.project_id
  service            = "storage.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "storage_api" {
  project            = var.project_id
  service            = "storage-api.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "artifactregistry" {
  project            = var.project_id
  service            = "artifactregistry.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "containerregistry" {
  project            = var.project_id
  service            = "containerregistry.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Security & IAM APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "secretmanager" {
  project            = var.project_id
  service            = "secretmanager.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "iam" {
  project            = var.project_id
  service            = "iam.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "iamcredentials" {
  project            = var.project_id
  service            = "iamcredentials.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "certificatemanager" {
  project            = var.project_id
  service            = "certificatemanager.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Networking APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "dns" {
  project            = var.project_id
  service            = "dns.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "servicenetworking" {
  project            = var.project_id
  service            = "servicenetworking.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "vpcaccess" {
  project            = var.project_id
  service            = "vpcaccess.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Scheduling APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "scheduler" {
  project            = var.project_id
  service            = "cloudscheduler.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Build & Deploy APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "cloudbuild" {
  project            = var.project_id
  service            = "cloudbuild.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Monitoring & Logging APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "logging" {
  project            = var.project_id
  service            = "logging.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "monitoring" {
  project            = var.project_id
  service            = "monitoring.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "cloudtrace" {
  project            = var.project_id
  service            = "cloudtrace.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Service Management APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "servicemanagement" {
  project            = var.project_id
  service            = "servicemanagement.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "serviceusage" {
  project            = var.project_id
  service            = "serviceusage.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Resource Manager APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "cloudresourcemanager" {
  project            = var.project_id
  service            = "cloudresourcemanager.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Firebase APIs
# -----------------------------------------------------------------------------

resource "google_project_service" "firebase" {
  project            = var.project_id
  service            = "firebase.googleapis.com"
  disable_on_destroy = false
}

# -----------------------------------------------------------------------------
# Billing API
# -----------------------------------------------------------------------------

resource "google_project_service" "cloudbilling" {
  project            = var.project_id
  service            = "cloudbilling.googleapis.com"
  disable_on_destroy = false
}
