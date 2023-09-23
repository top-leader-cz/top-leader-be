create table company
(
    id   bigint       not null,
    name varchar(255) not null
);

create sequence company_id_seq;

alter table users add column company_id bigint;
alter table users add column coach varchar(255);
alter table users add column credit integer;
alter table users add column requested_credit integer;
alter table users add column is_trial boolean;