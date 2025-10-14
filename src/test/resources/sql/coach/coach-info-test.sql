insert into users (username, password, status)
values ('no_coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED');
insert into users (username, password, status, authorities, time_zone)
values ('coach_no_info', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'Europe/Prague');
insert into users (username, email, password, status, authorities, first_name, last_name, time_zone, locale)
values ('coach', 'cool123@email.cz','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'firstName', 'lastName', 'Europe/Prague', 'en');
insert into users (username, password, status, authorities, first_name, last_name, locale)
values ('user1', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'user1FirstName', 'user1lastName', 'en');
insert into users (username, password, status, authorities, first_name, last_name)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'user2FirstName', 'user2lastName');
insert into users (username, password, status, authorities, first_name, last_name)
values ('user3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'user3FirstName', 'user3lastName');
insert into users (username, password, status, authorities, first_name, last_name)
values ('coach3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'coach3FirstName', 'coach3lastName');

insert into coach (username, bio,  experience_since, web_link, public_profile, rate, certificate, primary_roles)
values ('coach', 'some bio', '2023-08-06', 'http://some_video1', true, '$', 'ACC', '["COACH"]');

insert into coach (username, bio, experience_since, web_link, rate, certificate)
values ('coach3', 'some bio',  '2023-08-06', 'http://some_video1',  '$', 'ACC');

insert into coach_fields (coach_username, fields) values ('coach', 'field1');
insert into coach_fields (coach_username, fields) values ('coach', 'field2');
insert into coach_languages (coach_username, languages) values ('coach', 'cz');
insert into coach_languages (coach_username, languages) values ('coach', 'aj');

insert into scheduled_session (id, coach_username, username, time)
values (nextval('scheduled_session_id_seq'), 'coach', 'user1', '2023-08-14 10:30:00'),
       (nextval('scheduled_session_id_seq'), 'coach', 'user1', '2023-08-14 11:30:00'),
       (nextval('scheduled_session_id_seq'), 'coach', 'user2', '2023-08-15 10:30:00')
;

insert into coach_rate (rate_name, rate_credit, rate_order)
values ('$', 110, 1),
       ('$$', 165, 2),
       ('$$$', 275, 3);


