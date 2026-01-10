#!/bin/bash
# Import script for existing GCP resources into Terraform state
# Run this after `terraform init`

set -e

PROJECT_ID="topleader-394306"
REGION="europe-west3"

echo "=== Importing existing GCP resources into Terraform state ==="

# App Engine Application
echo "Importing App Engine..."
terraform import google_app_engine_application.app "${PROJECT_ID}"

# Cloud SQL
echo "Importing Cloud SQL..."
terraform import google_sql_database_instance.main "${PROJECT_ID}/top-leader-db"

# DNS Zones
echo "Importing DNS zones..."
terraform import google_dns_managed_zone.toplead "${PROJECT_ID}/toplead"
terraform import google_dns_managed_zone.topleaderplatform "${PROJECT_ID}/topleaderplatform-io"

# Storage Buckets
echo "Importing Storage buckets..."
terraform import google_storage_bucket.frontend_qa "${PROJECT_ID}/www.qa.topleaderplatform.io"
terraform import google_storage_bucket.frontend_prod "${PROJECT_ID}/www.topleaderplatform.io"
terraform import google_storage_bucket.ai_images "${PROJECT_ID}/ai-images-top-leader"

# Backend Buckets (CDN)
echo "Importing Backend Buckets..."
terraform import google_compute_backend_bucket.frontend_qa "${PROJECT_ID}/qa-topleaderplatform-io"
terraform import google_compute_backend_bucket.frontend_prod "${PROJECT_ID}/topleaderplatform-io"

# Serverless NEGs
echo "Importing Serverless NEGs..."
terraform import google_compute_region_network_endpoint_group.appengine_qa "${PROJECT_ID}/${REGION}/top-be-qa"
terraform import google_compute_region_network_endpoint_group.appengine_prod "${PROJECT_ID}/${REGION}/top-be-prod"

# Backend Services
echo "Importing Backend Services..."
terraform import google_compute_backend_service.qa_backend "${PROJECT_ID}/top-be"
terraform import google_compute_backend_service.prod_backend "${PROJECT_ID}/top-be-prod"

# URL Maps
echo "Importing URL Maps..."
terraform import google_compute_url_map.qa "${PROJECT_ID}/topleader-qa-lbc"
terraform import google_compute_url_map.prod "${PROJECT_ID}/topleader-prod-lob"

# HTTP Target Proxies
echo "Importing HTTP Target Proxies..."
terraform import google_compute_target_http_proxy.qa "${PROJECT_ID}/topleader-qa-lbc-target-proxy"
terraform import google_compute_target_http_proxy.prod "${PROJECT_ID}/topleader-prod-lob-target-proxy"

# SSL Certificates
echo "Importing SSL Certificates..."
terraform import google_compute_managed_ssl_certificate.qa "${PROJECT_ID}/qa-topleaderplatform-io-cert2"
terraform import google_compute_managed_ssl_certificate.prod "${PROJECT_ID}/topleaderplatform-io-cert"

# HTTPS Target Proxies
echo "Importing HTTPS Target Proxies..."
terraform import google_compute_target_https_proxy.qa "${PROJECT_ID}/topleader-qa-lbc-target-proxy-2"
terraform import google_compute_target_https_proxy.prod "${PROJECT_ID}/topleader-prod-lob-target-proxy-2"

# Forwarding Rules
echo "Importing Forwarding Rules..."
terraform import google_compute_global_forwarding_rule.qa_http "${PROJECT_ID}/topleader-qa-lbc-forwarding-rule"
terraform import google_compute_global_forwarding_rule.qa_https "${PROJECT_ID}/topleader-qa-lbc-forwarding-rule-2"
terraform import google_compute_global_forwarding_rule.prod_http "${PROJECT_ID}/prod-rule-http"
terraform import google_compute_global_forwarding_rule.prod_https "${PROJECT_ID}/prod-rule-https"

echo ""
echo "=== Import complete! ==="
echo "Run 'terraform plan' to verify there are no changes."
