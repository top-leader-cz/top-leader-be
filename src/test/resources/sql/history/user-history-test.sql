insert into users (username, password, status)
values ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED');
insert into users (username, password, status)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED');


insert into data_history (id, created_at, data, type, username)
values (1, '2023-11-13T16:40:31.313+00:00', '{"type": "STRENGTH_TYPE","strengths": ["s1", "s2"]}', 'STRENGTHS', 'user');
insert into data_history (id, created_at, data, type, username)
values (2, '2023-11-13T16:40:31.313+00:00', '{"type": "STRENGTH_TYPE","strengths": ["s1", "s2"]}', 'STRENGTHS', 'user2');
insert into data_history (id, created_at, data, type, username)
values (3, '2023-11-13T16:40:31.313+00:00', '{"type": "VALUES_TYPE","values": ["v1", "v2"]}', 'VALUES', 'user');
