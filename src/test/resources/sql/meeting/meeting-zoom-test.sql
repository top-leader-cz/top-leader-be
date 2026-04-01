INSERT INTO users (username, email, password, status, authorities, time_zone, first_name, last_name, locale)
VALUES ('zoom-coach', 'zoom-coach@example.com', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Zoom', 'Coach', 'en');

INSERT INTO users (username, email, password, status, authorities, time_zone, coach, first_name, last_name, locale)
VALUES ('zoom-client', 'zoom-client@example.com', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'UTC', 'zoom-coach', 'Zoom', 'Client', 'en');

INSERT INTO coach (username, public_profile, web_link, bio, experience_since, rate, rate_order, free_slots, priority, languages, fields)
VALUES ('zoom-coach', true, 'http://video', 'Zoom coach bio', '2020-01-01', '$', 1, false, 0, '["English"]', '["Fitness"]');

INSERT INTO coach_rate (rate_name, rate_credit, rate_order)
VALUES ('$', 110, 1)
ON CONFLICT DO NOTHING;

INSERT INTO company (id, name) VALUES (200, 'Zoom Test Company');

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, created_at, created_by, updated_at)
VALUES (200, 200, 'CORE', 100, 'ACTIVE', now(), 'test', now());

INSERT INTO user_allocation (id, package_id, company_id, username, allocated_units, consumed_units, status, created_at, created_by, updated_at)
VALUES (200, 200, 200, 'zoom-client', 10, 0, 'ACTIVE', now(), 'test', now());

INSERT INTO meeting_info (username, provider, refresh_token, access_token, email, auto_generate, status, created_at)
VALUES ('zoom-coach', 'ZOOM', 'zoom-refresh-token', 'zoom-access-token', 'zoom-coach@zoom.us', true, 'OK', now());
