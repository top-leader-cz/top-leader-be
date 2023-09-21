insert into users (username, password, status)
values ('no_coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED');
insert into users (username, password, status, authorities)
values ('coach_no_info', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]');
insert into users (username, password, status, authorities)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]');

insert into coach (username, bio, email, experience_since, first_name, last_name, web_link, public_profile, rate)
values ('coach', 'some bio', 'cool@email.cz', '2023-08-06', 'firstName', 'lastName', 'http://some_video1', true, '$$$');
insert into coach_fields (coach_username, fields) values ('coach', 'field1');
insert into coach_fields (coach_username, fields) values ('coach', 'field2');
insert into coach_languages (coach_username, languages) values ('coach', 'cz');
insert into coach_languages (coach_username, languages) values ('coach', 'aj');