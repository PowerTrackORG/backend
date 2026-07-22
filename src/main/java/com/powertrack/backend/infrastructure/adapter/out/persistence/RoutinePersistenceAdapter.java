package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.domain.routine.Routine;
import com.powertrack.backend.domain.routine.RoutineDay;
import com.powertrack.backend.domain.routine.RoutineExercise;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RoutinePersistenceAdapter implements RoutineRepositoryPort {

    private final RoutineJpaRepository jpaRepository;

    public RoutinePersistenceAdapter(RoutineJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Routine save(Routine routine) {
        RoutineJpaEntity entity = toEntity(routine);
        RoutineJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Routine> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public List<RoutineSummary> findSummariesByUserId(UUID userId) {
        return jpaRepository.findSummariesByUserId(userId).stream()
                .map(projection -> new RoutineSummary(projection.getId(), projection.getName(),
                        projection.getDescription(), projection.getCreatedAt(), Math.toIntExact(projection.getDayCount())))
                .toList();
    }

    @Override
    public boolean existsRoutineDayOwnedByUser(UUID routineDayId, UUID userId) {
        return jpaRepository.existsRoutineDayOwnedByUser(routineDayId, userId);
    }

    @Override
    public Optional<RoutineExerciseTargets> findRoutineExerciseTargets(UUID routineExerciseId, UUID userId) {
        return jpaRepository.findRoutineExerciseTargets(routineExerciseId, userId)
                .map(projection -> new RoutineExerciseTargets(projection.getId(), projection.getTargetSets(),
                        projection.getTargetRepMin(), projection.getTargetRepMax()));
    }

    @Override
    @Transactional
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        if (!jpaRepository.existsByIdAndUserId(id, userId)) {
            return false;
        }
        // Las FKs de routine_days/routine_exercises tienen ON DELETE CASCADE (ver
        // V2__create_routines_tables.sql), por lo que un solo DELETE en cascada a nivel
        // de base de datos elimina también días y ejercicios sin depender de que el
        // grafo de entidades JPA esté cargado en memoria.
        jpaRepository.deleteById(id);
        return true;
    }

    private RoutineJpaEntity toEntity(Routine routine) {
        RoutineJpaEntity routineEntity = new RoutineJpaEntity(
                routine.getId(), routine.getUserId(), routine.getName(), routine.getDescription(), routine.getCreatedAt());

        List<RoutineDayJpaEntity> dayEntities = routine.getDays().stream()
                .map(day -> toEntity(day, routineEntity))
                .toList();
        routineEntity.replaceDays(dayEntities);
        return routineEntity;
    }

    private RoutineDayJpaEntity toEntity(RoutineDay day, RoutineJpaEntity routineEntity) {
        RoutineDayJpaEntity dayEntity = new RoutineDayJpaEntity(day.getId(), routineEntity, day.getDayName(), day.getOrderIndex());
        List<RoutineExerciseJpaEntity> exerciseEntities = day.getExercises().stream()
                .map(exercise -> toEntity(exercise, dayEntity))
                .toList();
        dayEntity.replaceExercises(exerciseEntities);
        return dayEntity;
    }

    private RoutineExerciseJpaEntity toEntity(RoutineExercise exercise, RoutineDayJpaEntity dayEntity) {
        return new RoutineExerciseJpaEntity(
                exercise.getId(), dayEntity, exercise.getExerciseId(), exercise.getOrderIndex(),
                exercise.getTargetSets(), exercise.getTargetRepMin(), exercise.getTargetRepMax(),
                exercise.getRestSeconds(), exercise.getNotes());
    }

    private Routine toDomain(RoutineJpaEntity entity) {
        List<RoutineDay> days = entity.getDays().stream()
                .sorted(Comparator.comparingInt(RoutineDayJpaEntity::getOrderIndex))
                .map(this::toDomain)
                .toList();
        return Routine.rehydrate(entity.getId(), entity.getUserId(), entity.getName(), entity.getDescription(),
                entity.getCreatedAt(), days);
    }

    private RoutineDay toDomain(RoutineDayJpaEntity entity) {
        List<RoutineExercise> exercises = entity.getExercises().stream()
                .sorted(Comparator.comparingInt(RoutineExerciseJpaEntity::getOrderIndex))
                .map(this::toDomain)
                .toList();
        return RoutineDay.rehydrate(entity.getId(), entity.getDayName(), entity.getOrderIndex(), exercises);
    }

    private RoutineExercise toDomain(RoutineExerciseJpaEntity entity) {
        return RoutineExercise.rehydrate(entity.getId(), entity.getExerciseId(), entity.getOrderIndex(),
                entity.getTargetSets(), entity.getTargetRepMin(), entity.getTargetRepMax(),
                entity.getRestSeconds(), entity.getNotes());
    }
}
