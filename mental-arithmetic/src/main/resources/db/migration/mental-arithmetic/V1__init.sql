CREATE SCHEMA IF NOT EXISTS mental_arithmetic;

CREATE TABLE mental_arithmetic.arithmetic_settings
(
    id                       SERIAL PRIMARY KEY,
    difficulty               VARCHAR(50)    NOT NULL,
    problem_count            INT            NOT NULL,
    time_limit               INT,
    show_immediate_feedback  BOOLEAN        NOT NULL,
    allow_pause              BOOLEAN        NOT NULL,
    show_progress            BOOLEAN        NOT NULL,
    show_timer               BOOLEAN        NOT NULL,
    enable_sound             BOOLEAN        NOT NULL,
    use_keypad               BOOLEAN        NOT NULL,
    session_name             VARCHAR(255),
    shuffle_problems         BOOLEAN        NOT NULL,
    repeat_incorrect_problems BOOLEAN       NOT NULL,
    max_retries              INT            NOT NULL,
    show_correct_answer      BOOLEAN        NOT NULL,
    font_size                VARCHAR(50),
    high_contrast            BOOLEAN
);

CREATE TABLE mental_arithmetic.settings_operations
(
    settings_id    INT            NOT NULL,
    operation_type VARCHAR(50)    NOT NULL,
    PRIMARY KEY (settings_id, operation_type),
    FOREIGN KEY (settings_id) REFERENCES mental_arithmetic.arithmetic_settings (id) ON DELETE CASCADE
);

CREATE TABLE mental_arithmetic.arithmetic_session
(
    id                    VARCHAR(255) PRIMARY KEY,
    created_at            TIMESTAMP      NOT NULL,
    start_time            TIMESTAMP,
    end_time              TIMESTAMP,
    status                VARCHAR(50)    NOT NULL,
    settings_id           INT            NOT NULL,
    current_problem_index INT            NOT NULL,
    score                 INT            NOT NULL,
    correct_answers       INT            NOT NULL,
    incorrect_answers     INT            NOT NULL,
    total_time_spent      BIGINT         NOT NULL,
    problems_completed    INT            NOT NULL,
    total_problems        INT            NOT NULL,
    accuracy              DOUBLE PRECISION NOT NULL,
    avg_time_per_problem  DOUBLE PRECISION NOT NULL,
    is_completed          BOOLEAN        NOT NULL,
    is_timed_out          BOOLEAN        NOT NULL,
    notes                 VARCHAR(255),
    FOREIGN KEY (settings_id) REFERENCES mental_arithmetic.arithmetic_settings (id)
);

CREATE TABLE mental_arithmetic.arithmetic_problem
(
    id             VARCHAR(255) PRIMARY KEY,
    session_id     VARCHAR(255) NOT NULL,
    expression     VARCHAR(255) NOT NULL,
    answer         INT          NOT NULL,
    user_answer    INT,
    is_correct     BOOLEAN,
    time_spent     BIGINT       NOT NULL,
    presented_at   TIMESTAMP    NOT NULL,
    answered_at    TIMESTAMP,
    operation_type VARCHAR(50)  NOT NULL,
    difficulty     VARCHAR(50)  NOT NULL,
    operand1       INT          NOT NULL,
    operand2       INT          NOT NULL,
    FOREIGN KEY (session_id) REFERENCES mental_arithmetic.arithmetic_session (id) ON DELETE CASCADE
);

CREATE INDEX idx_arithmetic_problem_session_id ON mental_arithmetic.arithmetic_problem(session_id);
