create table favorite_coach
(
    username varchar(255) not null,
    coach_username varchar(255) not null,
    constraint pk_favorite_coach primary key (username, coach_username)
);
