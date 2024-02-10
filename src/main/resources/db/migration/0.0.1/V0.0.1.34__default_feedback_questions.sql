alter table fb_question add column default_question boolean default false not null;
update fb_question set default_question = true where key like 'question.%'