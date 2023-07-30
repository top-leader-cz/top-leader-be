create table user_info
(
    username            varchar(50) not null primary key,
    strengths           varchar(500),
    values              varchar(500),
    area_of_development varchar(500),
    notes               varchar(500),

    constraint fk_user_info_users foreign key (username) references users (username)

);