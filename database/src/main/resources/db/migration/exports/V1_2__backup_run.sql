CREATE SCHEMA IF NOT EXISTS exports;

CREATE TABLE exports.backup_run
(
    id             BIGSERIAL PRIMARY KEY,
    file_name      VARCHAR(256) NOT NULL,
    status         VARCHAR(32)  NOT NULL,
    started_at     TIMESTAMP    NOT NULL,
    finished_at    TIMESTAMP,
    duration_ms    BIGINT,
    upload_success BOOLEAN,
    error_message  TEXT
);

CREATE INDEX idx_backup_run_started_at ON exports.backup_run (started_at);
CREATE INDEX idx_backup_run_status ON exports.backup_run (status);
