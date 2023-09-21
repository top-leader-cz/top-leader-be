alter table users
    add column status varchar(50) not null default 'PENDING';
alter table users
    add column first_name varchar(255) not null default '';
alter table users
    add column last_name varchar(255) not null default '';


update users set  status = 'AUTHORIZED'

