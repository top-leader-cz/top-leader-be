insert into users (username, first_name, last_name, password, time_zone, status, aspired_competency, authorities)
values ('jakub.user@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING', 'test competency', '["USER"]');
insert into users (username, first_name, last_name, password, time_zone, status, aspired_competency, authorities)
values ('jakub.user2@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING', 'test competency', '["USER"]');
insert into users (username, first_name, last_name, password, time_zone, status, aspired_competency, authorities)
values ('petr.cocah@dummy.com', 'Petr', 'Mdly','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING', 'test competency',  '["COACH", "USER"]');


INSERT INTO coach (username, public_profile,  web_link, bio, experience_since, rate, internal_rate)
VALUES ('petr.cocah@dummy.com', true, 'http://some_video1', 'Experienced coach', '2021-01-01', '$', 90);


insert into coach_user_note(coach_id, user_id, note) values ('petr.cocah@dummy.com', 'jakub.user2@dummy.com', 'note1')