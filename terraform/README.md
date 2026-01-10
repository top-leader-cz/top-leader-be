# Terraform Infrastructure

This directory contains Terraform configuration for the TopLeader GCP infrastructure.

## Prerequisites

- Terraform >= 1.5.0
- Google Cloud SDK (`gcloud`)
- Authenticated with GCP: `gcloud auth application-default login`

## Setup

1. Copy the example tfvars file:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   ```

2. Edit `terraform.tfvars` with your IP address for Cloud SQL access

3. Initialize Terraform:
   ```bash
   terraform init
   ```

4. Review and apply changes:
   ```bash
   terraform plan
   terraform apply
   ```

## Local Database Access

To connect to Cloud SQL from your local machine (e.g., IntelliJ Database View):

1. Install Cloud SQL Proxy (one-time):
   ```bash
   brew install cloud-sql-proxy
   ```

2. Start the proxy before connecting:
   ```bash
   cloud-sql-proxy topleader-394306:europe-west3:top-leader-db --port=5432
   ```

3. Connect using:
   - Host: `localhost`
   - Port: `5432`
   - Database: `top_leader_prod`
   - User: `top-leader-prod`

## Infrastructure Components

- **App Engine** - Backend API (QA + PROD services)
- **Cloud SQL** - PostgreSQL 15 database
- **Cloud Storage** - Frontend static files with CDN
- **Load Balancer** - HTTPS routing for frontend + API
- **Cloud DNS** - DNS zones for topleaderplatform.io

## State

Terraform state is stored in GCS bucket: `gs://topleader-terraform-state/`
