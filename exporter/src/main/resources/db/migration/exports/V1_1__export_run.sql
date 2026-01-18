CREATE SCHEMA IF NOT EXISTS exports;

CREATE TABLE exports.export_run
(
    id             BIGSERIAL PRIMARY KEY,
    exporter_type  VARCHAR(64)  NOT NULL,
    export_name    VARCHAR(128),
    status         VARCHAR(32)  NOT NULL,
    started_at     TIMESTAMP    NOT NULL,
    finished_at    TIMESTAMP,
    duration_ms    BIGINT,
    exported_files TEXT,
    upload_success BOOLEAN,
    error_message  TEXT,
    request_params TEXT
);

CREATE INDEX idx_export_run_started_at ON exports.export_run (started_at);
CREATE INDEX idx_export_run_type_status ON exports.export_run (exporter_type, status);
