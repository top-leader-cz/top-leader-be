insert into company (id, name, business_strategy) values (1, 'Dummy Company', 'Dummy business strategy');

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'en', 1, 'competency', 'position', 'first', 'last');
insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'en', 1, 'competency', 'position', 'first', 'last');
insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user4', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'first', 'last');

insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (1, 'user2', now()::date - interval '3 day',  'coach', false, false);
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (2, 'user3', now()::date - interval '10 day',  'coach', false, false);
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (3, 'user4', now()::date - interval '24 day',  'coach', false, false);

insert into user_action_step(id ,username, label, date, checked)
values (1, 'user2', 'action 1', '2023-08-14', true);
insert into user_action_step(id ,username, label, date, checked)
values (2, 'user2', 'action 1', '2022-08-14', false);
insert into user_action_step(id ,username, label, date, checked)
values (3, 'user2', 'action 2', '2023-08-15', false);

