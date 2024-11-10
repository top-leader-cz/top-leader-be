insert into company (id, name, business_strategy) values (1, 'Dummy Company', 'Dummy business strategy');

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'first', 'last');

insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (1, 'user2', now()::date - interval '3 day',  'coach', false, false);

