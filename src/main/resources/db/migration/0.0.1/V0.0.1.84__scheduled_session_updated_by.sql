-- Add updated_by column to scheduled_session
ALTER TABLE scheduled_session ADD COLUMN updated_by VARCHAR(255);
