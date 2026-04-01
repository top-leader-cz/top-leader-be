INSERT INTO users (username, email, password, status, authorities, time_zone, first_name, last_name, locale)
VALUES ('meet-coach', 'meet-coach@example.com', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Meet', 'Coach', 'en'),
       ('meet-user', 'meet-user@example.com', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'UTC', 'Meet', 'User', 'en');

INSERT INTO users (username, email, password, status, authorities, time_zone, coach, first_name, last_name, locale)
VALUES ('meet-client', 'meet-client@example.com', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'UTC', 'meet-coach', 'Client', 'Test', 'en');

INSERT INTO coach (username, public_profile, web_link, bio, experience_since, rate, rate_order, free_slots, priority, languages, fields)
VALUES ('meet-coach', true, 'http://video', 'Coach bio', '2020-01-01', '$', 1, false, 0, '["English"]', '["Fitness"]');

INSERT INTO coach_rate (rate_name, rate_credit, rate_order)
VALUES ('$', 110, 1)
ON CONFLICT DO NOTHING;

INSERT INTO company (id, name) VALUES (100, 'Meet Test Company');

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, created_at, created_by, updated_at)
VALUES (100, 100, 'CORE', 100, 'ACTIVE', now(), 'test', now());

INSERT INTO user_allocation (id, package_id, company_id, username, allocated_units, consumed_units, status, created_at, created_by, updated_at)
VALUES (100, 100, 100, 'meet-client', 10, 0, 'ACTIVE', now(), 'test', now());

INSERT INTO meeting_info (username, provider, refresh_token, access_token, email, auto_generate, status, created_at)
VALUES ('meet-coach', 'GOOGLE', 'test-refresh-token', 'test-access-token', 'meet-coach@gmail.com', true, 'OK', now());
