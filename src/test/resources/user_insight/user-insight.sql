insert into users (username, first_name, last_name, password, status, time_zone, locale)
values ('user', 'Some', 'Dude', '$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO', 'AUTHORIZED', 'UTC', 'en');


insert into user_insight(username, leadership_style_analysis, animal_spirit_guide, world_leader_persona, user_previews, user_articles, suggestion)
values ('user', 'leadership-response', 'animal-response', 'world-leader-persona', 'test-user-previews', 'test-user-articles','suggestion');

insert into user_info(username, strengths, values, notes)
values ('user', '["solver","ideamaker","flexible","responsible","selfBeliever","concentrated","connector"]',  '["patriotism"]', 'cool note');
insert into article (username, content)
values ('user', '{
    "title": "title",
    "author": "Scott Berinato",
    "source": "Harvard Business Review",
    "url": "https://hbr.org/2018/04/better-brainstorming",
    "readTime": "6 min read",
    "language": "en",
    "perex": "perex",
    "summaryText": "summary",
    "application": "application",
    "imagePrompt": "prompt",
    "imageData": null,
    "date": "2025-08-25",
    "imageUrl": "gs://ai-images-top-leader/test_image.png"
  }');
