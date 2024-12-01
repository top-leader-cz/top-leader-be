insert into company (id, name, business_strategy) values (1, 'Dummy Company', 'Dummy business strategy');

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-2-days', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'user-2-days', 'last');
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (1, 'user-2-days', now()::date - interval '2 day',  'coach', false, false);

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-3-days', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'user-3-days', 'last');
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (2, 'user-3-days', now()::date - interval '3 day',  'coach', false, false);

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-10-days', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'user-10-days', 'last');
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (3, 'user-10-days', now()::date - interval '10 day',  'coach', false, false);

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-24-days', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'user-24-days', 'last');
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (4, 'user-24-days', now()::date - interval '24 day',  'coach', false, false);

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-no-session-3-days-scheduled-5', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'first', 'last');
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (5, 'user-no-session-3-days-scheduled-5', now()::date - interval '3 day',  'coach', false, false);
insert into scheduled_session(id, username, time, coach_username, paid, is_private) values (6, 'user-no-session-3-days-scheduled-5', now()::date + interval '5 day',  'coach', false, false);


insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name, created_at)
values ('user-no-session', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'user-no-session', 'last', now()::date - interval '3 day');

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name, created_at)
values ('user-3-days-manager-hr', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["MANAGER", "HR]', 'cs', 1, 'competency', 'position', 'user-3-days-manager-hr', 'last', now()::date - interval '3 day');

insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name, created_at)
values ('user-no-session-4-day', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'cs', 1, 'competency', 'position', 'user-no-session-4-day', 'last', now()::date - interval '4 day');
insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-3-days-manager', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["MANAGER"]', 'cs', 1, 'competency', 'position', 'user-3-days-manager', 'last');
insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-3-days-coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["COACH"]', 'cs', 1, 'competency', 'position', 'user-3-days-coach', 'last');
insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-3-days-admin', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["ADMIN"]', 'cs', 1, 'competency', 'position', 'user-3-days-admin', 'last');
insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-3-days-respondent', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["RESPONDENT"]', 'cs', 1, 'competency', 'position', 'user-3-days-respondent', 'last');
insert into users (username, password, status, authorities, locale, company_id, aspired_competency, aspired_position, first_name, last_name)
values ('user-3-days-pending', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'PENDING', '["USER"]', 'cs', 1, 'competency', 'position', 'user-3-days-pending', 'last');