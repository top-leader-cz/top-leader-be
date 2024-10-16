alter table feedback_notification
    add column created_at timestamp(6) not null default current_timestamp;
alter table feedback_notification
    add column manual_available_after timestamp(6);
alter table feedback_notification
    add column manual_reminder_sent_time timestamp(6);
