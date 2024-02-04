alter table user_message add column notified boolean default false;
alter table users alter column locale type varchar(10) using not null;