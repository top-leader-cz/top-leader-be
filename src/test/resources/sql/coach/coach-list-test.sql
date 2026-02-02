INSERT INTO users (username, email, password, status, authorities, time_zone, first_name, last_name, locale)
VALUES ('coach1', 'coach1.johnson@example.com', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', '[Europe/Prague]', 'John', 'Doe', 'cs'),
       ('coach2', 'coach2.johnson@example.com','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Jane', 'Smith', 'cs'),
       ('coach3', 'coach3.johnson@example.com','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Michael', 'Johnson', 'cs'),
       ('coach4', 'coach4.johnson@example.com', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Michael1', 'Johnson1', 'cs');

INSERT INTO users (username, password, status, authorities, time_zone, coach, credit, scheduled_credit, free_coach, locale)
VALUES ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach1', 400, 0, null, 'cs'),
       ('no-credit-user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach2', 400, 400, 'coach3', 'cs'),
       ('no-credit-user-free-coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach3', 400, 400, 'coach3', 'cs');

INSERT INTO users (username, password, status, authorities, time_zone, coach, credit, scheduled_credit, free_coach, locale)
VALUES ('user-with-filter', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach3', 400, 400, 'coach3', 'cs');

INSERT INTO coach (username, public_profile,  web_link, bio, experience_since, rate, rate_order, linkedin_profile, free_slots, priority, languages, fields)
VALUES ('coach1', true, 'http://some_video1', 'Experienced coach', '2021-01-01', '$', 1, null, false , 0, '["English", "French"]', '["Fitness"]'),
       ('coach2', true,  'http://some_video2', 'Passionate about coaching', '2017-05-15', '$$', 2, null, false, 2, '["English", "Spanish"]', '["Yoga", "Meditation"]'),
       ('coach3', true,  'http://some_video3', 'Certified fitness coach', '2019-09-10', '$$$', 3, 'http://linkac', false, 1, '["German"]', '["Weightlifting"]'),
       ('coach4', false, 'http://some_video4', 'Certified fitness coach', '2019-09-10', '$$$', 3, null, false, 0, '[]', '[]');

insert into coach_availability (id, username, recurring, day_from, time_from, day_to, time_to)
values (nextval('coach_availability_seq'), 'coach1', true, 'MONDAY', '13:00:00', 'MONDAY', '14:00:00');
insert into coach_availability (id, username, recurring, day_from, time_from, day_to, time_to)
values (nextval('coach_availability_seq'), 'coach1', true, 'TUESDAY', '13:00:00', 'TUESDAY', '14:00:00');

insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-08-14 10:00:00', '2023-08-14 12:00:00');
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-08-15 12:00:00', '2023-08-15 14:00:00');
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-09-14 13:00:00', '2023-09-14 14:00:00');
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach1', false, '2023-09-15 12:00:00', '2023-09-15 13:00:00');
insert into coach_availability (id, username, recurring, date_time_from, date_time_to)
values (nextval('coach_availability_seq'), 'coach3', false, now() + interval '25 hour', now() + interval '80 hour');


insert into coach_rate (rate_name, rate_credit, rate_order)
values ('$', 110, 1),
       ('$$', 165, 2),
       ('$$$', 275, 3)
;

INSERT INTO user_coach_rates (username, rate_name)
VALUES ('user-with-filter', '$');

INSERT INTO calendar_sync_info(username, status, sync_type, refresh_token, access_token, owner_url) VALUES ('coach1', 'OK', 'CALENDLY', 'token', 'accessToken', 'https://calendly.com/coach1');
INSERT INTO calendar_sync_info(username, status, sync_type, refresh_token, access_token, owner_url) VALUES ('coach1', 'OK', 'GOOGLE', 'token', 'accessToken', null);

-- Company and package for allocation tests
INSERT INTO company (id, name) VALUES (1, 'Test Company');
INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, created_at, created_by, updated_at)
VALUES (1, 1, 'CORE', 100, 'ACTIVE', now(), 'test', now());

-- Allocations for existing test users
INSERT INTO user_allocation (id, package_id, company_id, username, allocated_units, consumed_units, status, created_at, created_by, updated_at)
VALUES (1, 1, 1, 'user', 10, 0, 'ACTIVE', now(), 'test', now()),
       (2, 1, 1, 'no-credit-user', 5, 5, 'ACTIVE', now(), 'test', now()),
       (3, 1, 1, 'no-credit-user-free-coach', 10, 0, 'ACTIVE', now(), 'test', now());

-- User without allocation for testing no.units.available error
INSERT INTO users (username, password, status, authorities, time_zone, coach, credit, scheduled_credit, company_id, locale)
VALUES ('user-no-allocation', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'UTC', null, 1000, 0, 1, 'cs');

-- User with multiple allocations for testing fallback to second allocation
INSERT INTO users (username, password, status, authorities, time_zone, coach, credit, scheduled_credit, company_id, locale)
VALUES ('user-multi-alloc', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER"]', 'UTC', 'coach1', 1000, 0, 1, 'cs');

INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, created_at, created_by, updated_at)
VALUES (2, 1, 'ADDON', 50, 'ACTIVE', now(), 'test', now());

-- First allocation is full (5/5), second has available units (2/5)
INSERT INTO user_allocation (id, package_id, company_id, username, allocated_units, consumed_units, status, created_at, created_by, updated_at)
VALUES (4, 1, 1, 'user-multi-alloc', 5, 5, 'ACTIVE', now(), 'test', now()),
       (5, 2, 1, 'user-multi-alloc', 5, 2, 'ACTIVE', now(), 'test', now());