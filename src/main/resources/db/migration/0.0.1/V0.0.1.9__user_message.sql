create table user_message
(
    id           bigint not null
        primary key,
    created_at   timestamp(6),
    displayed    boolean,
    message_data varchar(1000),
    user_from    varchar(255),
    user_to      varchar(255)
);

create sequence message_id_seq;

alter table coach drop column time_zone;
alter table users add column time_zone varchar(255);