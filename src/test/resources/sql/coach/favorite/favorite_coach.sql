insert into users (username, first_name, last_name, password, time_zone, status, authorities)
values ('jakub.svezi@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'AUTHORIZED', '["USER"]');

insert into favorite_coach (username, coach_username) values ('jakub.svezi@dummy.com', 'coach');
insert into favorite_coach (username, coach_username) values ('jakub.svezi2@dummy.com', 'coach2');