ALTER TABLE vocabulary.flashcard_aud
    ADD COLUMN deck VARCHAR(128);

ALTER TABLE vocabulary.flashcard_aud
    ADD COLUMN updated boolean DEFAULT false;
