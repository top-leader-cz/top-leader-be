-- =============================================================================
-- QA Seed: add program_participant for all existing program/user_allocation combos
-- =============================================================================
-- Run after seed-programs-qa.sql was already executed.
-- =============================================================================

INSERT INTO program_participant (program_id, username, coach_username, created_by, created_at)
SELECT
    p.id          AS program_id,
    ua.username,
    u.coach       AS coach_username,
    'admin'       AS created_by,
    NOW()         AS created_at
FROM program p
JOIN coaching_package cp ON cp.id = p.coaching_package_id
JOIN user_allocation ua ON ua.package_id = cp.id
JOIN users u ON u.username = ua.username
ON CONFLICT (program_id, username) DO NOTHING;
