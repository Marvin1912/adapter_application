CREATE TABLE it_news.article_aud
(
    id           INTEGER   NOT NULL,
    title        VARCHAR(500),
    description  VARCHAR(4000),
    link         VARCHAR(1000),
    source       VARCHAR(255),
    category     VARCHAR(100),
    published_at TIMESTAMP,
    fetched_at   TIMESTAMP,
    rev          INTEGER   NOT NULL,
    revtype      SMALLINT,
    PRIMARY KEY (id, rev)
);

ALTER TABLE IF EXISTS it_news.article_aud
    ADD CONSTRAINT article_aud_revinfo_fk FOREIGN KEY (rev) REFERENCES public.revinfo;
