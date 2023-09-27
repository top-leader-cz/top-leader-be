create or replace view admin_view as
select
    u.*,
    cu.first_name as coach_first_name,
    cu.last_name as coach_last_name,
    cc.name as company_name
from users u
left join users cu on u.coach = cu.username
left join company cc on cc.id = u.company_id;