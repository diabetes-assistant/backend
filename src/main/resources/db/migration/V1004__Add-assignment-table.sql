CREATE TABLE assignments
(
    code      VARCHAR(6)  NOT NULL,
    doctorId  UUID        NOT NULL,
    patientId UUID        NOT NULL,
    state     VARCHAR(20) NOT NULL DEFAULT 'initial',
    PRIMARY KEY (code)
);
