insert into users (username, password, enabled)
values ('no_coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', true);
insert into users (username, password, enabled, authorities)
values ('coach_no_info', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', true, '["USER", "COACH"]');
insert into users (username, password, enabled, authorities)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', true, '["USER", "COACH"]');

insert into coach (username, bio, email, experience_since, first_name, last_name, photo, public_profile, rate)
values ('coach', 'some bio', 'cool@email.cz', '2023-08-06', 'firstName', 'lastName', null, true, '$$$');
insert into coach_fields (coach_username, fields) values ('coach', 'field1');
insert into coach_fields (coach_username, fields) values ('coach', 'field2');
insert into coach_languages (coach_username, languages) values ('coach', 'cz');
insert into coach_languages (coach_username, languages) values ('coach', 'aj');