delete
from coach_availability;

alter sequence coach_availability_seq increment by 1;
SELECT setval('coach_availability_seq', 1, true);

alter table coach_availability
    drop column day;
alter table coach_availability
    drop column date;
alter table coach_availability
    drop column first_day_of_the_week;
alter table scheduled_session
    drop column first_day_of_the_week;

alter table coach_availability
    add column day_from varchar(255);
alter table coach_availability
    add column day_to varchar(255);
alter table coach_availability
    add column date_time_from timestamp;
alter table coach_availability
    add column date_time_to timestamp;
