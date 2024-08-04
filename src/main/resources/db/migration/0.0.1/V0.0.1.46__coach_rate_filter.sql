alter table users add column max_coach_rate varchar(255);
alter table coach_rate add column rate_order integer;
alter table coach add column rate_order integer;
alter table company add column default_max_coach_rate varchar(255);

update coach_rate set rate_order = 1 where rate_name = '$';
update coach_rate set rate_order = 2 where rate_name = '$$';
update coach_rate set rate_order = 3 where rate_name = '$$$';

update coach set rate_order = 1 where rate = '$';
update coach set rate_order = 2 where rate = '$$';
update coach set rate_order = 3 where rate = '$$$';