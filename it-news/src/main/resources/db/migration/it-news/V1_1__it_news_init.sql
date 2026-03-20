CREATE SCHEMA IF NOT EXISTS it_news;

CREATE TABLE it_news.article
(
    id           SERIAL PRIMARY KEY,
    title        VARCHAR(500)  NOT NULL,
    description  VARCHAR(4000),
    link         VARCHAR(1000) NOT NULL UNIQUE,
    source       VARCHAR(255)  NOT NULL,
    category     VARCHAR(100)  NOT NULL,
    published_at TIMESTAMP,
    fetched_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_article_category ON it_news.article (category);
CREATE INDEX idx_article_source ON it_news.article (source);
CREATE INDEX idx_article_published_at ON it_news.article (published_at DESC);
