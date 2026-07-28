package com.powertrack.backend.application.analytics.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consultas de solo lectura sobre sesiones/logs ya persistidos, agregadas para el módulo
 * Analytics. A diferencia de {@code WorkoutLogRepositoryPort} (usado por el motor de
 * sugerencias), este puerto expone datos ya agrupados por sesión o por grupo muscular,
 * pensados para alimentar directamente a {@code SessionPerformanceAggregator} y
 * {@code ProgressClassifier} del dominio.
 */
public interface AnalyticsRepositoryPort {

    /**
     * Una entrada por cada sesión COMPLETADA del usuario que registró algún
     * {@code WorkoutLog} de este {@code exerciseId} (resuelto vía
     * {@code routine_exercise.exercise_id}), con las series de TODOS los logs de ese
     * ejercicio combinadas en una sola lista si hubiera más de un {@code routineExercise}
     * del mismo ejercicio en la misma sesión. Ordenado por {@code endTime} ascendente.
     */
    List<ExerciseSessionSets> findCompletedSessionSetsForExercise(UUID exerciseId, UUID userId, Instant from, Instant to);

    /**
     * Tonelaje total por {@code Exercise.targetMuscle}, agregando todas las series de
     * todas las sesiones COMPLETADAS del usuario en la ventana. Ordenado por volumen
     * descendente.
     */
    List<MuscleGroupVolume> sumVolumeByMuscleGroup(UUID userId, Instant from, Instant to);

    /**
     * Tonelaje total de todos los ejercicios de la rutina, sumando las series de las
     * sesiones COMPLETADAS del usuario en la ventana que pertenecen a algún
     * {@code routineDay} de esta rutina.
     */
    BigDecimal sumVolumeForRoutine(UUID routineId, UUID userId, Instant from, Instant to);

    /**
     * {@code endTime} de las sesiones COMPLETADAS de esta rutina dentro de la ventana,
     * ordenados ascendente. Usado para contar sesiones y calcular el promedio de días
     * entre sesiones consecutivas.
     */
    List<Instant> findCompletedSessionEndTimesForRoutine(UUID routineId, UUID userId, Instant from, Instant to);

    /**
     * Para cada {@code routineExerciseId} recibido, las 2 sesiones COMPLETADAS más
     * recientes del usuario (más reciente primero), SIN filtrar por ventana: la
     * clasificación de progreso siempre mira las últimas 2 sesiones reales. Si un
     * {@code routineExerciseId} tiene menos de 2 sesiones históricas, su entrada en el
     * mapa tiene menos de 2 elementos (o directamente no está presente si no tiene
     * ninguna). UNA sola llamada para toda la rutina, para evitar N+1.
     */
    Map<UUID, List<ExerciseSessionSets>> findLastTwoSessionsPerRoutineExercise(Collection<UUID> routineExerciseIds, UUID userId);

    record ExerciseSessionSets(UUID sessionId, Instant sessionEndTime, List<SetData> sets) {
    }

    record SetData(BigDecimal weightKg, int repsCompleted, int rpe) {
    }

    record MuscleGroupVolume(String targetMuscle, BigDecimal totalVolumeKg) {
    }
}
