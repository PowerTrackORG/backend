package com.powertrack.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
