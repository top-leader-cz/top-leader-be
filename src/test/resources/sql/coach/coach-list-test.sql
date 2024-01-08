INSERT INTO users (username, password, status, authorities, time_zone, first_name, last_name)
VALUES ('coach1', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'John', 'Doe'),
       ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Jane', 'Smith'),
       ('coach3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Michael', 'Johnson'),
       ('coach4', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Michael1', 'Johnson1');

INSERT INTO users (username, password, status, authorities, time_zone, coach, credit, scheduled_credit, free_coach)
VALUES ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach1', 400, 0, null),
       ('no-credit-user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach2', 400, 400, 'coach3'),
       ('no-credit-user-free-coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach3', 400, 400, 'coach3');

INSERT INTO coach (username, public_profile, email, web_link, bio, experience_since, rate)
VALUES ('coach1', true, 'john.doe@example.com', 'http://some_video1', 'Experienced coach', '2021-01-01', '$'),
       ('coach2', true, 'jane.smith@example.com', 'http://some_video2', 'Passionate about coaching', '2017-05-15', '$$'),
       ('coach3', true, 'michael.johnson@example.com', 'http://some_video3', 'Certified fitness coach', '2019-09-10', '$$$'),
       ('coach4', false, 'michael.johnson@example.com', 'http://some_video4', 'Certified fitness coach', '2019-09-10', '$$$');

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

insert into coach_rate (rate_name, rate_credit)
values ('$', 110),
       ('$$', 165),
       ('$$$', 275)
;