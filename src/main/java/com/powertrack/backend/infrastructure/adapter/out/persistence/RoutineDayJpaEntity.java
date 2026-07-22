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
@Table(name = "routine_days")
public class RoutineDayJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "routine_id", nullable = false)
    private RoutineJpaEntity routine;

    @Column(name = "day_name", nullable = false)
    private String dayName;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @OneToMany(mappedBy = "routineDay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<RoutineExerciseJpaEntity> exercises = new ArrayList<>();

    protected RoutineDayJpaEntity() {
        // requerido por JPA
    }

    public RoutineDayJpaEntity(UUID id, RoutineJpaEntity routine, String dayName, int orderIndex) {
        this.id = id;
        this.routine = routine;
        this.dayName = dayName;
        this.orderIndex = orderIndex;
    }

    public void replaceExercises(List<RoutineExerciseJpaEntity> newExercises) {
        this.exercises.clear();
        this.exercises.addAll(newExercises);
    }

    public UUID getId() {
        return id;
    }

    public RoutineJpaEntity getRoutine() {
        return routine;
    }

    public String getDayName() {
        return dayName;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public List<RoutineExerciseJpaEntity> getExercises() {
        return exercises;
    }
}
