package com.powertrack.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionJpaRepository extends JpaRepository<WorkoutSessionJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"logs", "logs.sets"})
    Optional<WorkoutSessionJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
