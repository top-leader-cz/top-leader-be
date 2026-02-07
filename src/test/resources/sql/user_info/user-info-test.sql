insert into company (id, name) values (100, 'Test Company');

insert into users (username, first_name, last_name, password, status, time_zone, locale, company_id)
values ('user', 'Some', 'Dude', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en', 100);
insert into users (username, password, status, time_zone, locale)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');
insert into users (username, password, status, time_zone, coach, credit, scheduled_credit, locale)
values ('user_with_coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'coach', 400, 400, 'en');
insert into users (username, password, status, first_name, last_name, time_zone, locale)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'Mitch', 'Cleverman', 'UTC', 'en');
insert into users (username, password, status, first_name, last_name, time_zone, locale)
values ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'Mitch', 'Cleverman', 'UTC', 'cs');

INSERT INTO coach (username, public_profile, web_link, bio, experience_since, rate)
VALUES ('coach', true,  'http://some_video1', 'Experienced coach', '2021-01-01', '$');

INSERT INTO coach (username, public_profile, web_link, bio, experience_since, rate)
VALUES ('coach2', true,  'http://some_video1', 'Experienced coach', '2021-01-01', '$');

insert into coach_rate (rate_name, rate_credit)
values ('$', 110),
       ('$$', 165),
       ('$$$', 275)
;

insert into user_info(username, strengths, values, area_of_development, notes)
values ('user', '["ss1","ss2"]', '["vv1","vv2"]', '["aa1","aa2"]', 'cool note');


insert into user_info(username, strengths, values, area_of_development, notes)
values ('user2', '["s1","s2"]', '["v1","v2"]', '["a1","a2"]', 'cool note');

insert into scheduled_session (id, coach_username, username, time)
values (nextval('scheduled_session_id_seq'), 'coach', 'user_with_coach', '2023-08-14 10:30:00');

-- Allocations for users
INSERT INTO coaching_package (id, company_id, pool_type, total_units, status, created_at, created_by, updated_at)
VALUES (100, 100, 'CORE', 100, 'ACTIVE', now(), 'test', now());

INSERT INTO user_allocation (id, package_id, company_id, username, allocated_units, consumed_units, status, created_at, created_by, updated_at)
VALUES (100, 100, 100, 'user_with_coach', 10, 0, 'ACTIVE', now(), 'test', now());

insert into ai_prompt (id, value) values ('LEADERSHIP_STYLE', 'test leadership {strengths} {values} {language}');
insert into ai_prompt (id, value) values('ANIMAL_SPIRIT', 'test animal {strengths} {values} {language}');
insert into ai_prompt (id, value) values('LEADERSHIP_TIP', 'test tip {strengths} {values} {language}');
insert into ai_prompt (id, value) values ('PERSONAL_GROWTH_TIP', 'test growth {strengths} {values} {areaOfDevelopment} {longTermGoal} {actionSteps} {language}');
insert into ai_prompt (id, value) values ('WORLD_LEADER_PERSONA', 'test persona {strengths} {values} {language}');
insert into ai_prompt (id, value) values ('LONG_TERM_GOALS', 'Based on the user''s top 5 strengths: {strengths}, selected values: {values}, and their chosen area for development: {areaOfDevelopment}, generate three long-term goals. Each goal should be articulated in a short, concise sentence, ensuring clarity and focus. The goals must be specific, measurable, achievable, relevant, and within a 3-6 month timeline. Provide only the goal statements in brief sentences without additional explanations. The text is to be in {language} language. Use second person when addressing the user.');
insert into ai_prompt (id, value) values ('ACTIONS_STEPS', 'Based on the user''s top 5 strengths: %s, selected values: %s, their chosen area for development: %s, and their long-term goal: %s, generate three action items, i.e short-term goals, each should be articulated in a short, concise sentence, ensuring clarity and focus. The goals must be specific, measurable, achievable, relevant, and within a 1-2 week timeline. Provide only the goal statements in brief sentences without additional explanations. The text is to be in %s language. Use second person when addressing the user.');
