alter table calendar_sync_info
    alter column access_token type varchar(4000);
alter table calendar_sync_info
    alter column refresh_token type varchar(4000);
alter table calendar_sync_info
    alter column last_sync set default now();

update calendar_sync_info
set last_sync = now()
where last_sync is null;

alter table calendar_sync_info
    alter column last_sync
        set not null;


