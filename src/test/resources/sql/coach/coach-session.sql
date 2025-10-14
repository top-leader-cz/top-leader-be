
insert into users (username, coach, password, time_zone, first_name, last_name)
values ('client1', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Cool', 'Client'),
       ('client2', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Bad', 'Client'),
       ('client3', 'coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'No', 'Client')
;

insert into users (username, password, time_zone, first_name, last_name, authorities)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'firstName', 'lastName', '["USER", "COACH"]');
insert into coach (username, bio,  experience_since, web_link, public_profile, rate)
values ('coach', 'some bio', '2023-08-06', 'http://some_video1', true, '$$$');

insert into users (username, password, time_zone, first_name, last_name, authorities)
values ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'firstName', 'coach2', '["USER", "COACH"]');
insert into coach (username, bio,  experience_since, web_link, public_profile, rate)
values ('coach2', 'some bio',  '2023-08-06', 'http://some_video1', true, '$$$');


insert into scheduled_session (id, coach_username, username, time, status)
values (1, 'coach', 'client1', '2023-08-14 10:30:00', 'COMPLETED'),
       (2, 'coach', 'client1', '2023-08-14 11:30:00', 'UPCOMING'),
       (3, 'coach', 'client1', '2023-08-15 10:30:00', 'CANCELED'),
       (4, 'coach', 'client1', '2023-08-15 11:30:00', 'UPCOMING'),
       (5, 'coach', 'client2', '2023-08-16 10:30:00', 'UPCOMING'),
       (6, 'coach', 'client3', '2023-08-16 11:30:00', 'UPCOMING'),
       (7, 'coach', 'client1', now() + INTERVAL '1 hour' , 'UPCOMING')
;