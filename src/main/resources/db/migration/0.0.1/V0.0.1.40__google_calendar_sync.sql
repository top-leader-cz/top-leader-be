create table google_calendar_sync_info
(
    username          varchar(50)  not null primary key,
    refresh_token     varchar(4000),
    sync_token        varchar(4000),
    status            varchar(255) not null,
    last_sync         timestamp(6),
    enforce_full_sync timestamp(6)
);

create table sync_event
(
    id          bigint       not null
        primary key,
    username    varchar(50)  not null,
    external_id varchar(255) not null,
    start_date  timestamp(6) not null,
    end_date    timestamp(6) not null
);

create sequence sync_event_id_seq
    increment by 1;