package com.powertrack.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseJpaEntity, UUID> {

    List<ExerciseJpaEntity> findByCreatedByUserIdIsNullOrCreatedByUserId(UUID createdByUserId);

    List<ExerciseJpaEntity> findByIdIn(Collection<UUID> ids);
}
