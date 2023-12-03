insert into users (username, password, time_zone, first_name, last_name)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'firstName', 'lastName');
insert into users (username, coach, password, time_zone, first_name, last_name)
values ('client1', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Cool', 'Client'),
       ('client2', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Bad', 'Client'),
       ('client3', 'coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'No', 'Client')
;

insert into coach (username, bio, email, experience_since, web_link, public_profile, rate)
values ('coach', 'some bio', 'cool@email.cz', '2023-08-06', 'http://some_video1', true, '$$$');

insert into scheduled_session (id, coach_username, username, time)
values (1, 'coach', 'client1', '2023-08-14 10:30:00'),
       (2, 'coach', 'client1', '2023-08-14 11:30:00'),
       (3, 'coach', 'client2', '2023-08-15 10:30:00'),
       (4, 'coach', 'client2', '2023-08-15 11:30:00'),
       (5, 'coach', 'client3', '2023-08-16 10:30:00'),
       (6, 'coach', 'client3', '2023-08-16 11:30:00')
;

select setval('scheduled_session_id_seq', 20, true);

