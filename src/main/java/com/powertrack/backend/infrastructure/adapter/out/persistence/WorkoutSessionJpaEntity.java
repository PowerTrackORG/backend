package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.domain.workout.OverallFeeling;
import com.powertrack.backend.domain.workout.SessionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workout_sessions")
public class WorkoutSessionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "routine_day_id", nullable = false)
    private UUID routineDayId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_feeling", length = 20)
    private OverallFeeling overallFeeling;

    // Nota: se usa Set (no List) deliberadamente, siguiendo la misma convención ya
    // resuelta en RoutineJpaEntity.days: evita MultipleBagFetchException de Hibernate al
    // hacer fetch join de "logs" + "logs.sets" (dos colecciones bag) en la misma consulta.
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WorkoutLogJpaEntity> logs = new LinkedHashSet<>();

    protected WorkoutSessionJpaEntity() {
        // requerido por JPA
    }

    public WorkoutSessionJpaEntity(UUID id, UUID userId, UUID routineDayId, Instant startTime, Instant endTime,
                                    SessionStatus status, OverallFeeling overallFeeling) {
        this.id = id;
        this.userId = userId;
        this.routineDayId = routineDayId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.overallFeeling = overallFeeling;
    }

    public void replaceLogs(List<WorkoutLogJpaEntity> newLogs) {
        this.logs.clear();
        this.logs.addAll(newLogs);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoutineDayId() {
        return routineDayId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public OverallFeeling getOverallFeeling() {
        return overallFeeling;
    }

    public Set<WorkoutLogJpaEntity> getLogs() {
        return logs;
    }
}
