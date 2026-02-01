# Terraform Infrastructure

This directory contains Terraform configuration for the TopLeader GCP infrastructure.

## Prerequisites

- Terraform >= 1.5.0
- Google Cloud SDK (`gcloud`)
- Authenticated with GCP: `gcloud auth application-default login`

## Setup

1. Copy and configure variables:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your values
   ```

2. Initialize Terraform (one-time):
   ```bash
   terraform init
   ```

3. Review changes:
   ```bash
   terraform plan
   ```

4. Apply infrastructure:
   ```bash
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
   cloud-sql-proxy <PROJECT_ID>:<REGION>:<INSTANCE_NAME> --port=5432
   ```

3. Connect using:
   - Host: `localhost`
   - Port: `5432`
   - Database: (see terraform output)
   - User: (see terraform output)

## Infrastructure Components

- **App Engine** - Backend API (PROD service)
- **Cloud Run** - Backend API (QA service) - Containerized deployment
- **Cloud SQL** - PostgreSQL 15 database
- **Cloud Storage** - Frontend static files with CDN
- **Load Balancer** - HTTPS routing for frontend + API
- **Cloud DNS** - DNS zones
- **Cloud Scheduler** - Scheduled jobs for both QA and PROD environments
- **Cloud Monitoring** - Alerting for failed scheduler jobs

## Architecture

### QA Environment - Load Balancer Routing

The QA environment uses a Load Balancer to route traffic between frontend (static files) and backend (API):

```
                         ┌──────────────────────────┐
                         │      Load Balancer       │
                         │      qa.<domain>         │
                         └────────────┬─────────────┘
                                      │
                    ┌─────────────────┴─────────────────┐
                    │                                   │
                    ▼                                   ▼
          ┌─────────────────┐                 ┌─────────────────┐
          │  Cloud Storage  │                 │   Cloud Run     │
          │    (Frontend)   │                 │    (Backend)    │
          │                 │                 │                 │
          │  /index.html    │                 │  /api/*         │
          │  /assets/*      │                 │                 │
          │  /*.js, /*.css  │                 │                 │
          └─────────────────┘                 └─────────────────┘
```

**Why Load Balancer?**
- Single domain for both FE and BE
- SSL termination at LB level
- Path-based routing (`/api/*` -> Cloud Run, everything else -> Storage)

**Note:** Cloud Scheduler jobs use Basic Auth (not OIDC) because OIDC tokens don't work through Load Balancer.

### PROD Environment

PROD uses App Engine which handles routing internally:

```
          ┌──────────────────────────┐
          │       App Engine         │
          │        <domain>          │
          └────────────┬─────────────┘
                       │
         ┌─────────────┴─────────────┐
         │                           │
         ▼                           ▼
   ┌───────────┐              ┌───────────┐
   │  default  │              │   prod    │
   │ (Frontend)│              │ (Backend) │
   └───────────┘              └───────────┘
```

## Cloud Scheduler Jobs

The infrastructure includes 5 scheduled jobs for each environment (QA and PROD):

| Job | Description | Endpoint |
|-----|-------------|----------|
| displayed-messages | Notify about not displayed messages | `/api/protected/jobs/displayedMessages` |
| feedback-notification | Send feedback notifications | `/api/protected/jobs/feedback-notification` |
| mark-session-completed | Mark old pending sessions as completed | `/api/protected/jobs/mark-session-completed` |
| remind-sessions | Remind users to schedule sessions | `/api/protected/jobs/remind-sessions` |
| process-payments | Process scheduled payments | `/api/protected/jobs/payments` |

### Authentication

All jobs use HTTP Basic Authentication:
- Username: `job-trigger`
- Password: Configured via `job_trigger_password` in `terraform.tfvars`

The password must match the `JOB_TRIGGER_PASSWORD` secret in Secret Manager.

### Testing Jobs

```bash
# Run a job manually
gcloud scheduler jobs run <JOB_NAME> --location=<REGION>

# Check job status
gcloud scheduler jobs describe <JOB_NAME> --location=<REGION>

# View job logs
gcloud logging read 'resource.type="cloud_scheduler_job"' --limit=10
```

## Alerting

Cloud Monitoring alerts are configured for application errors and Cloud Scheduler job failures.

### Configuration

Alert recipients are configured via `terraform.tfvars` (not committed to repo):

```hcl
# terraform.tfvars
alert_emails = [
  "alerts@example.com",
  "developer@example.com"
]

# Optional: SMS alerts
# alert_sms_number = "+1234567890"
```

### Alert Policies

| Policy | Trigger | Severity |
|--------|---------|----------|
| **Prod error** | Errors in application logs (log-based metric) | ERROR |
| **Cloud Scheduler Job Failed** | ERROR logs from scheduler jobs | ERROR |

### Viewing Alerts

```bash
# List alert policies
gcloud alpha monitoring policies list

# View incidents (requires alpha component)
gcloud alpha monitoring incidents list
```

## Terraform State

Terraform state is stored remotely in a GCS bucket. The bucket name is configured in `versions.tf`.

## Useful Commands

```bash
# Show all outputs
terraform output

# Show specific output
terraform output sql_connection_name

# Import existing resource
terraform import <RESOURCE_TYPE>.<NAME> <RESOURCE_ID>

# Destroy specific resource (use with caution!)
terraform destroy -target=<RESOURCE_TYPE>.<NAME>
```
