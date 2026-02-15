CREATE TABLE vocabulary.deck
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE
);

ALTER TABLE vocabulary.flashcard
    ADD COLUMN deck_id INTEGER,
    ADD COLUMN reverse_flashcard_id INTEGER;

ALTER TABLE vocabulary.flashcard_aud
    ADD COLUMN deck_id INTEGER,
    ADD COLUMN reverse_flashcard_id INTEGER;

INSERT INTO vocabulary.deck (name)
SELECT DISTINCT deck
FROM vocabulary.flashcard
WHERE deck IS NOT NULL;

INSERT INTO vocabulary.deck (name)
SELECT DISTINCT deck || '_reversed'
FROM vocabulary.flashcard
WHERE deck IS NOT NULL
ON CONFLICT (name) DO NOTHING;

UPDATE vocabulary.flashcard f
SET deck_id = d.id
FROM vocabulary.deck d
WHERE f.deck = d.name;

INSERT INTO vocabulary.flashcard (anki_id, front, back, description, deck_id, updated)
SELECT NULL,
       f.back,
       f.front,
       f.description,
       d_rev.id,
       f.updated
FROM vocabulary.flashcard f
         JOIN vocabulary.deck d_rev ON d_rev.name = f.deck || '_reversed'
WHERE f.reverse_flashcard_id IS NULL;

UPDATE vocabulary.flashcard f
SET reverse_flashcard_id = r.id
FROM vocabulary.flashcard r
         JOIN vocabulary.deck d_rev ON r.deck_id = d_rev.id
WHERE f.reverse_flashcard_id IS NULL
  AND r.front = f.back
  AND r.back = f.front
  AND d_rev.name = (SELECT d.name || '_reversed' FROM vocabulary.deck d WHERE d.id = f.deck_id);

UPDATE vocabulary.flashcard r
SET reverse_flashcard_id = f.id
FROM vocabulary.flashcard f
WHERE r.reverse_flashcard_id IS NULL
  AND f.reverse_flashcard_id = r.id;

ALTER TABLE vocabulary.flashcard
    ALTER COLUMN deck_id SET NOT NULL,
    ADD CONSTRAINT flashcard_deck_fk FOREIGN KEY (deck_id) REFERENCES vocabulary.deck (id),
    ADD CONSTRAINT flashcard_reverse_fk FOREIGN KEY (reverse_flashcard_id) REFERENCES vocabulary.flashcard (id);

CREATE INDEX IF NOT EXISTS flashcard_deck_id_idx ON vocabulary.flashcard (deck_id);
CREATE INDEX IF NOT EXISTS flashcard_reverse_flashcard_id_idx ON vocabulary.flashcard (reverse_flashcard_id);

ALTER TABLE vocabulary.flashcard
    DROP COLUMN deck;
