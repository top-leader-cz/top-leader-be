create table if not exists fb_question
(
    key  varchar(500) not null primary key,
    type varchar(20)  not null

);

create table if not exists fb_answer
(
    key          varchar(500) not null primary key,
    question_key varchar(500) not null references fb_question (key)
);
create index if not exists answer_question_idx on fb_answer (question_key);

create table if not exists feedback_form
(
    id          bigserial primary key,
    title       text         not null,
    description text,
    link        varchar(255) not null,
    valid_to    timestamp    not null
);

create table if not exists feedback_form_question
(
    feedback_form_id      bigint       not null references feedback_form (id) on delete cascade,
    question_key varchar(500) not null references fb_question (key),
    required     bool         not null default true
);

create index if not exists feedback_form_question_feedback_form_idx on feedback_form_question (feedback_form_id);
create index if not exists feedback_form_question_question_idx on feedback_form_question (question_key);

create table if not exists fb_recipient
(
    id        bigserial primary key,
    form_id   bigint       not null references feedback_form (id) on delete cascade,
    recipient varchar(255) not null,
    constraint feedback_form_recipient_unique unique (form_id, recipient)
);
create index if not exists fb_recipient_feedback_form_idx on fb_recipient (form_id);

create table if not exists feedback_form_answer
(
    feedback_form_id bigint       not null references feedback_form (id) on delete cascade,
    question_key     varchar(500) not null references fb_question (key),
    answer_key       varchar(500) not null references fb_answer (key),
    recipient        varchar(255) not null
);
-- create index if not exists feedback_form_answer_feedback_form_idx on feedback_form_answer (form_id);
-- create index if not exists feedback_form_answer_answer_idx on feedback_form_answer (answer_id);





