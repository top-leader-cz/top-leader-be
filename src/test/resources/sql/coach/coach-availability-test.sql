insert into users (username, password, status, authorities)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]');

insert into users (username, password, status, authorities)
values ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]');


insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach', null, null, 'MONDAY', true, '13:00:00', '14:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach', null, null, 'TUESDAY', true, '12:00:00', '13:00:00');

insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach', '2023-08-14', '2023-08-14', 'MONDAY', false, '13:00:00', '14:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach', '2023-08-15', '2023-08-14', 'TUESDAY', false, '12:00:00', '13:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach', '2023-09-14', '2023-09-14', 'MONDAY', false, '13:00:00', '14:00:00');
insert into coach_availability (id, username, date, first_day_of_the_week, day, recurring, time_from, time_to)
values (nextval('coach_availability_seq'), 'coach', '2023-09-15', '2023-09-14', 'TUESDAY', false, '12:00:00', '13:00:00');