insert into users (username, first_name, last_name, password, time_zone, status)
values ('user@mail.cz', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING');

insert into token (username, token, type)
values ('user@mail.cz', 'test-token', 'SET_PASSWORD');
