-- Add ON DELETE CASCADE to feedback_form_answer recipient_id foreign key
-- This allows recipients to be deleted even when they have associated answers

ALTER TABLE feedback_form_answer
    DROP CONSTRAINT IF EXISTS feedback_form_answer_recipient_id_fkey;

ALTER TABLE feedback_form_answer
    ADD CONSTRAINT feedback_form_answer_recipient_id_fkey
    FOREIGN KEY (recipient_id)
    REFERENCES fb_recipient(id)
    ON DELETE CASCADE;
