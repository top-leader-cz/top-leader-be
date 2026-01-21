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

2. Edit `terraform.tfvars` with required values:
   - Your IP address for Cloud SQL access
   - `job_trigger_password` - Password for Cloud Scheduler jobs authentication

3. Initialize Terraform:
   ```bash
   terraform init
   ```

4. Review and apply changes:
   ```bash
   terraform plan -var="job_trigger_password=YOUR_SECURE_PASSWORD"
   terraform apply -var="job_trigger_password=YOUR_SECURE_PASSWORD"
   ```

   **Note:** The `job_trigger_password` must match the `JOB_TRIGGER_PASSWORD` environment variable set in your App Engine services (QA and PROD).

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
- **Cloud Scheduler** - Scheduled jobs for both QA and PROD environments

## Cloud Scheduler Jobs

The infrastructure includes 5 scheduled jobs for each environment (QA and PROD):

### QA Environment

1. **qa-displayed-messages** - Process and send email notifications for not displayed messages
   - Schedule: Every 30 minutes
   - Endpoint: `/api/protected/jobs/displayedMessages`

2. **qa-feedback-notification** - Process and send feedback notifications
   - Schedule: Daily at 9:00 AM (Europe/Prague)
   - Endpoint: `/api/protected/jobs/feedback-notification`

3. **qa-mark-session-completed** - Mark pending sessions older than 48h as completed/no-show
   - Schedule: Daily at 2:00 AM (Europe/Prague)
   - Endpoint: `/api/protected/jobs/mark-session-completed`

4. **qa-remind-sessions** - Send reminders to users with no scheduled sessions
   - Schedule: Weekly on Monday at 10:00 AM (Europe/Prague)
   - Endpoint: `/api/protected/jobs/remind-sessions`

5. **qa-process-payments** - Process scheduled session payments
   - Schedule: Daily at 3:00 AM (Europe/Prague)
   - Endpoint: `/api/protected/jobs/payments`

### PROD Environment

Same 5 jobs with `prod-` prefix, identical schedules and endpoints but targeting the PROD App Engine service.

### Authentication

All jobs use HTTP Basic Authentication:
- Username: `job-trigger`
- Password: Configured via `job_trigger_password` Terraform variable

The password must match the `JOB_TRIGGER_PASSWORD` environment variable in your App Engine configuration.

## State

Terraform state is stored in GCS bucket: `gs://topleader-terraform-state/`
