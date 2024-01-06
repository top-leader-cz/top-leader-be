ALTER TABLE feedback_form
    DROP CONSTRAINT feedback_form_username_fkey,
    ADD CONSTRAINT feedback_form_username_fkey
        FOREIGN KEY (username)
            REFERENCES users (username)
            ON DELETE CASCADE;