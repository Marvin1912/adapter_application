CREATE SCHEMA IF NOT EXISTS plants;


CREATE TABLE plants.plant
(
    id                 SERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    species            VARCHAR(255) NOT NULL,
    description        TEXT         NOT NULL,
    location           VARCHAR(255) NOT NULL,
    watering_frequency smallint     NOT NULL,
    last_watered_date  date,
    image              VARCHAR(255)
);
