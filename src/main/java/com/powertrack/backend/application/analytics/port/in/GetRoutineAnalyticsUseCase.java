package com.powertrack.backend.application.analytics.port.in;

import com.powertrack.backend.domain.analytics.ProgressStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reporte de progreso de una rutina completa (módulo Analytics): tonelaje total en la
 * ventana consultada, resumen de cuántos ejercicios progresaron/mantuvieron/regresaron,
 * y el detalle día por día / ejercicio por ejercicio en el mismo orden que
 * {@code RoutineDetailResponse}.
 */
public interface GetRoutineAnalyticsUseCase {

    RoutineAnalyticsResult getRoutineAnalytics(UUID routineId, UUID userId, Instant from, Instant to);

    record RoutineAnalyticsResult(UUID routineId, String routineName, WindowResult window, BigDecimal totalVolumeKg,
                                   SummaryResult summary, List<DayResult> byDay) {
    }

    /**
     * @param completedSessions          cantidad de sesiones COMPLETADAS de esta rutina
     *                                    dentro de la ventana {@code [from, to]}.
     * @param averageDaysBetweenSessions promedio de días entre sesiones consecutivas
     *                                    dentro de la ventana; {@code null} si hay menos
     *                                    de 2 sesiones en la ventana.
     */
    record WindowResult(Instant from, Instant to, long completedSessions, Double averageDaysBetweenSessions) {
    }

    record SummaryResult(long progressed, long maintained, long regressed, long insufficientData) {
    }

    record DayResult(UUID routineDayId, String dayName, List<ExerciseProgressResult> exercises) {
    }

    /**
     * {@code progressStatus} SIEMPRE compara las últimas 2 sesiones reales del usuario
     * para este {@code routineExerciseId}, sin filtrar por la ventana {@code from}/{@code to}
     * (esa ventana solo acota {@code totalVolumeKg} y {@code window.completedSessions}).
     */
    record ExerciseProgressResult(UUID routineExerciseId, UUID exerciseId, String exerciseName, String targetMuscle,
                                   ProgressStatus progressStatus, SessionSnapshotResult lastSession,
                                   SessionSnapshotResult previousSession) {
    }

    record SessionSnapshotResult(Instant date, ReferenceSetResult referenceSet) {
    }

    record ReferenceSetResult(BigDecimal weightKg, int reps, int rpe) {
    }
}
