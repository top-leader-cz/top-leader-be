alter table users add column free_coach varchar(50);

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