package com.powertrack.backend.application.workout.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Precarga del histórico anterior (US-03): dado un {@code routineExerciseId}, devuelve el
 * último {@code WorkoutLog} registrado por el usuario autenticado para ese ejercicio, con
 * sus series. Si no hay histórico, se devuelve {@link Optional#empty()} (no es un error).
 */
public interface GetPreviousLogUseCase {

    Optional<PreviousLogResult> getPrevious(UUID routineExerciseId, UUID userId);

    record PreviousLogResult(UUID workoutLogId, UUID routineExerciseId, Instant performedAt, String notes,
                              List<LogSetResult> sets) {
    }

    record LogSetResult(int setNumber, BigDecimal weightKg, int repsCompleted, int rpe) {
    }
}
