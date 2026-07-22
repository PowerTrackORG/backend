package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.domain.routine.Exercise;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExercisePersistenceAdapter implements ExerciseRepositoryPort {

    private final ExerciseJpaRepository jpaRepository;

    public ExercisePersistenceAdapter(ExerciseJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Exercise save(Exercise exercise) {
        ExerciseJpaEntity entity = new ExerciseJpaEntity(
                exercise.getId(), exercise.getName(), exercise.getTargetMuscle(), exercise.getCategory(), exercise.getCreatedByUserId());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Exercise> findPredefinedAndByUser(UUID userId) {
        return jpaRepository.findByCreatedByUserIdIsNullOrCreatedByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, Exercise> findAllByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return jpaRepository.findByIdIn(ids).stream()
                .map(this::toDomain)
                .collect(Collectors.toMap(Exercise::getId, Function.identity()));
    }

    @Override
    public Optional<Exercise> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private Exercise toDomain(ExerciseJpaEntity entity) {
        return Exercise.rehydrate(entity.getId(), entity.getName(), entity.getTargetMuscle(), entity.getCategory(), entity.getCreatedByUserId());
    }
}
