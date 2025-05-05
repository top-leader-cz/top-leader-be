insert into company(id, name)
values (1, 'company1'),
       (2, 'company2');


insert into users (username, coach, password, time_zone, first_name, last_name, authorities,company_id)
values ('client1', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Cool', 'Client1',  '["USER"]', 1),
       ('client2', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Bad', 'Client2',   '["USER"]', 2),
       ('client3', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'No', 'Client3',   '["USER"]', 1),
       ('client4', 'coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'No', 'Client4',   '["USER"]', 1),
       ('hr1', null, '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'first', 'hr',   '["HR"]', 1)
;

insert into users (username, password, time_zone, first_name, last_name, authorities)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'firstName', 'lastName', '["USER", "COACH"]');
insert into coach (username, bio, email, experience_since, web_link, public_profile, rate)
values ('coach', 'some bio', 'cool@email.cz', '2023-08-06', 'http://some_video1', true, '$$$');

insert into scheduled_session (id, coach_username, username, time, status)
values (1, 'coach', 'client1', '2023-08-14 08:30:00', 'COMPLETED'),
       (2, 'coach', 'client1', '2023-08-14 12:30:00', 'COMPLETED'),
       (3, 'coach', 'client1', '2023-08-14 14:00:00', 'CANCELED'),
       (4, 'coach', 'client2', '2023-08-14 11:30:00', 'UPCOMING'),
       (5, 'coach', 'client2', '2023-08-14 12:30:00', 'UPCOMING'),
       (6, 'coach', 'client3', '2023-08-14 11:30:00', 'UPCOMING')
;