-- New user enrollment email test (user created during draft save, then launched)
INSERT INTO company (id, name) VALUES (100, 'Test Corp');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id, email, locale)
VALUES
    ('hr_prog', 'pass', '["HR","USER"]', 'AUTHORIZED', 'UTC', 'Hr', 'Manager', 100, 'hr_prog@test.cz', 'en'),
    ('newuser@test.cz', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', '', '', 100, 'newuser@test.cz', 'en');

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (100, 100, 'CORE', 20, 'ACTIVE', NOW() - INTERVAL '1 hour', NOW() + INTERVAL '90 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, coach_assignment_model, status, duration_days,
                     sessions_per_participant, created_at, created_by, updated_at, updated_by)
VALUES (1, 100, 'New User Program', 'Develop skills', 'PARTICIPANT_CHOOSES', 'DRAFT', 90,
        5, NOW(), 'hr_prog', NOW(), 'hr_prog');

INSERT INTO program_participant (id, program_id, username, new_user, created_by, created_at)
VALUES (1, 1, 'newuser@test.cz', TRUE, 'hr_prog', NOW());

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_by, created_at)
VALUES (1, 100, 100, 'newuser@test.cz', 0, 0, 'ACTIVE', 'hr_prog', NOW());

ALTER SEQUENCE program_id_seq RESTART WITH 100;
ALTER SEQUENCE program_participant_id_seq RESTART WITH 100;
ALTER SEQUENCE coaching_package_id_seq RESTART WITH 200;
ALTER SEQUENCE user_allocation_id_seq RESTART WITH 100;
