create table token
(
    id       bigserial primary key,
    username varchar(255) not null
        constraint fk_token_username references users on delete cascade,
    token    varchar(500)  not null,
    type     varchar(50)  not null
);

create index token_username_idx on token (username);
create index token_token_idx on token (token);
create index token_type_idx on token (type);

