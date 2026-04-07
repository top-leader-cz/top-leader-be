-- Manager program view test fixture
INSERT INTO focus_area (key) VALUES ('fa.giving-feedback') ON CONFLICT DO NOTHING;

INSERT INTO company (id, name) VALUES (100, 'Test Corp'), (200, 'Other Corp');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES
    ('hr_prog',  'pass', '["HR","USER"]',     'AUTHORIZED', 'UTC', 'Hr',     'Manager', 100),
    ('mgr1',     'pass', '["MANAGER","USER"]','AUTHORIZED', 'UTC', 'Manager','One',     100),
    ('mgr2',     'pass', '["MANAGER","USER"]','AUTHORIZED', 'UTC', 'Manager','Two',     100),
    ('mgr_other','pass', '["MANAGER","USER"]','AUTHORIZED', 'UTC', 'Other',  'Mgr',     200),
    ('user1',    'pass', '["USER"]',          'AUTHORIZED', 'UTC', 'Anna',   'Adams',   100),
    ('user2',    'pass', '["USER"]',          'AUTHORIZED', 'UTC', 'Bob',    'Brown',   100),
    ('user3',    'pass', '["USER"]',          'AUTHORIZED', 'UTC', 'Cara',   'Clark',   100),
    ('user4',    'pass', '["USER"]',          'AUTHORIZED', 'UTC', 'Dan',    'Davis',   100);

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES
    (100, 100, 'CORE', 20, 'ACTIVE', NOW(), NOW() + INTERVAL '90 days', 'hr_prog', NOW()),
    (101, 100, 'CORE', 20, 'ACTIVE', NOW(), NOW() + INTERVAL '90 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, coach_assignment_model, status, duration_days,
                     sessions_per_participant, created_at, created_by, updated_at, updated_by)
VALUES
    (1, 100, 'Alpha Program', 'Lead better',     'PARTICIPANT_CHOOSES', 'ACTIVE', 90, 5, NOW(), 'hr_prog', NOW(), 'hr_prog'),
    (2, 101, 'Bravo Program', 'Improve skills',  'PARTICIPANT_CHOOSES', 'ACTIVE', 90, 5, NOW(), 'hr_prog', NOW(), 'hr_prog');

-- mgr1 manages user1 (program 1) and user3 (program 2)
-- mgr2 manages user2 (program 1)
-- user4 is in program 1 with NO manager (must not appear in mgr1's list)
INSERT INTO program_participant (id, program_id, username, status, current_cycle, manager_username, focus_area, personal_goal, enrolled_at, created_by, created_at)
VALUES
    (1, 1, 'user1', 'ACTIVE', 1, 'mgr1', 'fa.giving-feedback', 'goal1', NOW() - INTERVAL '10 days', 'hr_prog', NOW()),
    (2, 1, 'user2', 'AT_RISK', 1, 'mgr2', 'fa.giving-feedback', 'goal2', NOW() - INTERVAL '10 days', 'hr_prog', NOW()),
    (3, 2, 'user3', 'INVITED', 1, 'mgr1', NULL, NULL, NULL, 'hr_prog', NOW()),
    (4, 1, 'user4', 'ACTIVE', 1, NULL,    'fa.giving-feedback', 'goal4', NOW() - INTERVAL '10 days', 'hr_prog', NOW());

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_by, created_at)
VALUES
    (1, 100, 100, 'user1', 5, 3, 'ACTIVE', 'hr_prog', NOW()),
    (2, 100, 100, 'user2', 5, 1, 'ACTIVE', 'hr_prog', NOW()),
    (3, 100, 101, 'user3', 5, 0, 'ACTIVE', 'hr_prog', NOW()),
    (4, 100, 100, 'user4', 5, 5, 'ACTIVE', 'hr_prog', NOW());

-- user1: 4 practices total, 3 responded => 0.75
INSERT INTO weekly_practice (id, participant_id, cycle, week_number, text, source, friday_response, created_at)
VALUES
    (1, 1, 1, 1, 'p1', 'AI', 'YES',     NOW() - INTERVAL '21 days'),
    (2, 1, 1, 2, 'p2', 'AI', 'PARTIAL', NOW() - INTERVAL '14 days'),
    (3, 1, 1, 3, 'p3', 'AI', 'YES',     NOW() - INTERVAL '7 days'),
    (4, 1, 1, 4, 'p4', 'AI', NULL,      NOW() - INTERVAL '1 days');

-- user2: 2 practices, 0 responded => 0.0
INSERT INTO weekly_practice (id, participant_id, cycle, week_number, text, source, friday_response, created_at)
VALUES
    (5, 2, 1, 1, 'p1', 'AI', NULL, NOW() - INTERVAL '14 days'),
    (6, 2, 1, 2, 'p2', 'AI', NULL, NOW() - INTERVAL '7 days');

-- user3: no practices => 0.0

ALTER SEQUENCE program_id_seq RESTART WITH 100;
ALTER SEQUENCE program_participant_id_seq RESTART WITH 100;
ALTER SEQUENCE coaching_package_id_seq RESTART WITH 200;
ALTER SEQUENCE user_allocation_id_seq RESTART WITH 100;
ALTER SEQUENCE weekly_practice_id_seq RESTART WITH 100;
