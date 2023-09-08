create table notification
(
    id         bigint       not null
        primary key,
    context    text,
    created_at timestamp(6),
    read       boolean      not null,
    type       varchar(255) not null,
    username   varchar(255) not null
);

create sequence notification_id_seq;

alter table data_history
    alter column data type text;
