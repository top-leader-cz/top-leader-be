create table data_history
(
    id         bigint not null
        primary key,
    created_at timestamp(6),
    data       varchar(255),
    type       varchar(255),
    username   varchar(255)
);

create sequence data_history_seq
    increment by 50;
