package com.powertrack.backend.domain.workout;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Entrada del {@link ProgressionRuleEngine} para UN ejercicio (routineExercise) de una
 * sesión que se está finalizando.
 *
 * @param targetRepMin   rango de repeticiones objetivo mínimo, configurado por el usuario
 *                       en su {@code RoutineExercise}.
 * @param targetRepMax   rango de repeticiones objetivo máximo.
 * @param sets           series realizadas en la sesión actual para este ejercicio.
 * @param overallFeeling sensación general de la SESIÓN completa (no del ejercicio).
 * @param recentHistory  resumen de las sesiones COMPLETADAS anteriores más recientes para
 *                        este mismo ejercicio y usuario, ordenado de la más reciente a la
 *                        más antigua. Se usa únicamente para la Regla 5 (Deload); puede
 *                        venir vacío si el usuario no tiene histórico suficiente.
 */
public record ExerciseEvaluationInput(
        int targetRepMin,
        int targetRepMax,
        List<SetPerformance> sets,
        OverallFeeling overallFeeling,
        List<HistoricalExerciseSummary> recentHistory) {

    public ExerciseEvaluationInput {
        Objects.requireNonNull(sets, "sets es obligatorio");
        Objects.requireNonNull(overallFeeling, "overallFeeling es obligatorio");
        Objects.requireNonNull(recentHistory, "recentHistory es obligatorio (usar List.of() si no hay histórico)");
        if (sets.isEmpty()) {
            throw new IllegalArgumentException("sets no puede estar vacío: no se puede evaluar un ejercicio sin series registradas");
        }
        if (targetRepMax < targetRepMin) {
            throw new IllegalArgumentException("targetRepMax debe ser mayor o igual a targetRepMin");
        }
        sets = List.copyOf(sets);
        recentHistory = List.copyOf(recentHistory);
    }

    /**
     * Desempeño de una serie individual dentro de la sesión que se está evaluando.
     */
    public record SetPerformance(BigDecimal weightKg, int repsCompleted, int rpe) {

        public SetPerformance {
            Objects.requireNonNull(weightKg, "weightKg es obligatorio");
            if (repsCompleted < 1) {
                throw new IllegalArgumentException("repsCompleted debe ser mayor a 0");
            }
            if (rpe < 1 || rpe > 10) {
                throw new IllegalArgumentException("rpe debe estar entre 1 y 10");
            }
        }
    }
}
