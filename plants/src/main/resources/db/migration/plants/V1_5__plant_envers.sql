CREATE SEQUENCE public.revinfo_seq START WITH 1 INCREMENT BY 50;


CREATE TABLE public.revinfo
(
    rev      INTEGER NOT NULL,
    revtstmp BIGINT,
    PRIMARY KEY (rev)
);


CREATE TABLE plants.plant_aud
(
    id                 INTEGER NOT NULL,
    last_watered_date  DATE,
    next_watered_date  DATE,
    rev                INTEGER NOT NULL,
    revtype            SMALLINT,
    watering_frequency SMALLINT,
    care_instructions  TEXT,
    description        TEXT,
    image              VARCHAR(255),
    location           VARCHAR(255) CHECK (location IN ('LIVING_ROOM', 'BEDROOM', 'KITCHEN', 'UNDEFINED')),
    name               VARCHAR(255),
    species            VARCHAR(255),
    PRIMARY KEY (id, rev)
);
ALTER TABLE IF EXISTS plants.plant_aud
    ADD CONSTRAINT revinfo_fk FOREIGN KEY (rev) REFERENCES revinfo;
