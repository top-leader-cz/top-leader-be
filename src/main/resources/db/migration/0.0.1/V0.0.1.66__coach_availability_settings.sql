create table coach_availability_settings
(
    coach      varchar(255)          not null
        primary key
        constraint fk_cas_users_username
            references coach
            on delete cascade,
    type       varchar(255)          not null,
    active    boolean               not null
);