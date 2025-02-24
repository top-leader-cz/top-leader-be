alter table coach add column priority int default 0;
update coach set priority = 10 where username in('Susana Jiménez', 'Minna Hämäläinen', 'Demetrio Mafrica');
