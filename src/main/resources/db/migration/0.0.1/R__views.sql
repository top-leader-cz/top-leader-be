drop view if exists admin_view;
create view admin_view as
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


drop view if exists coach_client_view;
create view coach_client_view as
SELECT concat(c.username, '_', u.username) AS id,
       c.username AS coach,
       u.username AS client,
       u.first_name AS client_first_name,
       u.last_name AS client_last_name,
       ls.last_session,
       ns.next_session
FROM users u
         LEFT JOIN coach c ON u.coach::text = c.username::text
         LEFT JOIN ( SELECT scheduled_session.username,
                            scheduled_session.coach_username,
                            max(scheduled_session."time") AS last_session
                     FROM scheduled_session
                     WHERE scheduled_session."time" < now()
                     GROUP BY scheduled_session.username, scheduled_session.coach_username) ls ON ls.username::text = u.username::text AND ls.coach_username::text = c.username::text
         LEFT JOIN ( SELECT scheduled_session.username,
                            scheduled_session.coach_username,
                            min(scheduled_session."time") AS next_session
                     FROM scheduled_session
                     WHERE scheduled_session."time" >= now()
                     GROUP BY scheduled_session.username, scheduled_session.coach_username) ns ON ns.username::text = u.username::text AND ns.coach_username::text = c.username::text
where coach is not null