insert into users (username, first_name, last_name, password, time_zone, status, aspired_competency)
values ('jakub.svezi@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING', 'test competency');

insert into token (username, token, type)
values ('jakub.svezi@dummy.com', 'test-token', 'SET_PASSWORD');

-- Reset sequence to avoid duplicate key violations
SELECT setval('token_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM token), false);
