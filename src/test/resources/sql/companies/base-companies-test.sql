insert into coach_rate (rate_name, rate_credit, rate_order)
values ('$', 110, 1),
       ('$$', 165, 2),
       ('$$$', 275, 3)
;

insert into company(id, name)
values (1, 'company1'),
       (2, 'company2');

select setval('company_id_seq', 2, true);

insert into company_coach_rates(company_id, rate_name)
values (1, '$$$'),
       (2, '$$');
;
