alter table calendar_sync_info add column email varchar(500) null;
create index calendar_sync_info_email_idx on  calendar_sync_info(email);

-- drop table if exists calendly_info;
drop table if exists google_calendar_sync_info;