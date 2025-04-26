alter table scheduled_session add column status varchar(30) not null default 'UPCOMING';
update scheduled_session set status = 'UPCOMING' where time > now();
update scheduled_session set status = 'PENDING' where time < now();