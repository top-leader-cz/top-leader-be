drop table  session_feedback;
create table session_feedback
(
    username varchar(255) not null references users,
    session_id bigint not null references data_history (id),
    answers text not null,
    feedback text null,
    primary key (username, session_id)
)

