insert into users (username, first_name, last_name, password, time_zone, status)
values ('jakub.svezi@dummy.com', 'Jakub', 'Svezi','$2a$12$jsTVqLPSt7pqxT.sPYKZ/.y0Vd6E.thnlpAJHghoQhIYihHys6OSO',  'Europe/Prague', 'PENDING');

insert into fb_question (key)
values ('question.key.1');

insert into fb_question (key)
values ('question.key.2');

insert into fb_question (key)
values ('question.key.3');

insert into fb_answer (key, question_key)
values ('answer.key.1.1', 'question.key.1');

insert into fb_answer (key, question_key)
values ('answer.key.1.2', 'question.key.1');

insert into fb_answer (key, question_key)
values ('answer.key.1.3', 'question.key.1');

insert into fb_answer (key, question_key)
values ('answer.key.2.1', 'question.key.2');

insert into fb_answer (key, question_key)
values ('answer.key.2.2', 'question.key.2');

insert into fb_answer (key, question_key)
values ('answer.key.2.3', 'question.key.2');

insert into fb_answer (key, question_key)
values ('answer.key.3.1', 'question.key.3');

insert into fb_answer (key, question_key)
values ('answer.key.3.2', 'question.key.3');

insert into fb_answer (key, question_key)
values ('answer.key.3.3', 'question.key.3');

insert into feedback_form(id, title, description, username, valid_to)
values (1, 'test-from', 'test description', 'jakub.svezi@dummy.com','2023-12-12');

insert into feedback_form_question(form_id, question_key, required, type)
values (1, 'question.key.1', true, 'PARAGRAPH');

insert into feedback_form_question(form_id, question_key, required, type)
values (1, 'question.key.2', false, 'PARAGRAPH');

insert into fb_recipient(form_id, recipient)
values (1, 'pepa@cerny.cz');

insert into fb_recipient(form_id, recipient)
values (1, 'ilja@bily.cz')


