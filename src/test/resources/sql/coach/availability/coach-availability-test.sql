-- Test data for CoachAvailabilityController tests

-- Insert test coach user
insert into users (username, first_name, last_name, password, time_zone, status)
values ('coach@test.com', 'Test', 'Coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'Europe/Prague', 'ACTIVE');

-- Add COACH role
insert into authority (username, authority)
values ('coach@test.com', 'COACH');
