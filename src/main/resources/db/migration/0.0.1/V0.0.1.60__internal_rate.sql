alter table coach add column internal_rate integer default 1;

update coach set internal_rate = cr.rate_credit from coach c left join coach_rate cr on c.rate = cr.rate_name;