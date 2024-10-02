alter table coach_availability
    drop constraint fk_coach_availability_coach;

alter table coach_availability
    add constraint fk_coach_availability_user foreign key (username) references users (username) on delete cascade;

create table feedback_notification
(
    id                bigserial primary key,
    username          varchar(255) not null,
    notification_time timestamp(6),
    feedback_form_id  bigint       not null,
    processed_time    timestamp(6),
    status            varchar(255) not null,
    constraint fk_feedback_notification_users foreign key (username) references users (username) on delete cascade,
    constraint fk_feedback_notification_feedback foreign key (feedback_form_id) references feedback_form (id) on delete cascade
);
