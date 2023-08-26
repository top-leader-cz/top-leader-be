alter table user_info
    add long_term_goal varchar(1000);

alter table user_info
    add motivation varchar(2000);

create table user_action_step
(
    id       bigint not null
        primary key,
    checked  boolean,
    date     date,
    label    varchar(255),
    username varchar(255)
);

create sequence user_action_step_seq
    increment by 50;