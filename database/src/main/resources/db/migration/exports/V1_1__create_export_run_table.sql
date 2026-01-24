CREATE SCHEMA IF NOT EXISTS exports;

CREATE TABLE exports.export_run
(
    id               SERIAL PRIMARY KEY,
    exporter_type    VARCHAR(64) NOT NULL,
    export_name      VARCHAR(128),
    status           VARCHAR(32) NOT NULL,
    started_at       TIMESTAMP NOT NULL,
    finished_at      TIMESTAMP,
    duration_ms      BIGINT,
    exported_files   TEXT,
    upload_success   BOOLEAN,
    error_message    TEXT,
    request_params   TEXT
);