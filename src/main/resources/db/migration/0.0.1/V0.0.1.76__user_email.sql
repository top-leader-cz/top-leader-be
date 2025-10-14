alter table users add column email varchar(255);
update users set email = username;
alter table users alter column email set not null;
alter table users add constraint users_email_unique unique (email);

create index user_email_index on users(email);

-- Create trigger function to set email to username if null
create or replace function set_email_default()
returns trigger as $$
begin
    if new.email is null then
        new.email := new.username;
    end if;
    return new;
end;
$$ language plpgsql;

-- Create trigger on insert
create trigger users_email_default_trigger
before insert on users
for each row
execute function set_email_default();

drop view if exists coach_list_view;
create or replace view coach_list_view as
select c.username,
       u.email,
       u.first_name,
       u.last_name,
       c.bio,
       c.experience_since,
       c.public_profile,
       c.rate,
       c.certificate,
       c.rate_order,
       c.web_link,
       u.time_zone,
       c.linkedin_profile,
       c.free_slots,
       c.priority,
       c.primary_roles
from coach c
         left join users u on c.username = u.username
where u.status != 'CANCELED';

alter table coach drop  column email;