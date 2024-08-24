insert into users (username, first_name, last_name, password, status, time_zone, locale)
values ('user', 'Some', 'Dude', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');


insert into user_insight(username, leadership_style_analysis, animal_spirit_guide, world_leader_persona, user_previews)
values ('user', 'leadership-response', 'animal-response', 'world-leader-persona', 'test-user-previews');

insert into user_info(username, strengths, values, notes)
values ('user', '["solver","ideamaker","flexible","responsible","selfBeliever","concentrated","connector"]',  '["patriotism"]', 'cool note');
