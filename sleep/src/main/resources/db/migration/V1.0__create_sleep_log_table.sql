CREATE TABLE sleep_log (
    id            BIGSERIAL                PRIMARY KEY,
    user_id       BIGINT                   NOT NULL,
    sleep_date    DATE                     NOT NULL,
    bed_time      TIME                     NOT NULL,
    wake_time     TIME                     NOT NULL,
    total_minutes INT                      NOT NULL,
    feeling       VARCHAR(10)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_user_sleep_date UNIQUE (user_id, sleep_date),
    CONSTRAINT chk_feeling CHECK (feeling IN ('BAD', 'OK', 'GOOD'))
);

CREATE INDEX idx_sleep_log_user_date ON sleep_log (user_id, sleep_date DESC);

COMMENT ON TABLE sleep_log IS 'Stores nightly sleep logs per user';
COMMENT ON COLUMN sleep_log.bed_time IS 'Local time the user went to bed';
COMMENT ON COLUMN sleep_log.wake_time IS 'Local time the user woke up';
COMMENT ON COLUMN sleep_log.total_minutes IS 'Total duration in bed in minutes';
COMMENT ON COLUMN sleep_log.feeling IS 'Morning mood: BAD, OK, GOOD';
