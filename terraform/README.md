# Terraform Infrastructure

This directory contains Terraform configuration for the TopLeader GCP infrastructure.

## Prerequisites

- Terraform >= 1.5.0
- Google Cloud SDK (`gcloud`)
- Authenticated with GCP: `gcloud auth application-default login`

## Setup

1. Initialize Terraform (one-time):
   ```bash
   terraform init
   ```

2. Review changes:
   ```bash
   terraform plan -var="job_trigger_password=YOUR_SECURE_PASSWORD"
   ```

3. Apply infrastructure:
   ```bash
   terraform apply -var="job_trigger_password=YOUR_SECURE_PASSWORD"
   ```

   **Note:** The `job_trigger_password` must match the `JOB_TRIGGER_PASSWORD` environment variable set in your App Engine services (QA and PROD).

### First-time Cloud Run Setup

For initial Cloud Run deployment, apply infrastructure in order:

```bash
# 1. Create Artifact Registry + Service Account + IAM permissions
terraform apply \
  -target=google_artifact_registry_repository.cloud_run \
  -target=google_service_account.cloud_run_qa \
  -target=google_project_iam_member.cloud_run_sql_client \
  -target=google_secret_manager_secret_iam_member.cloud_run_secrets

# 2. Deploy Cloud Run service via GitHub Actions
make deploy-qa

# 3. Create domain mapping + DNS
terraform apply \
  -target=google_cloud_run_domain_mapping.qa \
  -target=google_dns_record_set.qa_cloudrun

# 4. Verify everything
terraform apply  # Apply any remaining resources
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

- **App Engine** - Backend API (PROD service)
- **Cloud Run** - Backend API (QA service) - Containerized deployment
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

## Cloud Run Domain Mapping

The QA environment uses Cloud Run with custom domain mapping:

- **Service**: `top-leader-qa`
- **Domain**: `qa.topleaderplatform.io`
- **Region**: `europe-west3`

### Prerequisites for Domain Mapping

Before applying Terraform for domain mapping, ensure:

1. Cloud Run service exists:
   ```bash
   gcloud run services describe top-leader-qa \
     --region=europe-west3 \
     --project=topleader-394306
   ```

2. If service doesn't exist, deploy it first:
   ```bash
   make deploy-qa
   ```

### Applying Domain Mapping

```bash
# Initialize Terraform (if not done)
terraform init

# Review changes
terraform plan

# Apply domain mapping
terraform apply -target=google_cloud_run_domain_mapping.qa -target=google_dns_record_set.qa_cloudrun

# Or apply all changes
terraform apply
```

### Verifying Domain Mapping

After applying, verify the mapping:

```bash
# Check domain mapping status
gcloud run domain-mappings describe \
  --domain=qa.topleaderplatform.io \
  --region=europe-west3 \
  --project=topleader-394306

# Test DNS resolution
dig qa.topleaderplatform.io

# Test HTTPS endpoint
curl -I https://qa.topleaderplatform.io/actuator/health
```

### Troubleshooting Domain Mapping

If domain mapping fails:

1. **Service not found**: Deploy Cloud Run service first via `make deploy-qa`
2. **DNS propagation**: Wait 5-10 minutes for DNS changes to propagate
3. **Certificate provisioning**: SSL certificate can take 15-60 minutes to provision

Check status:
```bash
terraform output cloudrun_qa_domain_mapping_status
```

## State

Terraform state is stored in GCS bucket: `gs://topleader-terraform-state/`
