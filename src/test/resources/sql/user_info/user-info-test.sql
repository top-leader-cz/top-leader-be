insert into users (username, first_name, last_name, password, status, time_zone, locale)
values ('user', 'Some', 'Dude', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');
insert into users (username, password, status, time_zone, locale)
values ('user2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');
insert into users (username, password, status, time_zone, coach, credit, scheduled_credit, locale)
values ('user_with_coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'coach', 400, 400, 'en');
insert into users (username, password, status, first_name, last_name, time_zone, locale)
values ('coach', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'Mitch', 'Cleverman', 'UTC', 'en');
insert into users (username, password, status, first_name, last_name, time_zone, locale)
values ('coach2', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'Mitch', 'Cleverman', 'UTC', 'cs');

INSERT INTO coach (username, public_profile, email, web_link, bio, experience_since, rate)
VALUES ('coach', true, 'john.doe@example.com', 'http://some_video1', 'Experienced coach', '2021-01-01', '$');

INSERT INTO coach (username, public_profile, email, web_link, bio, experience_since, rate)
VALUES ('coach2', true, 'john.doe@example.com', 'http://some_video1', 'Experienced coach', '2021-01-01', '$');

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

insert into ai_prompt (id, value) values ('LEADERSHIP_STYLE', 'Given a user''s top 5 strengths: %s, and key values: %s, provide a comprehensive yet concise leadership style analysis. Highlight how their unique strengths and values combine to shape their approach to leadership. The analysis should be straightforward and resonate with users of varying backgrounds. Aim for an output that is inspiring and provides clear direction on how they can apply their strengths and values in their leadership role. Keep the analysis under 1000 characters. The text is to be in %s language. Use second person when addressing the user.');
insert into ai_prompt (id, value) values('ANIMAL_SPIRIT', 'Create a fun and engaging ''Animal Spirit Guide'' analysis for a user based on their top 5 strengths: %s, and key values: %s. The analysis should metaphorically link these attributes to an animal known for similar characteristics, providing a brief explanation of the connection. The content should be enlightening, fostering a deeper connection with their leadership style in an enjoyable manner. Ensure the description is succinct, clear, and does not exceed 600 characters. The text is to be in %s language. Use second person when addressing the user.');
insert into ai_prompt (id, value) values('LEADERSHIP_TIP', 'Generate a daily leadership tip within 500 characters, tailored to a user whose top 5 strengths are: %s, and whose key values include: %s. The tip should provide actionable advice, encouraging the user to apply their strengths and values in daily leadership scenarios. Make it inspiring yet practical, suited for leaders looking to improve their skills and positively influence their team. The text is to be in %s language. Use second person when addressing the user.');
insert into ai_prompt (id, value) values ('PERSONAL_GROWTH_TIP', 'Create a ''Personal Growth Tip'' within 500 characters, tailored to a user whose top strengths are %s, and key values include %s. The fact should be related to these strengths and values, offering unique and interesting insights into how they can be leveraged in leadership and self-improvement. Aim for the content to be specific, engaging, and directly relevant to the user''s personal development profile. The text is to be in %s language. Use second person when addressing the user.');
insert into ai_prompt (id, value) values ('WORLD_LEADER_PERSONA', 'Given a user''s top 5 strengths: %s, and key values: %s, from a diverse pool of 20 strengths and 30 values, identify three world leaders who most closely exemplify these unique characteristics. Ensure that these leaders represent diverse backgrounds, and reflect the distinctiveness of the user''s profile. Avoid recurring suggestions such as Nelson Mandela, Angela Merkel, and Jacinda Ardern unless they are the most accurate matches. If finding exact matches is challenging, select leaders who align most closely with the user''s combination of strengths and values. Provide a concise overview of their key leadership traits, main achievements, and the resonance with the user''s profile, avoiding any dictators or controversial figures. Recommend a widely recognized book, documentary, or article for each leader, mentioning the title and encouraging a URL search. The descriptions should be accurate, embellishment-free, and use respectful language, ensuring diverse representation. The response should be in %s language, addressed in the second person, and within an 800-character limit.');
insert into ai_prompt (id, value) values ('LONG_TERM_GOALS', 'Based on the user''s top 5 strengths: %s, selected values: %s, and their chosen area for development: %s, generate three long-term goals. Each goal should be articulated in a short, concise sentence, ensuring clarity and focus. The goals must be specific, measurable, achievable, relevant, and within a 3-6 month timeline. Provide only the goal statements in brief sentences without additional explanations. The text is to be in %s language. Use second person when addressing the user.');
insert into ai_prompt (id, value) values ('ACTIONS_STEPS', 'Based on the user''s top 5 strengths: %s, selected values: %s, their chosen area for development: %s, and their long-term goal: %s, generate three action items, i.e short-term goals, each should be articulated in a short, concise sentence, ensuring clarity and focus. The goals must be specific, measurable, achievable, relevant, and within a 1-2 week timeline. Provide only the goal statements in brief sentences without additional explanations. The text is to be in %s language. Use second person when addressing the user.');
