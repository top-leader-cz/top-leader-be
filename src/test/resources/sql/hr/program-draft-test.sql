-- Re-seed catalog data (truncated by ResetDatabaseAfterTestMethodListener)
INSERT INTO program_option (key, category, always_on) VALUES
    ('opt.hr.session-attendance',      'HR',      TRUE),
    ('opt.hr.goal-completion',         'HR',      TRUE),
    ('opt.hr.micro-action-rate',       'HR',      FALSE),
    ('opt.hr.checkpoint-responses',    'HR',      FALSE),
    ('opt.hr.assessment-results',      'HR',      FALSE),
    ('opt.mgr.enrollment-status',      'MANAGER', TRUE),
    ('opt.mgr.focus-area-goal',        'MANAGER', FALSE),
    ('opt.mgr.session-attendance',     'MANAGER', FALSE),
    ('opt.mgr.goal-progress',          'MANAGER', FALSE);

INSERT INTO company (id, name) VALUES (100, 'Test Corp');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES
    ('hr_prog', 'pass', '["HR","USER"]', 'AUTHORIZED', 'UTC', 'Hr', 'Manager', 100),
    ('user1@test.cz', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Jan', 'Novak', 100),
    ('user2@test.cz', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Petr', 'Svoboda', 100),
    ('user3@test.cz', 'pass', '["USER"]', 'CANCELED', 'UTC', 'Eva', 'Canceled', 100);

-- DRAFT program with participants for launch tests
INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (100, 100, 'CORE', 20, 'ACTIVE', NOW(), NOW() + INTERVAL '90 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, coach_assignment_model, status, duration_days,
                     sessions_per_participant, milestone_date, created_at, created_by, updated_at, updated_by)
VALUES (1, 100, 'Draft Program', 'Improve leadership', 'PARTICIPANT_CHOOSES', 'DRAFT', 90,
        5, NOW() + INTERVAL '80 days', NOW(), 'hr_prog', NOW(), 'hr_prog');

INSERT INTO program_participant (id, program_id, username, created_by, created_at)
VALUES (1, 1, 'user1@test.cz', 'hr_prog', NOW());

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_by, created_at)
VALUES (1, 100, 100, 'user1@test.cz', 0, 0, 'ACTIVE', 'hr_prog', NOW());

-- DRAFT program without goal (for launch validation)
INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (101, 100, 'CORE', 10, 'ACTIVE', NOW(), NOW() + INTERVAL '90 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, status, duration_days, created_at, created_by, updated_at, updated_by)
VALUES (2, 101, 'No Goal Program', 'DRAFT', 90, NOW(), 'hr_prog', NOW(), 'hr_prog');

INSERT INTO program_participant (id, program_id, username, created_by, created_at)
VALUES (2, 2, 'user1@test.cz', 'hr_prog', NOW());

-- DRAFT program without participants (for launch validation)
INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (102, 100, 'CORE', 10, 'ACTIVE', NOW(), NOW() + INTERVAL '90 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, coach_assignment_model, status, duration_days, created_at, created_by, updated_at, updated_by)
VALUES (3, 102, 'No Participants Program', 'Some goal', 'PARTICIPANT_CHOOSES', 'DRAFT', 90, NOW(), 'hr_prog', NOW(), 'hr_prog');

-- Advance sequences past inserted IDs
ALTER SEQUENCE program_id_seq RESTART WITH 100;
ALTER SEQUENCE program_participant_id_seq RESTART WITH 100;
ALTER SEQUENCE coaching_package_id_seq RESTART WITH 200;
ALTER SEQUENCE user_allocation_id_seq RESTART WITH 100;
