insert into users (username, password, status, authorities)
values ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]');

insert into user_assessment(username, question_id, answer)
values ('user', 1, 1);
insert into user_assessment(username, question_id, answer)
values ('user', 2, 1);
insert into user_assessment(username, question_id, answer)
values ('user', 3, 2);