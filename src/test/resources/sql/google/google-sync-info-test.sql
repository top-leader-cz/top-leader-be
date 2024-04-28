insert into users (username, password, status, authorities, time_zone)
values ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC');


insert into google_calendar_sync_info (username, refresh_token, sync_token, status, last_sync, enforce_full_sync)
values ('user', 'some-token', '2023-08-14 13:00:00', 'OK', '2023-08-14 13:00:00', '2023-08-14 13:00:00');