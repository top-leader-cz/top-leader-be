CREATE TABLE IF NOT EXISTS meeting_info
(
    id            bigserial    PRIMARY KEY,
    username      varchar(50)  NOT NULL,
    provider      varchar(50)  NOT NULL,
    refresh_token varchar(4000),
    access_token  varchar(4000),
    email         varchar(500),
    auto_generate boolean      NOT NULL DEFAULT true,
    status        varchar(50)  NOT NULL DEFAULT 'OK',
    created_at    timestamp    DEFAULT now(),
    CONSTRAINT uq_meeting_info_username UNIQUE (username),
    CONSTRAINT fk_meeting_info_users FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);
