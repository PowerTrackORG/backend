package com.powertrack.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineJpaRepository extends JpaRepository<RoutineJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"days", "days.exercises"})
    Optional<RoutineJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("""
            SELECT new com.powertrack.backend.infrastructure.adapter.out.persistence.RoutineSummaryProjection(
                r.id, r.name, r.description, r.createdAt, (SELECT COUNT(d) FROM RoutineDayJpaEntity d WHERE d.routine = r))
            FROM RoutineJpaEntity r
            WHERE r.userId = :userId
            ORDER BY r.createdAt DESC
            """)
    List<RoutineSummaryProjection> findSummariesByUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM RoutineDayJpaEntity d
            WHERE d.id = :routineDayId AND d.routine.userId = :userId
            """)
    boolean existsRoutineDayOwnedByUser(@Param("routineDayId") UUID routineDayId, @Param("userId") UUID userId);

    @Query("""
            SELECT new com.powertrack.backend.infrastructure.adapter.out.persistence.RoutineExerciseTargetsProjection(
                e.id, e.targetSets, e.targetRepMin, e.targetRepMax)
            FROM RoutineExerciseJpaEntity e
            WHERE e.id = :routineExerciseId AND e.routineDay.routine.userId = :userId
            """)
    Optional<RoutineExerciseTargetsProjection> findRoutineExerciseTargets(
            @Param("routineExerciseId") UUID routineExerciseId, @Param("userId") UUID userId);
}
