insert into users (username,first_name, last_name, password, time_zone, status, aspired_competency, authorities)
values ('jakub.svezi@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING', 'test competency', '["USER"]');
insert into users (username, first_name, last_name, password, time_zone, status, aspired_competency, authorities)
values ('petr.mdly@dummy.com', 'Petr', 'Mdly','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING', 'test competency',  '["USER"]');

insert into badge(username, achievement_type, month, year)
values ('petr.mdly@dummy.com', 'WATCHED_VIDEO', 'MARCH', 2025);
insert into badge(username, achievement_type, month, year)
values ('jakub.svezi@dummy.com', 'WATCHED_VIDEO', 'JANUARY', 2024);
insert into badge(username, achievement_type, month, year)
values ('jakub.svezi@dummy.com', 'WATCHED_VIDEO', 'JANUARY', 2025)

