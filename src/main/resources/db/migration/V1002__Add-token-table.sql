CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE tokens
(
    id         UUID        NOT NULL DEFAULT uuid_generate_v4(),
    type       VARCHAR(12) NOT NULL,
    user_id    UUID        NOT NULL,
    created_at timestamp   NOT NULL,
    PRIMARY KEY (id)
);
