insert into users (username, password, status, authorities, time_zone)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC');

insert into users (username, password, status, authorities, time_zone)
values ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC');

insert into coach (username, bio, email, experience_since, web_link, public_profile, rate)
values ('coach', 'some bio', 'cool@email.cz', '2023-08-06', 'http://some_video1', true, '$$$');
insert into coach (username, bio, email, experience_since, web_link, public_profile, rate)
values ('coach2', 'some bio', 'cool@email.cz', '2023-08-06', 'http://some_video1', true, '$$$');



insert into coach_availability (id, username, recurring, day_from, time_from, day_to, time_to)
values (nextval('coach_availability_seq'), 'coach', true, 'MONDAY', '13:00:00', 'MONDAY', '14:00:00');
insert into coach_availability (id, username, recurring, day_from, time_from, day_to, time_to)
values (nextval('coach_availability_seq'), 'coach', true, 'TUESDAY', '13:00:00', 'TUESDAY', '14:00:00');

insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach', false, to_timestamp('2023-08-14 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), to_timestamp('2023-08-14 14:00:00', 'YYYY-MM-DD HH24:MI:SS'));
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach', false, to_timestamp('2023-08-15 12:00:00', 'YYYY-MM-DD HH24:MI:SS'), to_timestamp('2023-08-15 14:00:00', 'YYYY-MM-DD HH24:MI:SS'));
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach', false, to_timestamp('2023-09-14 13:00:00', 'YYYY-MM-DD HH24:MI:SS'), to_timestamp('2023-09-14 14:00:00', 'YYYY-MM-DD HH24:MI:SS'));
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach', false, to_timestamp('2023-09-15 12:00:00', 'YYYY-MM-DD HH24:MI:SS'), to_timestamp('2023-09-15 13:00:00', 'YYYY-MM-DD HH24:MI:SS'));
