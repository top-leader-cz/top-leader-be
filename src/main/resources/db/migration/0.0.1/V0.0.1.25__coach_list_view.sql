alter table coach
    drop column first_name;
alter table coach
    drop column last_name;
alter table coach
    drop column time_zone;

create or replace view coach_list_view as
select c.username,
       u.first_name,
       u.last_name,
       c.bio,
       c.email,
       c.experience_since,
       c.public_profile,
       c.rate,
       c.web_link,
       u.time_zone
from coach c
         left join users u on c.username = u.username
;