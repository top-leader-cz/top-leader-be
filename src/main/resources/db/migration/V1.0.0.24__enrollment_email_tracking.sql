ALTER TABLE program_participant
    ADD COLUMN IF NOT EXISTS enrollment_email_scheduled_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS enrollment_email_sent_at      TIMESTAMP,
    ADD COLUMN IF NOT EXISTS new_user                      BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_pp_enrollment_pending
    ON program_participant (enrollment_email_scheduled_at)
    WHERE enrollment_email_sent_at IS NULL;
