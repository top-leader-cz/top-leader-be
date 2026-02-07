-- Migration for JPA to Spring Data JDBC conversion

-- Badge table - add id column
ALTER TABLE badge DROP CONSTRAINT badge_pkey;
ALTER TABLE badge ADD COLUMN id BIGSERIAL PRIMARY KEY;
CREATE UNIQUE INDEX badge_unique_idx ON badge(username, achievement_type, month, year);
CREATE INDEX IF NOT EXISTS badge_username_idx ON badge(username);


-- Drop old varchar languages column from coach table
ALTER TABLE coach DROP COLUMN IF EXISTS languages;

-- Add JSONB columns for languages and fields
ALTER TABLE coach ADD COLUMN languages jsonb DEFAULT '[]';
ALTER TABLE coach ADD COLUMN fields jsonb DEFAULT '[]';

-- Migrate data from coach_languages
UPDATE coach c SET languages = COALESCE(
        (SELECT jsonb_agg(cl.languages) FROM coach_languages cl WHERE cl.coach_username = c.username),
        '[]'::jsonb
                               );

-- Migrate data from coach_fields
UPDATE coach c SET fields = COALESCE(
        (SELECT jsonb_agg(cf.fields) FROM coach_fields cf WHERE cf.coach_username = c.username),
        '[]'::jsonb
                            );

-- Drop old tables
DROP TABLE coach_languages;
DROP TABLE coach_fields;

-- Add auto-generated id as primary key for coach
ALTER TABLE coach DROP CONSTRAINT IF EXISTS coach_pkey CASCADE;
ALTER TABLE coach ADD COLUMN id bigserial;
ALTER TABLE coach ADD PRIMARY KEY (id);
ALTER TABLE coach ADD CONSTRAINT coach_username_unique UNIQUE (username);

-- Recreate foreign key constraints pointing to coach.username
ALTER TABLE coach_availability_settings ADD CONSTRAINT fk_cas_users_username
    FOREIGN KEY (coach) REFERENCES coach(username) ON DELETE CASCADE;
ALTER TABLE coach_user_note ADD CONSTRAINT coach_user_note_coach_id_fkey
    FOREIGN KEY (coach_id) REFERENCES coach(username) ON DELETE CASCADE;

-- Add auto-generated id as primary key for coach_image
ALTER TABLE coach_image DROP CONSTRAINT IF EXISTS coach_image_pkey CASCADE;
ALTER TABLE coach_image ADD COLUMN id bigserial;
ALTER TABLE coach_image ADD PRIMARY KEY (id);
ALTER TABLE coach_image ADD CONSTRAINT coach_image_username_unique UNIQUE (username);

-- Convert image_data from OID (large object) to bytea
ALTER TABLE coach_image ALTER COLUMN image_data TYPE bytea USING CASE WHEN image_data IS NOT NULL THEN lo_get(image_data) ELSE NULL END;

-- Add auto-generated id as primary key for coach_availability_settings
ALTER TABLE coach_availability_settings DROP CONSTRAINT IF EXISTS coach_availability_settings_pkey CASCADE;
ALTER TABLE coach_availability_settings ADD COLUMN id bigserial;
ALTER TABLE coach_availability_settings ADD PRIMARY KEY (id);
ALTER TABLE coach_availability_settings ADD CONSTRAINT coach_availability_settings_coach_unique UNIQUE (coach);

-- =====================================================
-- USERS TABLE: Add auto-generated ID
-- =====================================================
-- Migration for adding auto-generated ID to users table (Spring Data JDBC requirement)
-- Similar to coach table migration above

-- Add auto-generated id as primary key for users
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey CASCADE;
ALTER TABLE users ADD COLUMN id bigserial;
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE users ADD CONSTRAINT users_username_unique UNIQUE (username);

-- Add index on username for faster lookups
CREATE INDEX IF NOT EXISTS users_username_idx ON users(username);

-- Add index on company_id for faster lookups (used by manager_view and other queries)
CREATE INDEX IF NOT EXISTS users_company_id_idx ON users(company_id);

-- Email already has unique constraint and index from V1.0.0.1__init.sql
-- No need to recreate

-- Recreate foreign key constraints pointing to users.username
-- These were dropped by CASCADE above

-- user_info table - add id column as primary key
ALTER TABLE user_info DROP CONSTRAINT IF EXISTS user_info_pkey CASCADE;
ALTER TABLE user_info DROP CONSTRAINT IF EXISTS fk_user_info_users CASCADE;
ALTER TABLE user_info ADD COLUMN id bigserial;
ALTER TABLE user_info ADD PRIMARY KEY (id);
ALTER TABLE user_info ADD CONSTRAINT user_info_username_unique UNIQUE (username);
ALTER TABLE user_info ADD CONSTRAINT fk_user_info_users
    FOREIGN KEY (username) REFERENCES users(username);
CREATE INDEX IF NOT EXISTS user_info_username_idx ON user_info(username);

-- coach table
ALTER TABLE coach DROP CONSTRAINT IF EXISTS fk_users_username;
ALTER TABLE coach ADD CONSTRAINT fk_users_username
    FOREIGN KEY (username) REFERENCES users(username);

-- coach_availability table
ALTER TABLE coach_availability DROP CONSTRAINT IF EXISTS fk_coach_availability_user;
ALTER TABLE coach_availability ADD CONSTRAINT fk_coach_availability_user
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS coach_availability_username_idx ON coach_availability(username);

-- coach_image table
ALTER TABLE coach_image DROP CONSTRAINT IF EXISTS fk_coach_image_users;
ALTER TABLE coach_image ADD CONSTRAINT fk_coach_image_coach
    FOREIGN KEY (username) REFERENCES coach(username) ON DELETE CASCADE;

-- token table
ALTER TABLE token DROP CONSTRAINT IF EXISTS fk_token_username;
ALTER TABLE token ADD CONSTRAINT fk_token_username
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS token_username_idx ON token(username);

-- user_action_step table
ALTER TABLE user_action_step DROP CONSTRAINT IF EXISTS user_action_step_username_fkey;
ALTER TABLE user_action_step ADD CONSTRAINT user_action_step_username_fkey
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS user_action_step_username_idx ON user_action_step(username);

-- user_insight table - add id column as primary key
ALTER TABLE user_insight DROP CONSTRAINT IF EXISTS user_insight_pkey CASCADE;
ALTER TABLE user_insight DROP CONSTRAINT IF EXISTS fk_user_insight_users CASCADE;
ALTER TABLE user_insight ADD COLUMN id bigserial;
ALTER TABLE user_insight ADD PRIMARY KEY (id);
ALTER TABLE user_insight ADD CONSTRAINT user_insight_username_unique UNIQUE (username);
ALTER TABLE user_insight ADD CONSTRAINT fk_user_insight_users
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS user_insight_username_idx ON user_insight(username);

-- users_managers table (both foreign keys)
ALTER TABLE users_managers DROP CONSTRAINT IF EXISTS fk_manager_username;
ALTER TABLE users_managers ADD CONSTRAINT fk_manager_username
    FOREIGN KEY (manager_username) REFERENCES users(username);
CREATE INDEX IF NOT EXISTS users_managers_manager_username_idx ON users_managers(manager_username);

ALTER TABLE users_managers DROP CONSTRAINT IF EXISTS fk_user_username;
ALTER TABLE users_managers ADD CONSTRAINT fk_user_username
    FOREIGN KEY (user_username) REFERENCES users(username);
CREATE INDEX IF NOT EXISTS users_managers_user_username_idx ON users_managers(user_username);

-- user_coach_rates table
ALTER TABLE user_coach_rates DROP CONSTRAINT IF EXISTS fk_ucr_username;
ALTER TABLE user_coach_rates ADD CONSTRAINT fk_ucr_username
    FOREIGN KEY (username) REFERENCES users(username);
CREATE INDEX IF NOT EXISTS user_coach_rates_username_idx ON user_coach_rates(username);

-- feedback_notification table
ALTER TABLE feedback_notification DROP CONSTRAINT IF EXISTS fk_feedback_notification_users;
ALTER TABLE feedback_notification ADD CONSTRAINT fk_feedback_notification_users
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS feedback_notification_username_idx ON feedback_notification(username);

-- calendar_sync_info table
ALTER TABLE calendar_sync_info DROP CONSTRAINT IF EXISTS pk_calendar_sync_info CASCADE;
ALTER TABLE calendar_sync_info DROP CONSTRAINT IF EXISTS fk_calendar_sync_info_info CASCADE;
ALTER TABLE calendar_sync_info ADD COLUMN IF NOT EXISTS id bigserial;
ALTER TABLE calendar_sync_info DROP CONSTRAINT IF EXISTS calendar_sync_info_pkey CASCADE;
ALTER TABLE calendar_sync_info ADD PRIMARY KEY (id);
ALTER TABLE calendar_sync_info ADD CONSTRAINT calendar_sync_info_username_sync_type_unique UNIQUE (username, sync_type);
ALTER TABLE calendar_sync_info ADD CONSTRAINT fk_calendar_sync_info_username
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS calendar_sync_info_sync_type_status_idx ON calendar_sync_info(sync_type, status);

-- scheduled_session table
ALTER TABLE scheduled_session DROP CONSTRAINT IF EXISTS scheduled_session_username_fkey;
ALTER TABLE scheduled_session ADD CONSTRAINT scheduled_session_username_fkey
    FOREIGN KEY (username) REFERENCES users(username);
CREATE INDEX IF NOT EXISTS scheduled_session_username_idx ON scheduled_session(username);
CREATE INDEX IF NOT EXISTS scheduled_session_coach_username_idx ON scheduled_session(coach_username);

-- user_allocation table
ALTER TABLE user_allocation DROP CONSTRAINT IF EXISTS user_allocation_user_id_fkey;
ALTER TABLE user_allocation ADD CONSTRAINT user_allocation_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(username);
CREATE INDEX IF NOT EXISTS user_allocation_user_id_idx ON user_allocation(user_id);

-- badge table
ALTER TABLE badge DROP CONSTRAINT IF EXISTS badge_username_fkey;
ALTER TABLE badge ADD CONSTRAINT badge_username_fkey
    FOREIGN KEY (username) REFERENCES users(username);

-- article table
ALTER TABLE article DROP CONSTRAINT IF EXISTS fk_articles_user;
ALTER TABLE article ADD CONSTRAINT fk_articles_user
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS article_username_idx ON article(username);

-- feedback_form_question table - add id column as primary key
ALTER TABLE feedback_form_question DROP CONSTRAINT IF EXISTS feedback_form_question_pkey CASCADE;
ALTER TABLE feedback_form_question ADD COLUMN IF NOT EXISTS id bigserial;
ALTER TABLE feedback_form_question ADD PRIMARY KEY (id);
-- UNIQUE constraint already exists: feedback_form_question_unique (form_id, question_key)

-- feedback_form_answer table - add id column as primary key
ALTER TABLE feedback_form_answer DROP CONSTRAINT IF EXISTS feedback_form_answer_pkey CASCADE;
ALTER TABLE feedback_form_answer ADD COLUMN IF NOT EXISTS id bigserial;
ALTER TABLE feedback_form_answer ADD PRIMARY KEY (id);
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'feedback_form_answer_unique') THEN
        ALTER TABLE feedback_form_answer ADD CONSTRAINT feedback_form_answer_unique UNIQUE (form_id, recipient_id, question_key);
    END IF;
END $$;

-- favorite_coach table - add id column as primary key
ALTER TABLE favorite_coach DROP CONSTRAINT IF EXISTS pk_favorite_coach CASCADE;
ALTER TABLE favorite_coach ADD COLUMN id bigserial;
ALTER TABLE favorite_coach ADD PRIMARY KEY (id);
ALTER TABLE favorite_coach ADD CONSTRAINT favorite_coach_username_coach_unique UNIQUE (username, coach_username);
CREATE INDEX IF NOT EXISTS favorite_coach_username_idx ON favorite_coach(username);
CREATE INDEX IF NOT EXISTS favorite_coach_coach_username_idx ON favorite_coach(coach_username);

-- data_history table - add compound index on username and type for faster queries
CREATE INDEX IF NOT EXISTS data_history_username_type_idx ON data_history(username, type);

-- coach_user_note table - add id column as primary key
ALTER TABLE coach_user_note DROP CONSTRAINT IF EXISTS coach_user_note_pkey CASCADE;
ALTER TABLE coach_user_note ADD COLUMN id bigserial;
ALTER TABLE coach_user_note ADD PRIMARY KEY (id);
ALTER TABLE coach_user_note ADD CONSTRAINT coach_user_note_user_coach_unique UNIQUE (user_id, coach_id);
CREATE INDEX IF NOT EXISTS coach_user_note_coach_id_idx ON coach_user_note(coach_id);
CREATE INDEX IF NOT EXISTS coach_user_note_user_id_idx ON coach_user_note(user_id);

-- session_feedback table - add id column as primary key
ALTER TABLE session_feedback DROP CONSTRAINT IF EXISTS session_feedback_pkey CASCADE;
ALTER TABLE session_feedback ADD COLUMN id bigserial;
ALTER TABLE session_feedback ADD PRIMARY KEY (id);
ALTER TABLE session_feedback ADD CONSTRAINT session_feedback_username_session_unique UNIQUE (username, session_id);
CREATE INDEX IF NOT EXISTS session_feedback_username_idx ON session_feedback(username);
CREATE INDEX IF NOT EXISTS session_feedback_session_id_idx ON session_feedback(session_id);

-- users_managers table - add id column as primary key
ALTER TABLE users_managers DROP CONSTRAINT IF EXISTS users_managers_pkey CASCADE;
ALTER TABLE users_managers ADD COLUMN id bigserial;
ALTER TABLE users_managers ADD PRIMARY KEY (id);
ALTER TABLE users_managers ADD CONSTRAINT users_managers_user_manager_unique UNIQUE (user_username, manager_username);

-- user_assessment table - add id column as primary key
ALTER TABLE user_assessment DROP CONSTRAINT IF EXISTS user_assessment_pkey CASCADE;
ALTER TABLE user_assessment ADD COLUMN id bigserial;
ALTER TABLE user_assessment ADD PRIMARY KEY (id);
ALTER TABLE user_assessment ADD CONSTRAINT user_assessment_username_question_unique UNIQUE (username, question_id);
CREATE INDEX IF NOT EXISTS user_assessment_username_idx ON user_assessment(username);

