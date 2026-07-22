package com.powertrack.backend.application.workout.port.out;

import com.powertrack.backend.domain.workout.HistoricalExerciseSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Consultas de solo lectura sobre logs de ejercicios ya persistidos, a través de
 * sesiones COMPLETADAS. Se usa tanto para la precarga de histórico (US-03) como para el
 * histórico que necesita la Regla 5 (Deload) del {@code ProgressionRuleEngine}.
 */
public interface WorkoutLogRepositoryPort {

    Optional<PreviousLog> findLatestByRoutineExerciseIdAndUserId(UUID routineExerciseId, UUID userId);

    /**
     * Últimas {@code limit} sesiones COMPLETADAS (más reciente primero) para este mismo
     * {@code routineExerciseId} y usuario. Se usa con {@code limit=2} desde
     * {@code FinishWorkoutSessionService}: junto con el desempeño de la sesión que se
     * está finalizando (que aún no está persistida en el momento de la consulta), forman
     * la ventana de 3 sesiones consecutivas que exige la Regla 5.
     */
    List<HistoricalExerciseSummary> findRecentSessionSummaries(UUID routineExerciseId, UUID userId, int limit);

    record PreviousLog(UUID workoutLogId, Instant performedAt, String notes, List<LogSetData> sets) {
    }

    record LogSetData(int setNumber, BigDecimal weightKg, int repsCompleted, int rpe) {
    }
}
