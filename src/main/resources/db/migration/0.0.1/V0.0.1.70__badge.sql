create table badge(
    username varchar(500) not null references users (username),
    achievement_type varchar(30) not null,
    month varchar(20) not null,
    year int not null,
    primary key (username, achievement_type, month, year)
)

