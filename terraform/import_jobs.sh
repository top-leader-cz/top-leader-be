#!/bin/bash
set -e

PROJECT="topleader-394306"
REGION="europe-west3"

echo "Importing QA jobs..."
terraform import google_cloud_scheduler_job.message_undisplayed_qa projects/${PROJECT}/locations/${REGION}/jobs/message-undisplayed-qa
terraform import google_cloud_scheduler_job.feedback_notifications_qa projects/${PROJECT}/locations/${REGION}/jobs/feedback-notifications-qa
terraform import google_cloud_scheduler_job.comple_session_qa projects/${PROJECT}/locations/${REGION}/jobs/comple-session-qa
terraform import google_cloud_scheduler_job.unscheduled_session_reminder_qa projects/${PROJECT}/locations/${REGION}/jobs/unscheduled_session_reminder
terraform import google_cloud_scheduler_job.payment_process_job_qa projects/${PROJECT}/locations/${REGION}/jobs/payment_process_job

echo "Importing PROD jobs..."
terraform import google_cloud_scheduler_job.message_undisplayed_prod projects/${PROJECT}/locations/${REGION}/jobs/message-undisplayed-prod
terraform import google_cloud_scheduler_job.feedback_notifications_prod projects/${PROJECT}/locations/${REGION}/jobs/feedback-notifications-prod
terraform import google_cloud_scheduler_job.unscheduled_session_reminder_prod projects/${PROJECT}/locations/${REGION}/jobs/unscheduled_session_reminder_prod
terraform import google_cloud_scheduler_job.payment_process_job_prod projects/${PROJECT}/locations/${REGION}/jobs/payment_process_job_prod

echo "Import complete!"
