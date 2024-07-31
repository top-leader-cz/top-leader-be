alter table feedback_form add column summary text;
insert into ai_prompt (id, value) values ('FEEDBACK_SUMMARY', 'test {0} {1}');
