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

drop view if exists hr_view;
create view hr_view as
select u.username,
       u.first_name,
       u.last_name,
       u.coach,
       u.credit,
       u.requested_credit,
       u.scheduled_credit,
       u.sum_requested_credit,
       u.paid_credit,
       u.company_id,
       ui.area_of_development,
       ui.long_term_goal,
       ui.strengths,
       cu.first_name as coach_first_name,
       cu.last_name  as coach_last_name
from users u
         left join user_info ui on u.username = ui.username
         left join users cu on u.coach = cu.username;

drop view if exists my_team_view;
create view my_team_view as
select concat(um.manager_username, '_', um.user_username) as id,
       u.username,
       u.first_name,
       u.last_name,
       u.coach,
       u.credit,
       u.requested_credit,
       u.scheduled_credit,
       u.sum_requested_credit,
       u.paid_credit,
       u.company_id,
       ui.area_of_development,
       ui.long_term_goal,
       ui.strengths,
       um.manager_username                                as manager,
       cu.first_name as coach_first_name,
       cu.last_name  as coach_last_name
from users_managers um
         left join users u on u.username = um.user_username
         left join user_info ui on ui.username = um.user_username
         left join users cu on u.coach = cu.username;

drop view if exists manager_view;
create view manager_view as
select u.username,
       u.first_name,
       u.last_name,
       u.company_id
from users u
where authorities like '%"MANAGER"%';

drop view if exists coach_client_view;
create view coach_client_view as
SELECT concat(c.username, '_', u.username) AS id,
       c.username                          AS coach,
       u.username                          AS client,
       u.first_name                        AS client_first_name,
       u.last_name                         AS client_last_name,
       ls.last_session,
       ns.next_session
FROM users u
         LEFT JOIN coach c ON u.coach::text = c.username::text
         LEFT JOIN (SELECT scheduled_session.username,
                           scheduled_session.coach_username,
                           max(scheduled_session."time") AS last_session
                    FROM scheduled_session
                    WHERE scheduled_session."time" < now()
                    GROUP BY scheduled_session.username, scheduled_session.coach_username) ls
                   ON ls.username::text = u.username::text AND ls.coach_username::text = c.username::text
         LEFT JOIN (SELECT scheduled_session.username,
                           scheduled_session.coach_username,
                           min(scheduled_session."time") AS next_session
                    FROM scheduled_session
                    WHERE scheduled_session."time" >= now()
                    GROUP BY scheduled_session.username, scheduled_session.coach_username) ns
                   ON ns.username::text = u.username::text AND ns.coach_username::text = c.username::text
where coach is not null;

drop view if exists coach_list_view;
create or replace view coach_list_view as
select c.username,
       u.first_name,
       u.last_name,
       c.bio,
       c.email,
       c.experience_since,
       c.public_profile,
       c.rate,
       c.rate_order,
       c.web_link,
       u.time_zone,
       c.linkedin_profile
from coach c
         left join users u on c.username = u.username
;