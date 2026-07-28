package com.powertrack.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WorkoutLogJpaRepository extends JpaRepository<WorkoutLogJpaEntity, UUID> {

    /**
     * Sesiones COMPLETADAS más recientes (más reciente primero) para un mismo
     * {@code routineExerciseId} + usuario. Se usa tanto para la precarga de histórico
     * (US-03, {@code Pageable} con tamaño 1) como para el histórico de la Regla 5 de
     * Deload ({@code Pageable} con tamaño 2).
     * <p>
     * Nota de performance (RNF-04): al combinar fetch join de una colección ("sets") con
     * paginación, Hibernate aplica la paginación en memoria (no a nivel de SQL) y emite
     * una advertencia (HHH90003025). Es aceptable para el MVP porque el número de
     * sesiones históricas por ejercicio y usuario es acotado, pero si el volumen de logs
     * por ejercicio crece mucho conviene revisar esta consulta (ej. separarla en dos
     * pasos: IDs paginados sin fetch join + fetch join por IDs).
     */
    @EntityGraph(attributePaths = {"sets", "session"})
    @Query("""
            SELECT l FROM WorkoutLogJpaEntity l
            WHERE l.routineExerciseId = :routineExerciseId
              AND l.session.userId = :userId
              AND l.session.status = com.powertrack.backend.domain.workout.SessionStatus.COMPLETED
            ORDER BY l.session.endTime DESC
            """)
    List<WorkoutLogJpaEntity> findMostRecentCompletedLogs(@Param("routineExerciseId") UUID routineExerciseId,
                                                           @Param("userId") UUID userId, Pageable pageable);

    /**
     * Todos los logs COMPLETADOS de un {@code exerciseId} del catálogo (resuelto vía
     * {@code routine_exercise.exercise_id}, sin asociación JPA directa — ver Javadoc de
     * {@link RoutineExerciseJpaEntity#getExerciseId()}), ordenados por fecha de fin de
     * sesión ascendente. Usado por el módulo Analytics (endpoint "por ejercicio") para
     * construir la serie temporal histórica; el adaptador agrupa el resultado por sesión.
     */
    @EntityGraph(attributePaths = {"sets", "session"})
    @Query("""
            SELECT l FROM WorkoutLogJpaEntity l
            JOIN RoutineExerciseJpaEntity re ON re.id = l.routineExerciseId
            WHERE re.exerciseId = :exerciseId
              AND l.session.userId = :userId
              AND l.session.status = com.powertrack.backend.domain.workout.SessionStatus.COMPLETED
              AND l.session.endTime BETWEEN :from AND :to
            ORDER BY l.session.endTime ASC
            """)
    List<WorkoutLogJpaEntity> findCompletedLogsForExercise(@Param("exerciseId") UUID exerciseId,
                                                            @Param("userId") UUID userId,
                                                            @Param("from") Instant from, @Param("to") Instant to);

    /**
     * Todos los logs COMPLETADOS de un conjunto de {@code routineExerciseId} (sin filtro
     * de ventana: la comparación de progreso siempre mira las últimas 2 sesiones reales),
     * ordenados por {@code routineExerciseId} y luego por fecha de fin de sesión
     * descendente. Usado por el módulo Analytics (endpoint "por rutina"); el adaptador
     * agrupa el resultado por {@code routineExerciseId} y se queda con los 2 primeros de
     * cada grupo (las 2 sesiones más recientes).
     */
    @EntityGraph(attributePaths = {"sets", "session"})
    @Query("""
            SELECT l FROM WorkoutLogJpaEntity l
            WHERE l.routineExerciseId IN :routineExerciseIds
              AND l.session.userId = :userId
              AND l.session.status = com.powertrack.backend.domain.workout.SessionStatus.COMPLETED
            ORDER BY l.routineExerciseId ASC, l.session.endTime DESC
            """)
    List<WorkoutLogJpaEntity> findCompletedLogsForRoutineExercises(@Param("routineExerciseIds") Collection<UUID> routineExerciseIds,
                                                                    @Param("userId") UUID userId);
}
