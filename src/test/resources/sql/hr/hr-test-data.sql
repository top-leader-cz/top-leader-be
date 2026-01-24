-- Test data for HrController tests

-- Insert test company
insert into company (id, name) values (1, 'Test Company');

-- Insert HR user
insert into users (username, first_name, last_name, password, time_zone, status, company_id, credit)
values ('hr@test.com', 'HR', 'User', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'Europe/Prague', 'ACTIVE', 1, 10);

insert into authority (username, authority)
values ('hr@test.com', 'HR');

-- Insert regular user in same company
insert into users (username, first_name, last_name, password, time_zone, status, company_id, credit)
values ('user@test.com', 'Test', 'User', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'Europe/Prague', 'ACTIVE', 1, 5);

insert into authority (username, authority)
values ('user@test.com', 'USER');

-- Insert HR without company (for testing empty list)
insert into users (username, first_name, last_name, password, time_zone, status, company_id, credit)
values ('hr-no-company@test.com', 'HR', 'NoCompany', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'Europe/Prague', 'ACTIVE', null, 0);

insert into authority (username, authority)
values ('hr-no-company@test.com', 'HR');
