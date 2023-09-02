create table user_chat
(
    chat_id bigint not null
        primary key,
    user1   varchar(255),
    user2   varchar(255),
    constraint ukhbn1s0q7a8m9nybsrcna43pqb
        unique (user1, user2)
);

create sequence chat_id_seq;

create table user_message
(
    id           bigint not null
        primary key,
    created_at   timestamp(6),
    displayed    boolean,
    message_data varchar(1000),
    user_from    varchar(255),
    user_to      varchar(255),
    chat_id      bigint
);

create sequence message_id_seq;

create table last_message
(
    chat_id    bigint not null
        primary key,
    message_id bigint
);

alter table coach drop column time_zone;
alter table users add column time_zone varchar(255);