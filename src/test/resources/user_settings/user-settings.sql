insert into company (id, name, business_strategy) values (1, 'Dummy Company', 'Dummy business strategy');

insert into users (username, email, first_name, last_name, password, time_zone, status, authorities, company_id, position, aspired_position, aspired_competency)
values ('jakub.svezi@dummy.com', 'jakub.svezi@email.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'AUTHORIZED', '["USER"]', 1, 'test position', 'aspired position', 'aspired competency');

insert into users (username, email, first_name, last_name, password, time_zone, status, authorities)
values ('jakub.manager@dummy.com', 'jakub.manager@dummy.com', 'Jakub', 'Manger','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'AUTHORIZED', '["MANAGER", "USER"]');



