ALTER TABLE scheduled_session
    ADD CONSTRAINT scheduled_session_pkey PRIMARY KEY (id);

create table session_feedback
(
    username varchar(255) not null references users,
    session_id bigint not null references scheduled_session (id),
    answers text not null,
    feedback text null,
    primary key (username, session_id)
)

