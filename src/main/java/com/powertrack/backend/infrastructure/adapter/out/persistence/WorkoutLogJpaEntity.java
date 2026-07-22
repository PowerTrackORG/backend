package com.powertrack.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workout_logs")
public class WorkoutLogJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSessionJpaEntity session;

    @Column(name = "routine_exercise_id", nullable = false)
    private UUID routineExerciseId;

    @Column
    private String notes;

    @OneToMany(mappedBy = "workoutLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("setNumber ASC")
    private List<LogSetJpaEntity> sets = new ArrayList<>();

    protected WorkoutLogJpaEntity() {
        // requerido por JPA
    }

    public WorkoutLogJpaEntity(UUID id, WorkoutSessionJpaEntity session, UUID routineExerciseId, String notes) {
        this.id = id;
        this.session = session;
        this.routineExerciseId = routineExerciseId;
        this.notes = notes;
    }

    public void replaceSets(List<LogSetJpaEntity> newSets) {
        this.sets.clear();
        this.sets.addAll(newSets);
    }

    public UUID getId() {
        return id;
    }

    public WorkoutSessionJpaEntity getSession() {
        return session;
    }

    public UUID getRoutineExerciseId() {
        return routineExerciseId;
    }

    public String getNotes() {
        return notes;
    }

    public List<LogSetJpaEntity> getSets() {
        return sets;
    }
}
