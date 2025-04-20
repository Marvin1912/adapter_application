CREATE SCHEMA IF NOT EXISTS images;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE images.image
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content BYTEA NOT NULL,
    content_type VARCHAR(100) NOT NULL
);