package com.powertrack.backend.domain.workout;

import java.math.BigDecimal;

/**
 * Motor determinista de sugerencias de progresión (PRD §14, "Matriz de Decisión de
 * Progresión"). No usa IA generativa: evalúa condiciones exactas sobre repeticiones
 * logradas, RPE, sensación general de la sesión y (solo para la Regla 5) histórico de
 * sesiones anteriores del mismo ejercicio. Es una clase pura: sin dependencias de Spring
 * ni de JPA, para poder testearla de forma aislada.
 *
 * <h2>Orden de evaluación y prioridad (decisión de diseño)</h2>
 * Se evalúa en el orden <b>5 → 4 → 3 → 2 → 1</b>, es decir, de mayor a menor severidad.
 * Justificación de negocio: las señales de fatiga acumulada (Deload, Regla 5) y de
 * sobrecarga aguda (Reducir Peso, Regla 4) son señales de riesgo/seguridad para el
 * usuario y deben ganar sobre cualquier señal de progreso (Reglas 1-3), incluso si
 * técnicamente varias condiciones se cumplieran a la vez (ej.: un ejercicio con reps
 * excelentes en la sesión actual pero con una tendencia de 3 sesiones de caída de peso
 * detrás — gana Deload, no "Aumentar Peso"). Progresar sobre una base de fatiga no
 * detectada sería el peor resultado posible del motor. Dentro de las señales de progreso,
 * se prioriza el criterio más conservador (Mantener, Regla 3) sobre los más agresivos
 * (Aumentar Repeticiones, Regla 2, y Aumentar Peso, Regla 1).
 *
 * <h2>Supuestos explícitos</h2>
 * El PRD §14 no precisa estos casos y se documentan aquí en vez de improvisarlos en
 * silencio:
 * <ul>
 *   <li><b>RPE por ejercicio vs. por serie:</b> el PRD habla de un único "RPE" por
 *   ejercicio, pero el registro captura RPE por serie. Se usa el RPE MÁXIMO entre las
 *   series del ejercicio (el indicador más conservador de fatiga real, ej. la última
 *   serie al fallo).</li>
 *   <li><b>R_done por ejercicio vs. por serie:</b> para las comparaciones contra
 *   {@code target_min} (Reglas 2 y 3) se usa el MÍNIMO de repeticiones logradas entre
 *   series (la serie más débil determina si el ejercicio "cumplió" el mínimo). Para la
 *   Regla 1 se exige, tal como indica el PRD explícitamente, que TODAS las series lleguen
 *   a {@code target_max}.</li>
 *   <li><b>Regla 5 (ventana de 3 sesiones):</b> se requieren al menos 2 sesiones
 *   históricas COMPLETADAS anteriores (además de la sesión que se está evaluando, que
 *   cuenta como la 3ª y más reciente) para poder evaluar la "ventana de 3 sesiones
 *   consecutivas". Si no hay histórico suficiente, la Regla 5 simplemente no aplica (no
 *   se penaliza a un usuario nuevo en el ejercicio). El "RPE promedio sostenido ≥ 9.5"
 *   se evalúa como el promedio de los RPE promedio de esas 3 sesiones (2 históricas +
 *   actual), combinado con que la sensación de la sesión actual sea Mala.</li>
 *   <li><b>Caso no cubierto por la matriz (gap detectado en el PRD):</b> si una minoría
 *   de series (≤50%) no alcanza {@code target_min} pero tampoco se cumplen las demás
 *   condiciones de ninguna regla (ej. RPE bajo, sensación buena), ninguna de las 5 reglas
 *   aplica literalmente. Se documenta como gap y se usa {@link Recommendation#MAINTAIN}
 *   como valor por defecto más seguro, en vez de inventar un criterio nuevo no solicitado.
 *   Señalado explícitamente para que producto decida si esto merece una regla 6.</li>
 * </ul>
 */
public final class ProgressionRuleEngine {

    private static final double SUSTAINED_FATIGUE_AVG_RPE_THRESHOLD = 9.5;
    private static final double MAJORITY_THRESHOLD = 0.5;
    private static final int MIN_HISTORICAL_SESSIONS_FOR_DELOAD = 2;

    public Recommendation evaluate(ExerciseEvaluationInput input) {
        if (appliesDeload(input)) {
            return Recommendation.DELOAD;
        }
        if (appliesDecreaseWeight(input)) {
            return Recommendation.DECREASE_WEIGHT;
        }
        if (appliesMaintain(input)) {
            return Recommendation.MAINTAIN;
        }
        if (appliesIncreaseReps(input)) {
            return Recommendation.INCREASE_REPS;
        }
        if (appliesIncreaseWeight(input)) {
            return Recommendation.INCREASE_WEIGHT;
        }
        // Gap de la matriz de decisión (ver Javadoc de la clase): ningún patrón exacto
        // aplica. Se mantiene por defecto como la opción más segura.
        return Recommendation.MAINTAIN;
    }

    /**
     * Regla 1: Aumentar Peso. R_done &gt;= target_max en TODAS las series Y RPE &lt;= 8
     * Y sensación en {Buena, Excelente}.
     */
    private boolean appliesIncreaseWeight(ExerciseEvaluationInput input) {
        boolean allSetsMeetMax = input.sets().stream()
                .allMatch(s -> s.repsCompleted() >= input.targetRepMax());
        boolean feelingOk = input.overallFeeling() == OverallFeeling.BUENA
                || input.overallFeeling() == OverallFeeling.EXCELENTE;
        return allSetsMeetMax && maxRpe(input) <= 8 && feelingOk;
    }

    /**
     * Regla 2: Mantener Peso y Aumentar Repeticiones. target_min &lt;= R_done &lt;
     * target_max Y RPE en {7, 8}.
     */
    private boolean appliesIncreaseReps(ExerciseEvaluationInput input) {
        int minReps = minReps(input);
        int maxRpe = maxRpe(input);
        boolean repsInRange = minReps >= input.targetRepMin() && minReps < input.targetRepMax();
        return repsInRange && (maxRpe == 7 || maxRpe == 8);
    }

    /**
     * Regla 3: Mantener Peso y Repeticiones. R_done &gt;= target_min pero RPE = 9 o
     * sensación = Regular.
     */
    private boolean appliesMaintain(ExerciseEvaluationInput input) {
        int minReps = minReps(input);
        return minReps >= input.targetRepMin()
                && (maxRpe(input) == 9 || input.overallFeeling() == OverallFeeling.REGULAR);
    }

    /**
     * Regla 4: Reducir Peso. R_done &lt; target_min en más del 50% de las series O
     * RPE = 10 con sensación Mala.
     */
    private boolean appliesDecreaseWeight(ExerciseEvaluationInput input) {
        long setsBelowMin = input.sets().stream()
                .filter(s -> s.repsCompleted() < input.targetRepMin())
                .count();
        double proportionBelowMin = (double) setsBelowMin / input.sets().size();
        boolean majorityFailed = proportionBelowMin > MAJORITY_THRESHOLD;
        boolean maxEffortAndBadFeeling = maxRpe(input) == 10 && input.overallFeeling() == OverallFeeling.MALA;
        return majorityFailed || maxEffortAndBadFeeling;
    }

    /**
     * Regla 5: Recomendar Descarga (Deload). Durante 3 sesiones consecutivas del mismo
     * ejercicio hay reducción de reps/peso, O el RPE promedio es &gt;= 9.5 de forma
     * sostenida con sensación Mala.
     */
    private boolean appliesDeload(ExerciseEvaluationInput input) {
        if (input.recentHistory().size() < MIN_HISTORICAL_SESSIONS_FOR_DELOAD) {
            return false;
        }
        // recentHistory llega ordenado del más reciente al más antiguo (contrato del
        // puerto de salida WorkoutLogRepositoryPort#findRecentSessionSummaries).
        HistoricalExerciseSummary mostRecentPrevious = input.recentHistory().get(0);
        HistoricalExerciseSummary olderPrevious = input.recentHistory().get(1);

        BigDecimal currentMaxWeight = input.sets().stream()
                .map(ExerciseEvaluationInput.SetPerformance::weightKg)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        double currentAvgReps = input.sets().stream()
                .mapToInt(ExerciseEvaluationInput.SetPerformance::repsCompleted)
                .average().orElse(0);
        double currentAvgRpe = input.sets().stream()
                .mapToInt(ExerciseEvaluationInput.SetPerformance::rpe)
                .average().orElse(0);

        boolean weightDeclining = olderPrevious.maxWeightKg().compareTo(mostRecentPrevious.maxWeightKg()) > 0
                && mostRecentPrevious.maxWeightKg().compareTo(currentMaxWeight) > 0;
        boolean repsDeclining = olderPrevious.avgReps() > mostRecentPrevious.avgReps()
                && mostRecentPrevious.avgReps() > currentAvgReps;
        boolean trendDecline = weightDeclining || repsDeclining;

        double avgRpeAcrossWindow = (olderPrevious.avgRpe() + mostRecentPrevious.avgRpe() + currentAvgRpe) / 3.0;
        boolean sustainedFatigue = avgRpeAcrossWindow >= SUSTAINED_FATIGUE_AVG_RPE_THRESHOLD
                && input.overallFeeling() == OverallFeeling.MALA;

        return trendDecline || sustainedFatigue;
    }

    private int maxRpe(ExerciseEvaluationInput input) {
        return input.sets().stream()
                .mapToInt(ExerciseEvaluationInput.SetPerformance::rpe)
                .max()
                .orElseThrow();
    }

    private int minReps(ExerciseEvaluationInput input) {
        return input.sets().stream()
                .mapToInt(ExerciseEvaluationInput.SetPerformance::repsCompleted)
                .min()
                .orElseThrow();
    }
}
