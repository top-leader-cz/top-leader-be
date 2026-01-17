-- TopLeader Database Schema Init Script
-- Generated from existing database structure

-- =====================
-- SEQUENCES
-- =====================
create sequence if not exists data_history_seq;
create sequence if not exists coach_availability_seq;
create sequence if not exists user_action_step_seq;
create sequence if not exists chat_id_seq;
create sequence if not exists message_id_seq;
create sequence if not exists notification_id_seq;
create sequence if not exists company_id_seq;
create sequence if not exists scheduled_session_id_seq;
create sequence if not exists credit_history_seq;
create sequence if not exists sync_event_id_seq;
create sequence if not exists coaching_package_id_seq;
create sequence if not exists user_allocation_id_seq;

-- =====================
-- TABLES
-- =====================

create table if not exists users
(
    username             varchar(50)                                       not null primary key,
    password             varchar(500),
    authorities          varchar(500),
    time_zone            varchar(255),
    status               varchar(50)  default 'PENDING'::character varying not null,
    first_name           varchar(255) default ''::character varying        not null,
    last_name            varchar(255) default ''::character varying        not null,
    company_id           bigint,
    coach                varchar(255),
    credit               integer,
    requested_credit     integer,
    hr_email             varchar(255),
    scheduled_credit     integer      default 0,
    paid_credit          integer      default 0,
    requested_by         varchar(255),
    sum_requested_credit integer      default 0,
    free_coach           varchar(50),
    locale               varchar(20),
    position             varchar(1000),
    aspired_competency   text,
    aspired_position     text,
    created_at           timestamp    default now(),
    updated_at           timestamp    default now(),
    email                varchar(255) not null constraint users_email_unique unique
);

create index if not exists user_email_index on users (email);

create table if not exists authorities
(
    username  varchar(50) not null constraint fk_authorities_users references users,
    authority varchar(50) not null
);

create unique index if not exists ix_auth_username on authorities (username, authority);

create table if not exists user_info
(
    username            varchar(50) not null primary key
        constraint fk_user_info_users references users on delete cascade,
    strengths           varchar(500),
    values              varchar(500),
    area_of_development varchar(500),
    notes               text,
    long_term_goal      varchar(1000),
    motivation          varchar(2000),
    last_reflection     varchar(2000)
);

create table if not exists data_history
(
    id         bigint not null primary key,
    created_at timestamp(6),
    data       text,
    type       varchar(255),
    username   varchar(255)
);

create table if not exists coach
(
    username            varchar(255) not null primary key
        constraint fk_users_username references users on delete cascade,
    bio                 varchar(1000),
    experience_since    date,
    languages           varchar(255),
    public_profile      boolean default false not null,
    rate                varchar(255),
    web_link            varchar(1000),
    linkedin_profile    varchar(1000),
    rate_order          integer,
    free_slots          boolean default false not null,
    internal_rate       integer default 1,
    certificate         jsonb,
    priority            integer default 0,
    primary_roles       jsonb   default '["COACH"]'::jsonb,
    delivery_format     jsonb,
    service_type        jsonb,
    topics              jsonb,
    diagnostic_tools    jsonb,
    industry_experience jsonb,
    base_locations      jsonb,
    user_references     text,
    travel_willingness  varchar(255)
);

create table if not exists coach_fields
(
    coach_username varchar(255) not null
        constraint fkpunxdv3csrj8sxi81ehyke81h references coach on delete cascade,
    fields         varchar(255)
);

create table if not exists coach_languages
(
    coach_username varchar(255) not null
        constraint fklanguages_pcoach_username references coach on delete cascade,
    languages      varchar(255)
);

create table if not exists coach_availability
(
    id             bigint not null primary key,
    recurring      boolean,
    time_from      time(6),
    time_to        time(6),
    username       varchar(255) constraint fk_coach_availability_user references users on delete cascade,
    day_from       varchar(255),
    day_to         varchar(255),
    date_time_from timestamp,
    date_time_to   timestamp
);

create table if not exists user_assessment
(
    question_id bigint       not null,
    username    varchar(255) not null,
    answer      integer,
    primary key (question_id, username)
);

create table if not exists user_action_step
(
    id       bigint not null primary key,
    checked  boolean,
    date     date,
    label    varchar(255),
    username varchar(255)
);

create table if not exists user_chat
(
    chat_id bigint not null primary key,
    user1   varchar(255),
    user2   varchar(255),
    constraint ukhbn1s0q7a8m9nybsrcna43pqb unique (user1, user2)
);

create table if not exists user_message
(
    id           bigint not null primary key,
    created_at   timestamp(6),
    displayed    boolean,
    message_data varchar(3100),
    user_from    varchar(255),
    user_to      varchar(255),
    chat_id      bigint,
    notified     boolean default false
);

create table if not exists last_message
(
    chat_id    bigint not null primary key,
    message_id bigint
);

create table if not exists notification
(
    id         bigint       not null primary key,
    context    text,
    created_at timestamp(6),
    read       boolean      not null,
    type       varchar(255) not null,
    username   varchar(255) not null
);

create table if not exists coach_image
(
    username   varchar(255) not null primary key
        constraint fk_coach_image_users references users on delete cascade,
    image_data oid,
    type       varchar(255) not null
);

create table if not exists company
(
    id                bigint       not null primary key,
    name              varchar(255) not null,
    business_strategy text
);

create table if not exists scheduled_session
(
    id             bigint                                            not null primary key,
    username       varchar(255)                                      not null,
    coach_username varchar(255),
    time           timestamp,
    paid           boolean     default false,
    is_private     boolean     default false,
    status         varchar(30) default 'UPCOMING'::character varying not null,
    created_at     timestamp   default CURRENT_TIMESTAMP             not null,
    updated_at     timestamp   default CURRENT_TIMESTAMP             not null,
    updated_by     varchar(255)
);

create index if not exists idx_scheduled_session_username on scheduled_session (username);
create index if not exists idx_scheduled_session_status on scheduled_session (status);
create index if not exists idx_scheduled_session_username_status on scheduled_session (username, status);
create index if not exists idx_scheduled_session_time on scheduled_session (time);

create table if not exists token
(
    id       bigserial primary key,
    username varchar(255) not null constraint fk_token_username references users on delete cascade,
    token    varchar(500) not null,
    type     varchar(50)  not null
);

create index if not exists token_username_idx on token (username);
create index if not exists token_token_idx on token (token);
create index if not exists token_type_idx on token (type);

create table if not exists fb_question
(
    key              varchar(500)          not null primary key,
    default_question boolean default false not null
);

create table if not exists feedback_form
(
    id          bigserial primary key,
    username    varchar(50) not null references users on delete cascade,
    title       text        not null,
    description text,
    valid_to    timestamp,
    created_at  timestamp   not null,
    summary     text,
    draft       boolean default false not null,
    constraint feedback_form_unique_title unique (title, username)
);

create index if not exists feedback_form_users_idx on feedback_form (username);

create table if not exists feedback_form_question
(
    form_id      bigint               not null references feedback_form on delete cascade,
    question_key varchar(500)         not null references fb_question,
    type         varchar(20)          not null,
    required     boolean default true not null,
    constraint feedback_form_question_unique unique (form_id, question_key)
);

create index if not exists feedback_form_question_feedback_form_idx on feedback_form_question (form_id);
create index if not exists feedback_form_question_question_idx on feedback_form_question (question_key);

create table if not exists fb_recipient
(
    id        bigserial primary key,
    form_id   bigint                not null references feedback_form on delete cascade,
    recipient varchar(255)          not null,
    token     varchar(500)          not null,
    submitted boolean default false not null,
    constraint fb_recipient_unique unique (form_id, recipient)
);

create index if not exists fb_recipient_feedback_form_idx on fb_recipient (form_id);

create table if not exists feedback_form_answer
(
    form_id      bigint       not null references feedback_form on delete cascade,
    recipient_id bigint       not null references fb_recipient,
    question_key varchar(500) not null,
    answer       text         not null,
    primary key (form_id, recipient_id, question_key)
);

create index if not exists feedback_form_answer_feedback_form_idx on feedback_form_answer (form_id);
create index if not exists feedback_form_answer_question_idx on feedback_form_answer (question_key);
create index if not exists feedback_form_answer_recipient_idx on feedback_form_answer (recipient_id);

create table if not exists credit_history
(
    id       bigint not null primary key,
    time     timestamp,
    type     varchar(255),
    username varchar(255),
    credit   integer,
    context  varchar(2000)
);

create table if not exists coach_rate
(
    rate_name   varchar(255) not null primary key,
    rate_credit integer,
    rate_order  integer
);

create table if not exists spring_session
(
    primary_id            char(36) not null constraint spring_session_pk primary key,
    session_id            char(36) not null,
    creation_time         bigint   not null,
    last_access_time      bigint   not null,
    max_inactive_interval integer  not null,
    expiry_time           bigint   not null,
    principal_name        varchar(100)
);

create unique index if not exists spring_session_ix1 on spring_session (session_id);
create index if not exists spring_session_ix2 on spring_session (expiry_time);
create index if not exists spring_session_ix3 on spring_session (principal_name);

create table if not exists spring_session_attributes
(
    session_primary_id char(36)     not null
        constraint spring_session_attributes_fk references spring_session on delete cascade,
    attribute_name     varchar(200) not null,
    attribute_bytes    bytea        not null,
    constraint spring_session_attributes_pk primary key (session_primary_id, attribute_name)
);

create table if not exists user_insight
(
    username                  varchar(255) not null primary key
        constraint fk_user_insight_users references users on delete cascade,
    leadership_style_analysis text,
    animal_spirit_guide       text,
    leadership_tip            text,
    personal_growth_tip       text,
    tips_generated_at         timestamp,
    leadership_pending        boolean default false,
    animal_spirit_pending     boolean default false,
    daily_tips_pending        boolean default false,
    world_leader_persona      text,
    action_goals_pending      boolean default false,
    user_previews             text,
    user_articles             text,
    suggestion                text,
    suggestion_pending        boolean default false not null
);

create table if not exists ai_prompt
(
    id    varchar(255) not null primary key,
    value text         not null
);

create table if not exists users_managers
(
    manager_username varchar(255) not null constraint fk_manager_username references users,
    user_username    varchar(255) not null constraint fk_user_username references users,
    primary key (manager_username, user_username)
);

create table if not exists sync_event
(
    id          bigint       not null primary key,
    username    varchar(50)  not null,
    external_id varchar(255) not null,
    start_date  timestamp(6) not null,
    end_date    timestamp(6) not null
);

create table if not exists user_coach_rates
(
    username  varchar(255) not null constraint fk_ucr_username references users,
    rate_name varchar(255) not null constraint fk_ucr_rate_name references coach_rate,
    primary key (username, rate_name)
);

create table if not exists company_coach_rates
(
    company_id bigint       not null constraint fk_ccr_company_id references company,
    rate_name  varchar(255) not null constraint fk_ccr_rate_name references coach_rate,
    primary key (company_id, rate_name)
);

create table if not exists feedback_notification
(
    id                        bigserial primary key,
    username                  varchar(255) not null
        constraint fk_feedback_notification_users references users on delete cascade,
    notification_time         timestamp(6),
    feedback_form_id          bigint       not null
        constraint fk_feedback_notification_feedback references feedback_form on delete cascade,
    processed_time            timestamp(6),
    status                    varchar(255) not null,
    created_at                timestamp(6) default CURRENT_TIMESTAMP not null,
    manual_available_after    timestamp(6),
    manual_reminder_sent_time timestamp(6)
);

create table if not exists favorite_coach
(
    username       varchar(255) not null,
    coach_username varchar(255) not null,
    constraint pk_favorite_coach primary key (username, coach_username)
);

create table if not exists calendly_info
(
    username      varchar(500) not null primary key,
    refresh_token text         not null,
    owner_url     text         not null
);

create table if not exists calendar_sync_info
(
    username      varchar(250) not null constraint fk_calendar_sync_info_info references users on delete cascade,
    status        varchar(50)  not null,
    sync_type     varchar(50)  not null,
    refresh_token varchar(4000),
    access_token  varchar(4000),
    last_sync     timestamp default now(),
    owner_url     varchar(500),
    email         varchar(500),
    constraint pk_calendar_sync_info primary key (username, sync_type)
);

create index if not exists calendar_sync_info_email_idx on calendar_sync_info (email);

create table if not exists coach_availability_settings
(
    coach    varchar(255) not null primary key
        constraint fk_cas_users_username references coach on delete cascade,
    type     varchar(255) not null,
    resource varchar(500),
    active   boolean      not null
);

create table if not exists badge
(
    username         varchar(500) not null references users,
    achievement_type varchar(30)  not null,
    month            varchar(20)  not null,
    year             integer      not null,
    primary key (username, achievement_type, month, year)
);

create table if not exists coach_user_note
(
    coach_id   varchar(500) not null references coach,
    user_id    varchar(500) not null references users,
    note       text,
    created_at timestamp default CURRENT_TIMESTAMP,
    primary key (user_id, coach_id)
);

create table if not exists session_feedback
(
    username   varchar(255) not null references users,
    session_id bigint       not null references data_history,
    answers    text         not null,
    feedback   text,
    primary key (username, session_id)
);

create table if not exists article
(
    id         bigserial primary key,
    username   varchar not null constraint fk_articles_user references users on delete cascade,
    content    jsonb   not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP
);

create index if not exists idx_articles_username on article (username);

create table if not exists coaching_package
(
    id          bigint       default nextval('coaching_package_id_seq') not null primary key,
    company_id  bigint       not null references company,
    pool_type   varchar(50)  not null,
    total_units integer      not null,
    valid_from  timestamp,
    valid_to    timestamp,
    status      varchar(50)  default 'ACTIVE' not null,
    context_ref varchar(4000),
    created_at  timestamp    default CURRENT_TIMESTAMP not null,
    created_by  varchar(255) not null,
    updated_at  timestamp    default CURRENT_TIMESTAMP,
    updated_by  varchar(255)
);

create index if not exists idx_coaching_package_company_id on coaching_package (company_id);
create index if not exists idx_coaching_package_status on coaching_package (status);
create index if not exists idx_coaching_package_company_status on coaching_package (company_id, status);

create table if not exists user_allocation
(
    id              bigint       default nextval('user_allocation_id_seq') not null primary key,
    company_id      bigint       not null references company,
    package_id      bigint       not null references coaching_package,
    user_id         varchar(255) not null,
    allocated_units integer      default 0 not null,
    consumed_units  integer      default 0 not null,
    status          varchar(50)  default 'ACTIVE' not null,
    context_ref     varchar(4000),
    created_at      timestamp    default CURRENT_TIMESTAMP not null,
    created_by      varchar(255) not null,
    updated_at      timestamp    default CURRENT_TIMESTAMP,
    updated_by      varchar(255),
    constraint uk_user_allocation_package_user unique (package_id, user_id)
);

create index if not exists idx_user_allocation_package_id on user_allocation (package_id);
create index if not exists idx_user_allocation_user_id on user_allocation (user_id);
create index if not exists idx_user_allocation_company_id on user_allocation (company_id);
create index if not exists idx_user_allocation_status on user_allocation (status);

-- =====================
-- FUNCTIONS & TRIGGERS
-- =====================

create or replace function set_email_default() returns trigger
    language plpgsql
as
$$
begin
    if new.email is null then
        new.email := new.username;
    end if;
    return new;
end;
$$;

drop trigger if exists users_email_default_trigger on users;
create trigger users_email_default_trigger
    before insert on users
    for each row execute procedure set_email_default();

-- =====================
-- VIEWS
-- =====================

create or replace view hr_view as
SELECT u.username,
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
       cu.first_name AS coach_first_name,
       cu.last_name  AS coach_last_name
FROM users u
         LEFT JOIN user_info ui ON u.username::text = ui.username::text
         LEFT JOIN users cu ON u.coach::text = cu.username::text
WHERE u.status::text <> 'CANCELED'::text;

create or replace view my_team_view as
SELECT concat(um.manager_username, '_', um.user_username) AS id,
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
       um.manager_username                                AS manager,
       cu.first_name                                      AS coach_first_name,
       cu.last_name                                       AS coach_last_name
FROM users_managers um
         LEFT JOIN users u ON u.username::text = um.user_username::text
         LEFT JOIN user_info ui ON ui.username::text = um.user_username::text
         LEFT JOIN users cu ON u.coach::text = cu.username::text
WHERE u.status::text <> 'CANCELED'::text;

create or replace view manager_view as
SELECT u.username,
       u.first_name,
       u.last_name,
       u.company_id
FROM users u
WHERE u.authorities::text ~~ '%"MANAGER"%'::text
  AND u.status::text <> 'CANCELED'::text;

create or replace view coach_client_view as
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
WHERE u.coach IS NOT NULL
  AND u.status::text <> 'CANCELED'::text;

create or replace view session_reminder_view as
WITH scheduled AS (SELECT max(scheduled_session.username::text) AS username,
                          max(scheduled_session."time")         AS "time"
                   FROM scheduled_session
                   GROUP BY scheduled_session.username)
SELECT DISTINCT u.username,
                u.email,
                u.first_name,
                u.last_name,
                u.locale,
                s."time",
                CASE
                    WHEN s."time"::date = (now()::date - '3 days'::interval) OR
                         u.created_at::date = (now()::date - '3 days'::interval) THEN 'DAYS3'::text
                    WHEN s."time"::date = (now()::date - '10 days'::interval) OR
                         u.created_at::date = (now()::date - '10 days'::interval) THEN 'DAYS10'::text
                    WHEN s."time"::date = (now()::date - '24 days'::interval) OR
                         u.created_at::date = (now()::date - '24 days'::interval) THEN 'DAYS24'::text
                    ELSE 'unknown'::text
                    END AS reminder_interval
FROM users u
         LEFT JOIN scheduled s ON u.username::text = s.username
WHERE (u.status::text <> 'PENDING'::text OR u.status::text <> 'CANCELED'::text)
  AND (u.authorities::text !~~ '%ADMIN%'::text AND u.authorities::text !~~ '%COACH%'::text AND
       u.authorities::text !~~ '%MANAGER%'::text AND u.authorities::text !~~ '%RESPONDENT%'::text OR
       u.authorities::text ~~ '%MANAGER%'::text AND u.authorities::text ~~ '%HR%'::text)
  AND ((s."time"::date = ANY
        (ARRAY [now()::date - '3 days'::interval, now()::date - '10 days'::interval, now()::date - '24 days'::interval])) OR
       s."time" IS NULL AND (u.created_at::date = ANY
                             (ARRAY [now()::date - '3 days'::interval, now()::date - '10 days'::interval, now()::date - '24 days'::interval])));

create or replace view coach_session_view as
SELECT s.id,
       s.username AS client,
       s."time"   AS date,
       s.coach_username,
       CASE
           WHEN s.status::text = 'UPCOMING'::text AND s."time" <= now() THEN 'PENDING'::character varying
           WHEN s.status::text = 'UPCOMING'::text AND s."time" > now() THEN 'UPCOMING'::character varying
           ELSE s.status
           END    AS status,
       u.first_name,
       u.last_name
FROM scheduled_session s
         LEFT JOIN users u ON s.username::text = u.username::text;

create or replace view report_session_view as
SELECT DISTINCT row_number() OVER (ORDER BY u.username, s."time") AS id,
                u.username,
                u.first_name,
                u.last_name,
                s.status,
                u.company_id,
                s."time"                                          AS date
FROM users u
         LEFT JOIN scheduled_session s ON s.username::text = u.username::text
WHERE u.status::text <> 'CANCELED'::text
  AND u.company_id IS NOT NULL;

create or replace view admin_view as
SELECT u.username,
       u.password,
       u.authorities,
       u.time_zone,
       u.status,
       u.first_name,
       u.last_name,
       u.company_id,
       u.coach,
       u.credit,
       u.requested_credit,
       u.hr_email,
       u.scheduled_credit,
       u.paid_credit,
       u.requested_by,
       u.sum_requested_credit,
       u.free_coach,
       u.locale,
       u."position",
       u.aspired_competency,
       u.aspired_position,
       u.created_at,
       u.updated_at,
       u.email,
       cu.first_name          AS coach_first_name,
       cu.last_name           AS coach_last_name,
       cc.name                AS company_name,
       hr_cu.hrs,
       ucr.allowed_coach_rates,
       c.rate,
       c.internal_rate,
       c.certificate,
       c.primary_roles
FROM users u
         LEFT JOIN users cu ON u.coach::text = cu.username::text
         LEFT JOIN coach c ON c.username::text = u.username::text
         LEFT JOIN company cc ON cc.id = u.company_id
         LEFT JOIN (SELECT string_agg(users.username::text, ', '::text) AS hrs,
                           users.company_id
                    FROM users
                    WHERE users.authorities::text ~~ '%"HR"%'::text
                    GROUP BY users.company_id) hr_cu ON hr_cu.company_id = u.company_id
         LEFT JOIN (SELECT string_agg(user_coach_rates.rate_name::text, ', '::text) AS allowed_coach_rates,
                           user_coach_rates.username
                    FROM user_coach_rates
                    GROUP BY user_coach_rates.username) ucr ON ucr.username::text = u.username::text;

create or replace view coach_list_view as
SELECT c.username,
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
FROM coach c
         LEFT JOIN users u ON c.username::text = u.username::text
WHERE u.status::text <> 'CANCELED'::text;
