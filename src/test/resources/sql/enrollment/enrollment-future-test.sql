-- Future start date enrollment email test (email should be scheduled, not sent)
INSERT INTO company (id, name) VALUES (100, 'Test Corp');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id, email, locale)
VALUES
    ('hr_prog', 'pass', '["HR","USER"]', 'AUTHORIZED', 'UTC', 'Hr', 'Manager', 100, 'hr_prog@test.cz', 'en'),
    ('user1@test.cz', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Jan', 'Novak', 100, 'user1@test.cz', 'en');

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (100, 100, 'CORE', 20, 'ACTIVE', NOW() + INTERVAL '14 days', NOW() + INTERVAL '104 days', 'hr_prog', NOW());

INSERT INTO program (id, coaching_package_id, name, goal, coach_assignment_model, status, duration_days,
                     sessions_per_participant, created_at, created_by, updated_at, updated_by)
VALUES (1, 100, 'Future Program', 'Future goals', 'PARTICIPANT_CHOOSES', 'DRAFT', 90,
        5, NOW(), 'hr_prog', NOW(), 'hr_prog');

INSERT INTO program_participant (id, program_id, username, created_by, created_at)
VALUES (1, 1, 'user1@test.cz', 'hr_prog', NOW());

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_by, created_at)
VALUES (1, 100, 100, 'user1@test.cz', 0, 0, 'ACTIVE', 'hr_prog', NOW());

ALTER SEQUENCE program_id_seq RESTART WITH 100;
ALTER SEQUENCE program_participant_id_seq RESTART WITH 100;
ALTER SEQUENCE coaching_package_id_seq RESTART WITH 200;
ALTER SEQUENCE user_allocation_id_seq RESTART WITH 100;
