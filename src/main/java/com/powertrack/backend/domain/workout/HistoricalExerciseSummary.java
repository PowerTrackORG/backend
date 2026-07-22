package com.powertrack.backend.domain.workout;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Agregado de UNA sesión histórica ya COMPLETADA para un mismo
 * {@code routineExerciseId} + usuario, usado exclusivamente por la Regla 5 (Deload) de
 * {@link ProgressionRuleEngine} para detectar tendencias de caída de reps/peso o fatiga
 * sostenida a lo largo de varias sesiones.
 *
 * @param maxWeightKg peso máximo levantado en esa sesión para el ejercicio.
 * @param avgReps     promedio de repeticiones logradas entre las series de esa sesión.
 * @param avgRpe      promedio de RPE entre las series de esa sesión.
 * @param feeling     sensación general de esa sesión.
 */
public record HistoricalExerciseSummary(BigDecimal maxWeightKg, double avgReps, double avgRpe, OverallFeeling feeling) {

    public HistoricalExerciseSummary {
        Objects.requireNonNull(maxWeightKg, "maxWeightKg es obligatorio");
        Objects.requireNonNull(feeling, "feeling es obligatorio");
    }
}
