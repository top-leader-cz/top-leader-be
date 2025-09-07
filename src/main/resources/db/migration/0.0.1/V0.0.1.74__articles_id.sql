create table article (
    id          bigserial primary key,
    username    varchar not null,
    content     jsonb not null,
    created_at  timestamp with time zone default current_timestamp
);

alter table article add constraint fk_articles_user foreign key (username) references users(username)
    on delete cascade;

create index idx_articles_username on article(username);