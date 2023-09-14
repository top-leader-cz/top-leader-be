INSERT INTO users (username, password, status, authorities)
VALUES ('coach1', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]'),
       ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]'),
       ('coach3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]');


INSERT INTO coach (username, public_profile, first_name, last_name, email, web_link, bio, experience_since, rate)
VALUES ('coach1', true, 'John', 'Doe', 'john.doe@example.com', 'http://some_video1', 'Experienced coach', '2021-01-01', '$'),
       ('coach2', true, 'Jane', 'Smith', 'jane.smith@example.com', 'http://some_video2', 'Passionate about coaching', '2017-05-15', '$$'),
       ('coach3', true, 'Michael', 'Johnson', 'michael.johnson@example.com', 'http://some_video3', 'Certified fitness coach', '2019-09-10', '$$$');

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

insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach1', null, null, 'MONDAY', true, '13:00:00', '14:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach1', null, null, 'TUESDAY', true, '12:00:00', '13:00:00');

insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach1', '2023-08-14', '2023-08-14', 'MONDAY', false, '13:00:00', '14:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach1', '2023-08-15', '2023-08-14', 'TUESDAY', false, '12:00:00', '13:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach1', '2023-09-14', '2023-09-14', 'MONDAY', false, '13:00:00', '14:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach1', '2023-09-15', '2023-09-14', 'TUESDAY', false, '12:00:00', '13:00:00');
