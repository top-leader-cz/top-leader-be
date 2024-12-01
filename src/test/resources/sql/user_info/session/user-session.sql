insert into company (id, name, business_strategy) values (1, 'Dummy Company', 'Dummy business strategy');
insert into users (username, password, status, authorities)
values ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]');

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, position)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'en', 1, 'competency', 'position');

insert into user_info(username, strengths, values, area_of_development, notes, long_term_goal, motivation, last_reflection)
values ('user2', '["s1","s2"]', '["v1","v2"]', '["a1","a2"]', 'cool note', 'some cool goal', 'I wanna be cool', 'I am cool');
insert into user_action_step(id ,username, label, date, checked)
values (nextval('user_action_step_seq'), 'user2', 'action 1', '2023-08-14', true);
insert into user_action_step(id ,username, label, date, checked)
values (nextval('user_action_step_seq'), 'user2', 'action 2', '2023-08-15', false);
