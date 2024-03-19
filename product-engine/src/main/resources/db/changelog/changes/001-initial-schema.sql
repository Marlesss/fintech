CREATE TABLE product
(
    code                   VARCHAR PRIMARY KEY,
    min_term               INT,
    max_term               INT,
    min_principal_amount   INT,
    max_principal_amount   INT,
    min_interest           DECIMAL,
    max_interest           DECIMAL,
    min_origination_amount INT,
    max_origination_amount INT
);

CREATE TABLE agreement
(
    id                 SERIAL PRIMARY KEY,
    product_code       VARCHAR REFERENCES product (code),
    client_id          INT,
    term               INT,
    interest           DECIMAL,
    origination_amount INT,
    status             VARCHAR NOT NULL CHECK ( status in ('NEW', 'ACTIVE', 'CLOSED')),
    disbursement_date  DATE,
    next_payment_date  DATE,
    principal_amount INT
);

CREATE TABLE payment_schedule
(
    id               SERIAL PRIMARY KEY,
    agreement_number INT REFERENCES agreement (id),
    version          INT,
    CONSTRAINT unique_versions UNIQUE (id, version)
);

CREATE TABLE payment_schedule_payment
(
    payment_schedule_id INT REFERENCES payment_schedule (id),
    status              VARCHAR NOT NULL CHECK ( status in ('PAID', 'OVERDUE', 'FUTURE')),
    payment_date        DATE,
    period_payment      DECIMAL,
    interest_payment    DECIMAL,
    principal_payment   DECIMAL,
    period_number       INT
);