insert into users (username, first_name, last_name, password, status, time_zone, locale)
values ('user', 'John', 'Doe', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');

insert into user_info (id, username, strengths, values, area_of_development, long_term_goal)
values (1, 'user', '["leadership", "communication"]', '["integrity", "growth"]', '["delegation", "time management"]', 'Become a CTO');

insert into data_history (id, created_at, data, type, username)
values (1, '2024-01-15T10:00:00.000+00:00',
        '{"type": "USER_SESSION_TYPE", "areaOfDevelopment": ["delegation"], "longTermGoal": "Become a CTO", "motivation": "Want to grow as a leader", "reflection": "Made progress on delegation skills", "actionSteps": [{"id": 1, "label": "Practice delegation daily", "date": "2024-02-01", "checked": true}]}',
        'USER_SESSION', 'user');

insert into data_history (id, created_at, data, type, username)
values (2, '2024-02-10T10:00:00.000+00:00',
        '{"type": "USER_SESSION_TYPE", "areaOfDevelopment": ["delegation", "time management"], "longTermGoal": "Become a CTO", "motivation": "Focus on time management", "reflection": "Delegation is improving, need to work on prioritization", "actionSteps": [{"id": 2, "label": "Use time blocking technique", "date": "2024-03-01", "checked": false}]}',
        'USER_SESSION', 'user');

insert into users (username, first_name, last_name, password, status, time_zone, locale)
values ('coach1', 'Anna', 'Coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');

insert into coach (username, bio, experience_since, web_link, public_profile, rate, certificate, primary_roles, fields, languages, priority)
values ('coach1', 'Executive coach specializing in leadership development', '2018-01-01', 'http://coach1.com', true, '$', '["PCC", "ACC"]', '["COACH", "MENTOR"]', '["leadership", "management"]', '["en", "cs"]', 10);
