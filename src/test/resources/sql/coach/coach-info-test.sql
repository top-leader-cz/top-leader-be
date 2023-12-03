insert into users (username, password, status)
values ('no_coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED');
insert into users (username, password, status, authorities, time_zone)
values ('coach_no_info', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'Europe/Prague');
insert into users (username, password, status, authorities, first_name, last_name, time_zone)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'firstName', 'lastName', 'Europe/Prague');
insert into users (username, password, status, authorities, first_name, last_name)
values ('user1', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'user1FirstName', 'user1lastName');
insert into users (username, password, status, authorities, first_name, last_name)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'user2FirstName', 'user2lastName');
insert into users (username, password, status, authorities, first_name, last_name)
values ('user3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'user3FirstName', 'user3lastName');

insert into coach (username, bio, email, experience_since, web_link, public_profile, rate)
values ('coach', 'some bio', 'cool@email.cz', '2023-08-06', 'http://some_video1', true, '$$$');
insert into coach_fields (coach_username, fields) values ('coach', 'field1');
insert into coach_fields (coach_username, fields) values ('coach', 'field2');
insert into coach_languages (coach_username, languages) values ('coach', 'cz');
insert into coach_languages (coach_username, languages) values ('coach', 'aj');

insert into scheduled_session (id, coach_username, username, time)
values (1, 'coach', 'user1', '2023-08-14 10:30:00'),
       (2, 'coach', 'user1', '2023-08-14 11:30:00'),
       (3, 'coach', 'user2', '2023-08-15 10:30:00')
;

insert into coach_rate (rate_name, rate_credit)
values ('$', 110),
       ('$$', 165),
       ('$$$', 275)