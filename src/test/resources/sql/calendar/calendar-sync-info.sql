INSERT INTO users (username, first_name, last_name, password, time_zone, status)
VALUES ('test@example.com', 'Test', 'User', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'Europe/Prague', 'AUTHORIZED');

INSERT INTO calendar_sync_info (username, sync_type, refresh_token, access_token, status, last_sync)
VALUES ('test@example.com', 'GOOGLE', 'old-refresh-token', 'old-access-token', 'OK', NOW());
