insert into users (username, password, status, time_zone)
values ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC');
insert into users (username, password, status, time_zone)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC');
insert into users (username, password, status, time_zone, coach, credit, scheduled_credit)
values ('user_with_coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'coach', 400, 400);
insert into users (username, password, status, first_name, last_name, time_zone)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'Mitch', 'Cleverman', 'UTC');

INSERT INTO coach (username, public_profile, email, web_link, bio, experience_since, rate)
VALUES ('coach', true, 'john.doe@example.com', 'http://some_video1', 'Experienced coach', '2021-01-01', '$');

insert into coach_rate (rate_name, rate_credit)
values ('$', 110),
       ('$$', 165),
       ('$$$', 275)
;

insert into user_info(username, strengths, values, area_of_development, notes)
values ('user2', '["s1","s2"]', '["v1","v2"]', '["a1","a2"]', 'cool note');


insert into scheduled_session (id, coach_username, username, time)
values (nextval('scheduled_session_id_seq'), 'coach', 'user_with_coach', '2023-08-14 10:30:00');
