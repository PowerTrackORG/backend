package com.powertrack.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "log_sets")
public class LogSetJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_log_id", nullable = false)
    private WorkoutLogJpaEntity workoutLog;

    @Column(name = "set_number", nullable = false)
    private int setNumber;

    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "reps_completed", nullable = false)
    private int repsCompleted;

    @Column(nullable = false)
    private int rpe;

    protected LogSetJpaEntity() {
        // requerido por JPA
    }

    public LogSetJpaEntity(UUID id, WorkoutLogJpaEntity workoutLog, int setNumber, BigDecimal weightKg,
                            int repsCompleted, int rpe) {
        this.id = id;
        this.workoutLog = workoutLog;
        this.setNumber = setNumber;
        this.weightKg = weightKg;
        this.repsCompleted = repsCompleted;
        this.rpe = rpe;
    }

    public UUID getId() {
        return id;
    }

    public WorkoutLogJpaEntity getWorkoutLog() {
        return workoutLog;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public int getRepsCompleted() {
        return repsCompleted;
    }

    public int getRpe() {
        return rpe;
    }
}
