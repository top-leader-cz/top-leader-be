alter table scheduled_session
    add column is_private boolean default false;
alter table scheduled_session
    alter column coach_username drop not null;