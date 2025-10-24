INSERT INTO users (username, password, status, authorities, time_zone, first_name, last_name, locale)
VALUES ('coach1', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'John', 'Doe', 'cs'),
       ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Jane', 'Smith', 'cs'),
       ('coach3', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Michael', 'Johnson', 'cs'),
       ('coach4', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'Michael1', 'Johnson1', 'cs');

INSERT INTO users (username, password, status, authorities, time_zone, coach, credit, scheduled_credit, free_coach, locale)
VALUES ('user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach1', 400, 0, null, 'cs'),
       ('no-credit-user', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach2', 400, 400, 'coach3', 'cs'),
       ('no-credit-user-free-coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach3', 400, 400, 'coach3', 'cs');

INSERT INTO users (username, password, status, authorities, time_zone, coach, credit, scheduled_credit, free_coach, locale)
VALUES ('user-with-filter', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', '["USER", "COACH"]', 'UTC', 'coach3', 400, 400, 'coach3', 'cs');

INSERT INTO coach (username, public_profile, web_link, bio, experience_since, rate, rate_order, linkedin_profile)
VALUES ('coach1', true,  'http://some_video1', 'Experienced coach', '2021-01-01', '$', 1, null),
       ('coach2', true, 'http://some_video2', 'Passionate about coaching', '2017-05-15', '$$', 2, null),
       ('coach3', true,  'http://some_video3', 'Certified fitness coach', '2019-09-10', '$$$', 3, 'http://linkac'),
       ('coach4', false,  'http://some_video4', 'Certified fitness coach', '2019-09-10', '$$$', 3, null);

INSERT INTO calendar_sync_info(username, status, sync_type, refresh_token, owner_url)
VALUES ('coach1', 'OK', 'CALENDLY', 'token', 'https://calendly.com/coach1');