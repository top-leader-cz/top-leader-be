insert into users (username, password, status, credit, first_name, last_name, time_zone)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 0, 'John', 'Doe', 'Europe/Prague');
insert into users (username, password, status, credit)
values ('client1', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 300),
       ('client2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 300),
       ('client3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 300)
;

insert into coach_rate (rate_name, rate_credit)
values ('$', 110),
       ('$$', 165),
       ('$$$', 275)
;

INSERT INTO coach (username, public_profile, email, web_link, bio, experience_since, rate)
VALUES ('coach', true, 'john.doe@example.com', 'http://some_video1', 'Experienced coach', '2021-01-01', '$');


insert into scheduled_session (id, coach_username, username, time, paid)
values (1, 'coach', 'client1', '2023-08-14 10:30:00', false),
       (2, 'coach', 'client1', '2023-08-14 11:30:00', false),
       (3, 'coach', 'client2', '2023-08-15 10:30:00', false),
       (4, 'coach', 'client2', '2023-08-15 11:30:00', false),
       (5, 'coach', 'client3', '2023-08-16 10:30:00', false),
       (6, 'coach', 'client3', '2023-08-16 11:30:00', false)