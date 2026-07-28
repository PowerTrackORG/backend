package com.powertrack.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionJpaRepository extends JpaRepository<WorkoutSessionJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"logs", "logs.sets"})
    Optional<WorkoutSessionJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Tonelaje total por grupo muscular ({@code Exercise.targetMuscle}), agregando todas
     * las series de todas las sesiones COMPLETADAS del usuario en la ventana. Usado por
     * el módulo Analytics (endpoint "por músculo"). {@code exercise_id} de
     * {@code routine_exercise} no es una asociación JPA navegable (ver Javadoc de
     * {@link RoutineExerciseJpaEntity#getExerciseId()}), de ahí el {@code JOIN ... ON}
     * explícito.
     */
    @Query("""
            SELECT new com.powertrack.backend.infrastructure.adapter.out.persistence.MuscleGroupVolumeProjection(
                e.targetMuscle, SUM(ls.weightKg * ls.repsCompleted))
            FROM WorkoutSessionJpaEntity s
            JOIN s.logs l
            JOIN RoutineExerciseJpaEntity re ON re.id = l.routineExerciseId
            JOIN ExerciseJpaEntity e ON e.id = re.exerciseId
            JOIN l.sets ls
            WHERE s.userId = :userId
              AND s.status = com.powertrack.backend.domain.workout.SessionStatus.COMPLETED
              AND s.endTime BETWEEN :from AND :to
            GROUP BY e.targetMuscle
            ORDER BY SUM(ls.weightKg * ls.repsCompleted) DESC
            """)
    List<MuscleGroupVolumeProjection> sumVolumeByMuscleGroup(@Param("userId") UUID userId,
                                                              @Param("from") Instant from, @Param("to") Instant to);

    /**
     * Tonelaje total de todos los ejercicios de una rutina, sumando las series de las
     * sesiones COMPLETADAS del usuario en la ventana que pertenecen a algún
     * {@code routineDay} de esa rutina. {@code routine_day_id} de
     * {@code workout_session} es un UUID plano sin asociación JPA (mismo criterio que
     * {@code routineExerciseId} en {@code WorkoutLogJpaEntity}), de ahí el
     * {@code JOIN ... ON} explícito hacia {@link RoutineDayJpaEntity}; desde ahí,
     * {@code d.routine} SÍ es una asociación {@code @ManyToOne} navegable.
     */
    @Query("""
            SELECT COALESCE(SUM(ls.weightKg * ls.repsCompleted), 0)
            FROM WorkoutSessionJpaEntity s
            JOIN RoutineDayJpaEntity d ON d.id = s.routineDayId
            JOIN s.logs l
            JOIN l.sets ls
            WHERE d.routine.id = :routineId
              AND s.userId = :userId
              AND s.status = com.powertrack.backend.domain.workout.SessionStatus.COMPLETED
              AND s.endTime BETWEEN :from AND :to
            """)
    BigDecimal sumVolumeForRoutine(@Param("routineId") UUID routineId, @Param("userId") UUID userId,
                                    @Param("from") Instant from, @Param("to") Instant to);

    /**
     * {@code endTime} de las sesiones COMPLETADAS de una rutina dentro de la ventana,
     * ordenados ascendente. Usado por el módulo Analytics (endpoint "por rutina") para
     * contar sesiones completadas y calcular el promedio de días entre sesiones.
     */
    @Query("""
            SELECT s.endTime
            FROM WorkoutSessionJpaEntity s
            JOIN RoutineDayJpaEntity d ON d.id = s.routineDayId
            WHERE d.routine.id = :routineId
              AND s.userId = :userId
              AND s.status = com.powertrack.backend.domain.workout.SessionStatus.COMPLETED
              AND s.endTime BETWEEN :from AND :to
            ORDER BY s.endTime ASC
            """)
    List<Instant> findCompletedSessionEndTimesForRoutine(@Param("routineId") UUID routineId, @Param("userId") UUID userId,
                                                          @Param("from") Instant from, @Param("to") Instant to);
}
