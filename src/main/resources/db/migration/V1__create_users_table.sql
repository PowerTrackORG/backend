CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    sport_goal      VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_users_email ON users (email);
