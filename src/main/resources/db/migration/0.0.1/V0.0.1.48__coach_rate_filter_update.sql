alter table users
    drop column max_coach_rate;
alter table company
    drop column default_max_coach_rate;

alter table company add primary key (id);
alter table coach_rate add primary key (rate_name);

create table user_coach_rates
(
    username  varchar(255) not null,
    rate_name varchar(255) not null,
    primary key (username, rate_name),
    constraint fk_ucr_username foreign key (username) references users (username),
    constraint fk_ucr_rate_name foreign key (rate_name) references coach_rate (rate_name)
);

create table company_coach_rates
(
    company_id bigint not null,
    rate_name  varchar(255) not null,
    primary key (company_id, rate_name),
    constraint fk_ccr_company_id foreign key (company_id) references company (id),
    constraint fk_ccr_rate_name foreign key (rate_name) references coach_rate (rate_name)
);