insert into users (username, password, time_zone, first_name, last_name)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'firstName', 'lastName');
insert into users (username,  password, time_zone, first_name, last_name)
values ('client1',  '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Cool', 'Client'),
       ('client2',  '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'Bad', 'Client'),
       ('client3',  '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'UTC', 'No', 'Client')
;

insert into coach (username, bio,  experience_since, web_link, public_profile, rate)
values ('coach', 'some bio',  '2023-08-06', 'http://some_video1', true, '$$$');