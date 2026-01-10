# GCS Bucket for QA Frontend
resource "google_storage_bucket" "frontend_qa" {
  name          = "www.qa.topleaderplatform.io"
  project       = var.project_id
  location      = "EUROPE-WEST3"
  force_destroy = false

  website {
    main_page_suffix = "index.html"
    not_found_page   = "index.html"
  }

  uniform_bucket_level_access = true
}

# GCS Bucket for PROD Frontend
resource "google_storage_bucket" "frontend_prod" {
  name          = "www.topleaderplatform.io"
  project       = var.project_id
  location      = "EU"
  force_destroy = false

  website {
    main_page_suffix = "index.html"
    not_found_page   = "index.html"
  }

  uniform_bucket_level_access = true
}

# GCS Bucket for AI images
resource "google_storage_bucket" "ai_images" {
  name          = "ai-images-top-leader"
  project       = var.project_id
  location      = "EU"
  force_destroy = false

  uniform_bucket_level_access = true
}

# Make QA frontend bucket publicly readable
resource "google_storage_bucket_iam_member" "frontend_qa_public" {
  bucket = google_storage_bucket.frontend_qa.name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}

# Make PROD frontend bucket publicly readable
resource "google_storage_bucket_iam_member" "frontend_prod_public" {
  bucket = google_storage_bucket.frontend_prod.name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}

# Backend Bucket for QA with CDN
resource "google_compute_backend_bucket" "frontend_qa" {
  name             = "qa-topleaderplatform-io"
  project          = var.project_id
  bucket_name      = google_storage_bucket.frontend_qa.name
  enable_cdn       = true
  compression_mode = "DISABLED"

  cdn_policy {
    cache_mode        = "CACHE_ALL_STATIC"
    client_ttl        = 3600
    default_ttl       = 3600
    max_ttl           = 86400
    negative_caching  = false
    request_coalescing = true
    serve_while_stale = 0
  }
}

# Backend Bucket for PROD with CDN
resource "google_compute_backend_bucket" "frontend_prod" {
  name             = "topleaderplatform-io"
  project          = var.project_id
  bucket_name      = google_storage_bucket.frontend_prod.name
  enable_cdn       = true
  compression_mode = "DISABLED"

  cdn_policy {
    cache_mode        = "CACHE_ALL_STATIC"
    client_ttl        = 3600
    default_ttl       = 3600
    max_ttl           = 86400
    negative_caching  = false
    request_coalescing = true
    serve_while_stale = 0
  }
}
