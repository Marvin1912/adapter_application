CREATE SCHEMA IF NOT EXISTS vocabulary;

CREATE TABLE vocabulary.flashcard
(
    id          SERIAL PRIMARY KEY,
    anki_id     VARCHAR(255) NULL,
    front       VARCHAR(255) NOT NULL,
    back        VARCHAR(255) NOT NULL,
    description TEXT         NULL
);


CREATE TABLE vocabulary.flashcard_aud
(
    id          INTEGER      NOT NULL,
    rev         INTEGER      NOT NULL,
    revtype     SMALLINT,
    anki_id     VARCHAR(255) NULL,
    front       VARCHAR(255) NOT NULL,
    back        VARCHAR(255) NOT NULL,
    description TEXT         NULL,
    PRIMARY KEY (id, rev)
);

ALTER TABLE IF EXISTS vocabulary.flashcard_aud
    ADD CONSTRAINT revinfo_fk FOREIGN KEY (rev) REFERENCES revinfo;

