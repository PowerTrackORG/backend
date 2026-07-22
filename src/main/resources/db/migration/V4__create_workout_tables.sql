CREATE TABLE workout_sessions (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    routine_day_id  UUID NOT NULL REFERENCES routine_days (id),
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP,
    status          VARCHAR(20) NOT NULL,
    overall_feeling VARCHAR(20)
);

CREATE INDEX idx_workout_sessions_user_id ON workout_sessions (user_id);
CREATE INDEX idx_workout_sessions_routine_day_id ON workout_sessions (routine_day_id);
-- Compuesto para el filtro user_id + status = COMPLETED que usa
-- WorkoutLogJpaRepository#findMostRecentCompletedLogs (RNF-04).
CREATE INDEX idx_workout_sessions_user_id_status ON workout_sessions (user_id, status);

CREATE TABLE workout_logs (
    id                   UUID PRIMARY KEY,
    session_id           UUID NOT NULL REFERENCES workout_sessions (id) ON DELETE CASCADE,
    routine_exercise_id  UUID NOT NULL REFERENCES routine_exercises (id),
    notes                VARCHAR(1000)
);

CREATE INDEX idx_workout_logs_session_id ON workout_logs (session_id);
CREATE INDEX idx_workout_logs_routine_exercise_id ON workout_logs (routine_exercise_id);

CREATE TABLE log_sets (
    id               UUID PRIMARY KEY,
    workout_log_id   UUID NOT NULL REFERENCES workout_logs (id) ON DELETE CASCADE,
    set_number       INTEGER NOT NULL,
    weight_kg        NUMERIC(6,2) NOT NULL,
    reps_completed   INTEGER NOT NULL,
    rpe              INTEGER NOT NULL
);

CREATE INDEX idx_log_sets_workout_log_id ON log_sets (workout_log_id);
