CREATE TABLE it_news.feed_config (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    url      VARCHAR(1000) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE it_news.feed_config_aud (
    id       INTEGER NOT NULL,
    name     VARCHAR(255),
    url      VARCHAR(1000),
    category VARCHAR(100),
    active   BOOLEAN,
    rev      INTEGER NOT NULL,
    revtype  SMALLINT,
    PRIMARY KEY (id, rev)
);

ALTER TABLE IF EXISTS it_news.feed_config_aud
    ADD CONSTRAINT feed_config_aud_revinfo_fk FOREIGN KEY (rev) REFERENCES public.revinfo;

-- Seed existing feed configurations
INSERT INTO it_news.feed_config (name, url, category) VALUES
    ('Inside Java', 'https://inside.java/feed.xml', 'Java'),
    ('Baeldung', 'https://www.baeldung.com/feed', 'Java'),
    ('Angular Blog', 'https://blog.angular.dev/feed', 'Angular'),
    ('MIT Technology Review', 'https://www.technologyreview.com/feed/', 'AI'),
    ('The Verge AI', 'https://www.theverge.com/rss/ai-artificial-intelligence/index.xml', 'AI'),
    ('Hacker News Best', 'https://hnrss.org/best', 'General'),
    ('InfoQ', 'https://feed.infoq.com/', 'General');
