package com.powertrack.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "routine_exercises")
public class RoutineExerciseJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "routine_day_id", nullable = false)
    private RoutineDayJpaEntity routineDay;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "target_sets", nullable = false)
    private int targetSets;

    @Column(name = "target_rep_min", nullable = false)
    private int targetRepMin;

    @Column(name = "target_rep_max", nullable = false)
    private int targetRepMax;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column
    private String notes;

    protected RoutineExerciseJpaEntity() {
        // requerido por JPA
    }

    public RoutineExerciseJpaEntity(UUID id, RoutineDayJpaEntity routineDay, UUID exerciseId, int orderIndex,
                                     int targetSets, int targetRepMin, int targetRepMax, Integer restSeconds, String notes) {
        this.id = id;
        this.routineDay = routineDay;
        this.exerciseId = exerciseId;
        this.orderIndex = orderIndex;
        this.targetSets = targetSets;
        this.targetRepMin = targetRepMin;
        this.targetRepMax = targetRepMax;
        this.restSeconds = restSeconds;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public RoutineDayJpaEntity getRoutineDay() {
        return routineDay;
    }

    public UUID getExerciseId() {
        return exerciseId;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public int getTargetSets() {
        return targetSets;
    }

    public int getTargetRepMin() {
        return targetRepMin;
    }

    public int getTargetRepMax() {
        return targetRepMax;
    }

    public Integer getRestSeconds() {
        return restSeconds;
    }

    public String getNotes() {
        return notes;
    }
}
