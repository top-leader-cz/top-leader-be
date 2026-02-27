-- password = "pass" (BCrypt)
insert into users (username, first_name, last_name, password, status, time_zone, locale, authorities)
values ('session_user', 'Session', 'User', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en', '["USER"]');

insert into users (username, first_name, last_name, password, status, time_zone, locale, authorities)
values ('session_coach', 'Session', 'Coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en', '["USER","COACH"]');

insert into users (username, first_name, last_name, password, status, time_zone, locale, authorities)
values ('session_hr', 'Session', 'Hr', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en', '["USER","HR"]');

insert into user_info (username, strengths, values, area_of_development, notes)
values ('session_user', '[]', '[]', '[]', '');
