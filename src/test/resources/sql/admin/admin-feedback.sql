insert into fb_question (key)
values ('question.key.1');

insert into feedback_form(id, title, description, username, valid_to, created_at)
values (nextval('feedback_form_id_seq'), 'test-from', 'test description', 'user1','2060-12-12', '2023-10-12');

insert into fb_recipient(id, form_id, recipient, token, submitted)
values (nextval('fb_recipient_id_seq'), 1, 'pepa@cerny.cz', 'token', false);

insert into feedback_form_question(form_id, question_key, required, type)
values (1, 'question.key.1', true, 'PARAGRAPH');

insert into feedback_form_answer(form_id, recipient_id, question_key, answer)
values(1, 1, 'question.key.1', 'answer 21');

