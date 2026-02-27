INSERT INTO company (id, name) VALUES (100, 'Test Corp');

INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id)
VALUES ('hr_prog', 'pass', '["HR","USER"]', 'AUTHORIZED', 'UTC', 'Hr', 'Manager', 100);

-- 90-day program: started 57 days ago, ends in 33 days
INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, valid_from, valid_to, created_by, created_at)
VALUES (100, 100, 'CORE', 40, 'ACTIVE',
        NOW() - INTERVAL '57 days',
        NOW() + INTERVAL '33 days',
        'hr_prog', NOW() - INTERVAL '57 days');

INSERT INTO program (id, coaching_package_id, name, milestone_date, created_at, created_by)
VALUES (1, 100, 'Leadership Development 90d',
        NOW() + INTERVAL '33 days',
        NOW() - INTERVAL '57 days', 'hr_prog');

-- Participants: 57/90 days elapsed -> expected consumed = 4 * 57/90 = 2.53, threshold (80%) = 2.03
INSERT INTO users (username, password, authorities, status, time_zone, first_name, last_name, company_id, coach, last_login_at)
VALUES
    ('tomas.novak',   'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Tomas',   'Novak',   100, 'coach1', NOW()),
    ('jana.dvorak',   'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Jana',    'Dvorak',  100, 'coach1', NOW() - INTERVAL '1 day'),
    ('marketa.cerva', 'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Marketa', 'Cerva',   100, null,     NOW() - INTERVAL '9 days'),
    ('oleg.volkov',   'pass', '["USER"]', 'AUTHORIZED', 'UTC', 'Oleg',    'Volkov',  100, null,     NULL);

INSERT INTO user_allocation (id, company_id, package_id, username, allocated_units, consumed_units, status, created_by, created_at)
VALUES
    (1, 100, 100, 'tomas.novak',   4, 3, 'ACTIVE', 'hr_prog', NOW() - INTERVAL '57 days'),
    (2, 100, 100, 'jana.dvorak',   4, 3, 'ACTIVE', 'hr_prog', NOW() - INTERVAL '57 days'),
    (3, 100, 100, 'marketa.cerva', 4, 2, 'ACTIVE', 'hr_prog', NOW() - INTERVAL '57 days'),
    (4, 100, 100, 'oleg.volkov',   4, 0, 'ACTIVE', 'hr_prog', NOW() - INTERVAL '57 days');

-- marketa.cerva: manually put ON_HOLD (overrides computation)
INSERT INTO program_participant (id, program_id, username, status, created_by, created_at)
VALUES (1, 1, 'marketa.cerva', 'ON_HOLD', 'hr_prog', NOW() - INTERVAL '10 days');
