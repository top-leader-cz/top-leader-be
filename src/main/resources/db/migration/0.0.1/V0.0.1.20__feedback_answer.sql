drop table feedback_form_answer;
drop table fb_answer;
drop table fb_recipient;
create table if not exists fb_recipient
(
    id bigserial primary key,
    form_id   bigint       not null references feedback_form (id) on delete cascade,
    recipient varchar(255) not null,
    token varchar(500) not null,
    submitted bool not null default false,
    constraint fb_recipient_unique unique (form_id, recipient)
);
create index if not exists fb_recipient_feedback_form_idx on fb_recipient (form_id);

create table if not exists feedback_form_answer
(
    form_id      bigint       not null references feedback_form (id) on delete cascade,
    recipient_id   bigint not null references fb_recipient(id),
    question_key varchar(500) not null,
    answer  text not null,
    primary key (form_id, recipient_id, question_key)
);
create index if not exists feedback_form_answer_feedback_form_idx on feedback_form_answer (form_id);
create index if not exists feedback_form_answer_question_idx on feedback_form_answer (question_key);
create index if not exists feedback_form_answer_recipient_idx on feedback_form_answer (recipient_id);

alter table feedback_form add column created_at timestamp not null;


