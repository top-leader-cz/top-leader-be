INSERT INTO users (username, password, status, authorities, time_zone)
VALUES ('coach1', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'CET'),
       ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'CET'),
       ('coach3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'CET'),
       ('coach4', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'CET');


INSERT INTO coach (username, public_profile, first_name, last_name, email, web_link, bio, experience_since, rate, time_zone)
VALUES ('coach1', true, 'John', 'Doe', 'john.doe@example.com', 'http://some_video1', 'Experienced coach', '2021-01-01', '$', 'Europe/Prague'),
       ('coach2', true, 'Jane', 'Smith', 'jane.smith@example.com', 'http://some_video2', 'Passionate about coaching', '2017-05-15', '$$', 'Europe/Prague'),
       ('coach3', true, 'Michael', 'Johnson', 'michael.johnson@example.com', 'http://some_video3', 'Certified fitness coach', '2019-09-10', '$$$', 'Europe/Prague'),
       ('coach4', false, 'Michael1', 'Johnson1', 'michael.johnson@example.com', 'http://some_video4', 'Certified fitness coach', '2019-09-10', '$$$', 'Europe/Prague');

INSERT INTO coach_languages (coach_username, languages)
VALUES ('coach1', 'English'),
       ('coach1', 'French'),
       ('coach2', 'English'),
       ('coach2', 'Spanish'),
       ('coach3', 'German');

INSERT INTO coach_fields (coach_username, fields)
VALUES ('coach1', 'Fitness'),
       ('coach2', 'Yoga'),
       ('coach2', 'Meditation'),
       ('coach3', 'Weightlifting');

insert into coach_availability (id, username, recurring, day_from, time_from, day_to, time_to)
values (nextval('coach_availability_seq'), 'coach1', true, 'MONDAY', '13:00:00', 'MONDAY', '14:00:00');
insert into coach_availability (id, username, recurring, day_from, time_from, day_to, time_to)
values (nextval('coach_availability_seq'), 'coach1', true, 'TUESDAY', '13:00:00', 'TUESDAY', '14:00:00');

insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-08-14 10:00:00', '2023-08-14 12:00:00');
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-08-15 12:00:00', '2023-08-15 14:00:00');
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-09-14 13:00:00', '2023-09-14 14:00:00');
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-09-15 12:00:00', '2023-09-15 13:00:00');