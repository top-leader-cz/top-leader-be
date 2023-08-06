create table coach
(
    username         varchar(255) not null
        primary key
        constraint fk_users_username
            references users,
    bio              varchar(1000),
    email            varchar(255),
    experience_since date,
    first_name       varchar(255),
    languages        varchar(255),
    last_name        varchar(255),
    photo            oid,
    public_profile   boolean,
    rate             varchar(255),
    time_zone        varchar(255)
);

create table coach_fields
(
    coach_username varchar(255) not null
        constraint fkpunxdv3csrj8sxi81ehyke81h
            references coach,
    fields         varchar(255)
);

create table coach_languages
(
    coach_username varchar(255) not null
        constraint fklanguages_pcoach_username
            references coach,
    languages      varchar(255)
);

