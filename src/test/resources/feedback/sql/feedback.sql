insert into fb_question (key, type)
values ('question.key.1', 'PARAGRAPH');

insert into fb_question (key, type)
values ('question.key.2', 'PARAGRAPH');

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

insert into feedback_form(id, title, description, username, valid_to)
values (1, 'test-from', 'test description', 'jakub.svezi@dummy.com','2023-12-12');

insert into feedback_form_question(feedback_form_id, question_key, required)
values (1, 'question.key.1', true);

insert into feedback_form_question(feedback_form_id, question_key, required)
values (1, 'question.key.2', false);

insert into fb_recipient(id, form_id, recipient)
values (1, 1, 'pepa@cerny.cz');

insert into fb_recipient(id, form_id, recipient)
values (2, 1, 'ilja@bily.cz')


