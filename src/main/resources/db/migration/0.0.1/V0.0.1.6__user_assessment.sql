create table user_assessment
(
    question_id bigint       not null,
    username    varchar(255) not null,
    answer      integer,
    primary key (question_id, username)
);