CREATE TABLE client
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR,
    last_name  VARCHAR,
    email      VARCHAR UNIQUE,
    salary     INT
);

CREATE TABLE application
(
    id                            SERIAL PRIMARY KEY,
    client_id                     SERIAL REFERENCES client(id),
    agreement_id                  SERIAL,
    requested_disbursement_amount INT,
    status                        VARCHAR NOT NULL CHECK ( status in ('NEW', 'SCORING', 'ACCEPTED',
                                                                      'CANCELLED', 'REJECTED', 'ACTIVE', 'CLOSED') )
);
