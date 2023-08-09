create table coach_availability
(
    id                    bigint not null
        primary key,
    date                  date,
    day                   varchar(255)
        constraint coach_availability_day_check
            check ((day)::text = ANY
                   ((ARRAY ['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[])),
    first_day_of_the_week date,
    recurring             boolean,
    time_from             time(6),
    time_to               time(6),
    username              varchar(255)
);

create sequence coach_availability_seq
    increment by 50;