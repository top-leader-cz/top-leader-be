create table calendar_sync_info
(
    username         varchar(250) not null,
--         constraint fk_calendar_sync_info_info
--             references users
--             on delete cascade,
    status        varchar(50)  not null,
    sync_type     varchar(50)  not null,
    refresh_token varchar(500) null,
    access_token  varchar(500) null,
    last_sync     timestamp    null,
    owner_url     varchar(500) null,
    constraint pk_calendar_sync_info
        primary key (username, sync_type)
);

insert into calendar_sync_info (username, status, sync_type, refresh_token)
select username, 'OK', 'GOOGLE', refresh_token from google_calendar_sync_info;