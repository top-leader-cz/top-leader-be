insert into users (username, first_name, last_name, password, time_zone, status)
values ('jakub.svezi@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING');

insert into token (username, token, type)
values ('jakub.svezi@dummy.com', 'test-token', 'SET_PASSWORD');
