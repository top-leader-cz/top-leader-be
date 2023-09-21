insert into users (username, password, status, time_zone)
values ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC');
insert into users (username, password, status, time_zone)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC');

insert into user_info(username, strengths, values, area_of_development, notes)
values ('user2', '["s1","s2"]', '["v1","v2"]', '["a1","a2"]', 'cool note');