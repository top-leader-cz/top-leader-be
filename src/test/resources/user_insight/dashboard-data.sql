insert into users (username, first_name, last_name, password, status, time_zone, locale)
values ('user', 'Some', 'Dude', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');



insert into ai_prompt (id, value) values ('USER_PREVIEWS', 'video %s');
insert into ai_prompt (id, value) values ('USER_ARTICLES', 'article %s');
insert into ai_prompt (id, value) values ('SUGGESTION', 'suggestion %s %s %s %s');
