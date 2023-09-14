alter table coach
    drop column photo;
alter table coach
    add column web_link varchar(1000);

create table coach_image
(
    username varchar(255) not null
        primary key
        constraint fk_users_username
            references users,
    image_data    oid,
    type     varchar(255) not null
);

