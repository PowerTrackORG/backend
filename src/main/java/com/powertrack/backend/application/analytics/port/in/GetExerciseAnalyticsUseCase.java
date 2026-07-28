package com.powertrack.backend.application.analytics.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Serie temporal de desempeño de un ejercicio del catálogo a través de las sesiones
 * COMPLETADAS del usuario autenticado (módulo Analytics). Un punto por sesión, ordenados
 * por fecha ascendente.
 */
public interface GetExerciseAnalyticsUseCase {

    ExerciseAnalyticsResult getExerciseAnalytics(UUID exerciseId, UUID userId, Instant from, Instant to);

    record ExerciseAnalyticsResult(UUID exerciseId, String exerciseName, List<SessionPointResult> points) {
    }

    record SessionPointResult(Instant sessionDate, BigDecimal estimated1RM, BigDecimal maxWeightKg,
                               BigDecimal volumeKg, ReferenceSetResult referenceSet, double avgRpe, double avgReps) {
    }

    record ReferenceSetResult(BigDecimal weightKg, int reps, int rpe) {
    }
}
