insert into company (id, name)
values (1, 'test company');

insert into users (username, first_name, last_name, password, time_zone, status, company_id)
values ('user.one@dummy.com', 'user', 'one','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'AUTHORIZED', 1);

insert into users (username, first_name, last_name, password, time_zone, status, authorities, position, company_id, aspired_competency)
values ('manager.one@dummy.com', 'manager', 'one','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'AUTHORIZED', '["MANAGER", "USER"]', 'dummy position', 1, 'test competency');

insert into users (username, first_name, last_name, password, time_zone, status, authorities, position, company_id)
values ('manager.two@dummy.com', 'manager', 'two','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'AUTHORIZED', '["MANAGER", "USER"]', 'dummy position', 1);
