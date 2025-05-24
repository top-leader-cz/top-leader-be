create table coach_user_note
(
    coach_id   varchar(500) references coach (username),
    user_id    varchar(500) references users (username),
    note       text,
    created_at timestamp default current_timestamp,
    primary key (user_id, coach_id)
)