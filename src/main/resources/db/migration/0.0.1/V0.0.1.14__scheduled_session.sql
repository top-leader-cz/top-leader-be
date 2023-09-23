create table scheduled_session
(
    id                    bigint       not null,
    username              varchar(255) not null,
    coach_username        varchar(255) not null,
    first_day_of_the_week date,
    time                  timestamp
);

create sequence scheduled_session_id_seq;

create or replace view coach_client_view as
select
    concat(c.username, '_', u.username) as id,
    c.username as coach,
    u.username as client,
    u.first_name as client_first_name,
    u.last_name as client_last_name,
    ls.last_session,
    ns.next_session
from coach c left join users u on c.username = u.coach
left join (select username, coach_username, max(time) as last_session from scheduled_session where time < now() group by username, coach_username) ls
    on ls.username = u.username and ls.coach_username = c.username
left join (select username, coach_username, min(time) as next_session from scheduled_session where time >= now() group by username, coach_username) ns
          on ns.username = u.username and ns.coach_username = c.username
;
