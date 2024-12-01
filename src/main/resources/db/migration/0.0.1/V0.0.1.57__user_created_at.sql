alter table users add column created_at timestamp default now();
alter table users add column updated_at timestamp default now();