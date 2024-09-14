alter table user_info
    drop constraint fk_user_info_users,
    add constraint fk_user_info_users foreign key (username) references users (username) on delete cascade;

alter table user_insight
    drop constraint fk_user_insight_users,
    add constraint fk_user_insight_users foreign key (username) references users (username) on delete cascade;

alter table coach
    drop constraint fk_users_username,
    add constraint fk_users_username foreign key (username) references users (username) on delete cascade;

alter table coach_fields
    drop constraint fkpunxdv3csrj8sxi81ehyke81h,
    add constraint fkpunxdv3csrj8sxi81ehyke81h foreign key (coach_username) references coach (username) on delete cascade;

alter table coach_languages
    drop constraint fklanguages_pcoach_username,
    add constraint fklanguages_pcoach_username foreign key (coach_username) references coach (username) on delete cascade;

alter table coach_image
    drop constraint fk_users_username,
    add constraint fk_users_username foreign key (username) references users (username) on delete cascade;

alter table coach_availability
    add constraint fk_coach_availability_coach foreign key (username) references coach (username) on delete cascade;

alter table google_calendar_sync_info
    add constraint fk_google_calendar_sync_info foreign key (username) references users (username) on delete cascade;