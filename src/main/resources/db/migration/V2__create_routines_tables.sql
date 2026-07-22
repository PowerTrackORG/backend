CREATE TABLE exercises (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    target_muscle       VARCHAR(100) NOT NULL,
    category            VARCHAR(100) NOT NULL,
    created_by_user_id  UUID REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_exercises_created_by_user_id ON exercises (created_by_user_id);

CREATE TABLE routines (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(1000),
    created_at   TIMESTAMP NOT NULL
);

CREATE INDEX idx_routines_user_id ON routines (user_id);

CREATE TABLE routine_days (
    id           UUID PRIMARY KEY,
    routine_id   UUID NOT NULL REFERENCES routines (id) ON DELETE CASCADE,
    day_name     VARCHAR(255) NOT NULL,
    order_index  INTEGER NOT NULL
);

CREATE INDEX idx_routine_days_routine_id ON routine_days (routine_id);

CREATE TABLE routine_exercises (
    id              UUID PRIMARY KEY,
    routine_day_id  UUID NOT NULL REFERENCES routine_days (id) ON DELETE CASCADE,
    exercise_id     UUID NOT NULL REFERENCES exercises (id),
    order_index     INTEGER NOT NULL,
    target_sets     INTEGER NOT NULL,
    target_rep_min  INTEGER NOT NULL,
    target_rep_max  INTEGER NOT NULL,
    rest_seconds    INTEGER,
    notes           VARCHAR(1000)
);

CREATE INDEX idx_routine_exercises_routine_day_id ON routine_exercises (routine_day_id);
CREATE INDEX idx_routine_exercises_exercise_id ON routine_exercises (exercise_id);
