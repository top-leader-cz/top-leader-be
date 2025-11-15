drop view if exists coach_list_view;
drop view if exists admin_view;

alter table coach alter column certificate type jsonb using
    case
        when certificate is null then null
        else jsonb_build_array(certificate)
    end;

alter table coach add column if not exists rate_order int null;
alter table coach add column if not exists free_slots boolean default false;
alter table coach add column if not exists priority int default 0;
alter table coach add column if not exists travel_willingness varchar(255) null;
alter table coach add column if not exists delivery_format jsonb null;
alter table coach add column if not exists service_type jsonb null;
alter table coach add column if not exists topics jsonb null;
alter table coach add column if not exists diagnostic_tools jsonb null;
alter table coach add column if not exists industry_experience jsonb null;
alter table coach add column if not exists base_locations jsonb null;
alter table coach add column if not exists user_references text null;
