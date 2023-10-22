insert into users (username, first_name, last_name, password, time_zone, status)
values ('jakub.svezi@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING');

insert into fb_question (key)
values ('question.key.1');

insert into fb_question (key)
values ('question.key.2');

insert into fb_question (key)
values ('question.key.3');

insert into feedback_form(id, title, description, username, valid_to, created_at)
values (1, 'test-from', 'test description', 'jakub.svezi@dummy.com','2060-12-12', '2023-10-12');

insert into feedback_form_question(form_id, question_key, required, type)
values (1, 'question.key.1', true, 'PARAGRAPH');

insert into feedback_form_question(form_id, question_key, required, type)
values (1, 'question.key.2', false, 'PARAGRAPH');

insert into fb_recipient(id, form_id, recipient, token, submitted)
values (1, 1, 'pepa@cerny.cz', 'token', false);

insert into fb_recipient(id, form_id, recipient, token, submitted)
values (2, 1, 'ilja@bily.cz', 'token2', false)


