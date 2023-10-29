alter table users
    add column scheduled_credit integer default 0;
alter table users
    add column paid_credit integer default 0;
alter table users
    add column requested_by varchar(255);

alter table scheduled_session
    add column paid boolean default false;

create table credit_history
(
    id       bigint not null
        primary key,
    time     timestamp,
    type     varchar(255),
    username varchar(255),
    credit   integer,
    context  varchar(2000)
);

create sequence credit_history_seq;

create table coach_rate
(
    rate_name   varchar(255),
    rate_credit integer
);

drop view admin_view;

create or replace view admin_view as
select u.*,
       cu.first_name as coach_first_name,
       cu.last_name  as coach_last_name,
       cc.name       as company_name,
       hr_cu.hrs     as hrs
from users u
         left join users cu on u.coach = cu.username
         left join company cc on cc.id = u.company_id
         left join (select string_agg(username, ', ') as hrs, company_id from users where authorities like '%"HR"%' group by company_id) hr_cu
                   on hr_cu.company_id = u.company_id;