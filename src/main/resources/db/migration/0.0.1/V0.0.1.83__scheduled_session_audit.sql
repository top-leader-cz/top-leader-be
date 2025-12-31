-- Add audit columns to scheduled_session
ALTER TABLE scheduled_session ADD COLUMN created_at TIMESTAMP;
ALTER TABLE scheduled_session ADD COLUMN updated_at TIMESTAMP;

-- Set default values for existing sessions where time is in the past
UPDATE scheduled_session
SET created_at = time - INTERVAL '5 days',
    updated_at = time + INTERVAL '2 hours'
WHERE time < CURRENT_TIMESTAMP;

-- Set current timestamp for future sessions
UPDATE scheduled_session
SET created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE time >= CURRENT_TIMESTAMP;

-- Make columns NOT NULL
ALTER TABLE scheduled_session ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE scheduled_session ALTER COLUMN updated_at SET NOT NULL;

-- Set default for new records
ALTER TABLE scheduled_session ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE scheduled_session ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- Add index on time column for efficient time-based queries
CREATE INDEX idx_scheduled_session_time ON scheduled_session(time);
