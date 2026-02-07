-- V1.0.0.4: Rename confusing column names for consistency
-- Problem: Some columns are named "user_id" or "coach_id" but contain VARCHAR username, not BIGINT id
-- Solution: Rename them to "username" or "coach_username" for clarity

-- ==============================================================================
-- RENAME user_id TO username (where it contains username string, not ID)
-- ==============================================================================

-- user_allocation table
-- Column "user_id" currently contains username (VARCHAR), not ID - CONFUSING!
ALTER TABLE user_allocation RENAME COLUMN user_id TO username;

-- Update FK constraint name for clarity
ALTER TABLE user_allocation DROP CONSTRAINT IF EXISTS user_allocation_user_id_fkey;
ALTER TABLE user_allocation ADD CONSTRAINT fk_user_allocation_username
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;

-- Recreate index with new column name
DROP INDEX IF EXISTS user_allocation_user_id_idx;
CREATE INDEX user_allocation_username_idx ON user_allocation(username);

-- Update unique constraint with new column name
ALTER TABLE user_allocation DROP CONSTRAINT IF EXISTS user_allocation_package_id_user_id_key;
ALTER TABLE user_allocation ADD CONSTRAINT user_allocation_package_id_username_unique
    UNIQUE (package_id, username);

-- ==============================================================================
-- RENAME coach_id and user_id in coach_user_note table
-- ==============================================================================

-- coach_user_note table has TWO confusing columns:
-- 1. "coach_id" contains coach username (VARCHAR), not ID
-- 2. "user_id" contains user username (VARCHAR), not ID

-- Rename coach_id to coach_username
ALTER TABLE coach_user_note RENAME COLUMN coach_id TO coach_username;

-- Rename user_id to username
ALTER TABLE coach_user_note RENAME COLUMN user_id TO username;

-- Update FK constraints with new names
ALTER TABLE coach_user_note DROP CONSTRAINT IF EXISTS coach_user_note_coach_id_fkey;
ALTER TABLE coach_user_note ADD CONSTRAINT fk_coach_user_note_coach_username
    FOREIGN KEY (coach_username) REFERENCES coach(username) ON DELETE CASCADE;

ALTER TABLE coach_user_note DROP CONSTRAINT IF EXISTS coach_user_note_user_id_fkey;
ALTER TABLE coach_user_note ADD CONSTRAINT fk_coach_user_note_username
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE;

-- Recreate indexes with new column names
DROP INDEX IF EXISTS coach_user_note_coach_id_idx;
CREATE INDEX coach_user_note_coach_username_idx ON coach_user_note(coach_username);

DROP INDEX IF EXISTS coach_user_note_user_id_idx;
CREATE INDEX coach_user_note_username_idx ON coach_user_note(username);

-- Update unique constraint with new column names
ALTER TABLE coach_user_note DROP CONSTRAINT IF EXISTS coach_user_note_user_id_coach_id_unique;
ALTER TABLE coach_user_note ADD CONSTRAINT coach_user_note_username_coach_username_unique
    UNIQUE (username, coach_username);


