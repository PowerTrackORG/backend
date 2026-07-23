package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.application.workout.port.out.WorkoutSessionRepositoryPort;
import com.powertrack.backend.domain.workout.LogSet;
import com.powertrack.backend.domain.workout.WorkoutLog;
import com.powertrack.backend.domain.workout.WorkoutSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WorkoutSessionPersistenceAdapter implements WorkoutSessionRepositoryPort {

    private final WorkoutSessionJpaRepository jpaRepository;

    public WorkoutSessionPersistenceAdapter(WorkoutSessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public WorkoutSession save(WorkoutSession session) {
        WorkoutSessionJpaEntity entity = toEntity(session);
        WorkoutSessionJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<WorkoutSession> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    private WorkoutSessionJpaEntity toEntity(WorkoutSession session) {
        WorkoutSessionJpaEntity entity = new WorkoutSessionJpaEntity(session.getId(), session.getUserId(),
                session.getRoutineDayId(), session.getStartTime(), session.getEndTime(), session.getStatus(),
                session.getOverallFeeling());

        List<WorkoutLogJpaEntity> logEntities = session.getLogs().stream()
                .map(log -> toEntity(log, entity))
                .toList();
        entity.replaceLogs(logEntities);
        return entity;
    }

    private WorkoutLogJpaEntity toEntity(WorkoutLog log, WorkoutSessionJpaEntity sessionEntity) {
        WorkoutLogJpaEntity logEntity = new WorkoutLogJpaEntity(log.getId(), sessionEntity, log.getRoutineExerciseId(), log.getNotes());
        List<LogSetJpaEntity> setEntities = log.getSets().stream()
                .map(set -> toEntity(set, logEntity))
                .toList();
        logEntity.replaceSets(setEntities);
        return logEntity;
    }

    private LogSetJpaEntity toEntity(LogSet set, WorkoutLogJpaEntity logEntity) {
        return new LogSetJpaEntity(set.getId(), logEntity, set.getSetNumber(), set.getWeightKg(),
                set.getRepsCompleted(), set.getRpe());
    }

    private WorkoutSession toDomain(WorkoutSessionJpaEntity entity) {
        List<WorkoutLog> logs = entity.getLogs().stream()
                .map(this::toDomain)
                .toList();
        return WorkoutSession.rehydrate(entity.getId(), entity.getUserId(), entity.getRoutineDayId(),
                entity.getStartTime(), entity.getEndTime(), entity.getStatus(), entity.getOverallFeeling(), logs);
    }

    private WorkoutLog toDomain(WorkoutLogJpaEntity entity) {
        List<LogSet> sets = entity.getSets().stream()
                .sorted(Comparator.comparingInt(LogSetJpaEntity::getSetNumber))
                .map(this::toDomain)
                .toList();
        return WorkoutLog.rehydrate(entity.getId(), entity.getRoutineExerciseId(), entity.getNotes(), sets);
    }

    private LogSet toDomain(LogSetJpaEntity entity) {
        return LogSet.rehydrate(entity.getId(), entity.getSetNumber(), entity.getWeightKg(),
                entity.getRepsCompleted(), entity.getRpe());
    }
}
