-- Add ON DELETE CASCADE to feedback_form_answer recipient_id foreign key
-- This allows recipients to be deleted even when they have associated answers

ALTER TABLE feedback_form_answer
    DROP CONSTRAINT IF EXISTS feedback_form_answer_recipient_id_fkey;

ALTER TABLE feedback_form_answer
    ADD CONSTRAINT feedback_form_answer_recipient_id_fkey
    FOREIGN KEY (recipient_id)
    REFERENCES fb_recipient(id)
    ON DELETE CASCADE;


-- Fix feedback_form id default to use the sequence
ALTER TABLE feedback_form ALTER COLUMN id SET DEFAULT nextval('feedback_form_id_seq');
ALTER SEQUENCE feedback_form_id_seq OWNED BY feedback_form.id;

-- Fix fb_recipient id default to use the sequence
ALTER TABLE fb_recipient ALTER COLUMN id SET DEFAULT nextval('fb_recipient_id_seq');
ALTER SEQUENCE fb_recipient_id_seq OWNED BY fb_recipient.id;

-- Fix feedback_form_question id default to use the sequence
ALTER TABLE feedback_form_question ALTER COLUMN id SET DEFAULT nextval('feedback_form_question_id_seq');
ALTER SEQUENCE feedback_form_question_id_seq OWNED BY feedback_form_question.id;

-- Fix feedback_form_answer id default to use the sequence
ALTER TABLE feedback_form_answer ALTER COLUMN id SET DEFAULT nextval('feedback_form_answer_id_seq');
ALTER SEQUENCE feedback_form_answer_id_seq OWNED BY feedback_form_answer.id;

-- Fix feedback_notification id default to use the sequence
ALTER TABLE feedback_notification ALTER COLUMN id SET DEFAULT nextval('feedback_notification_id_seq');
ALTER SEQUENCE feedback_notification_id_seq OWNED BY feedback_notification.id;

ALTER TABLE feedback_form_question DROP CONSTRAINT IF EXISTS feedback_form_question_question_key_fkey;

-- Drop the primary key constraint
ALTER TABLE fb_question DROP CONSTRAINT IF EXISTS fb_question_pkey;

-- Add id column as bigserial
ALTER TABLE fb_question ADD COLUMN id BIGSERIAL;

-- Set id as primary key
ALTER TABLE fb_question ADD PRIMARY KEY (id);

-- Add unique constraint on key
ALTER TABLE fb_question ADD CONSTRAINT fb_question_key_unique UNIQUE (key);

-- Re-add foreign key constraint referencing key (not id) since feedback_form_question uses question_key
ALTER TABLE feedback_form_question
    ADD CONSTRAINT feedback_form_question_question_key_fkey
        FOREIGN KEY (question_key) REFERENCES fb_question(key);

