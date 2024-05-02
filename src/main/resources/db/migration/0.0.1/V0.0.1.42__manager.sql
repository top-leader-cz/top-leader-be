drop view if exists admin_view;
drop view if exists manager_view;

create table users_managers
(
    manager_username varchar(255) not null
        constraint fk_manager_username
            references users,
    user_username varchar(255)not null
        constraint fk_user_username
            references users,
    primary key (manager_username, user_username)
);

alter table users drop column is_trial;
alter table users drop column company;
alter table users add column position varchar(1000);