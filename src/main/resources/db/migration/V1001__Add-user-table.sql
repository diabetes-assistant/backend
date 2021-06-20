create table users
(
    id       UUID         NOT NULL,
    email    VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR      NOT NULL,
    PRIMARY KEY (id)
);
