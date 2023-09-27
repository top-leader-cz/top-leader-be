create table token
(
    id       bigint       not null,
    username varchar(255) not null
        constraint fk_token_username references users on delete cascade,
    token    varchar(50)  not null,
    type     varchar(50)  not null
);

create sequence token_id_seq;

create index token_username_idx on token (username);
create index token_token_idx on token (token);
create index token_type_idx on token (type);

