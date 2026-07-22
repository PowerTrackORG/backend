package com.powertrack.backend.domain.routine;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework.
 * Vive dentro de una {@link Routine} como parte de una lista ordenada de días.
 */
public final class RoutineDay {

    private final UUID id;
    private final String dayName;
    private final int orderIndex;
    private final List<RoutineExercise> exercises;

    private RoutineDay(UUID id, String dayName, int orderIndex, List<RoutineExercise> exercises) {
        Objects.requireNonNull(dayName, "dayName es obligatorio");
        Objects.requireNonNull(exercises, "exercises es obligatorio");
        if (dayName.isBlank()) {
            throw new IllegalArgumentException("dayName no puede estar vacío");
        }
        if (orderIndex < 0) {
            throw new IllegalArgumentException("orderIndex no puede ser negativo");
        }
        this.id = id;
        this.dayName = dayName;
        this.orderIndex = orderIndex;
        this.exercises = List.copyOf(exercises);
    }

    public static RoutineDay create(String dayName, int orderIndex, List<RoutineExercise> exercises) {
        return new RoutineDay(UUID.randomUUID(), dayName, orderIndex, exercises);
    }

    public static RoutineDay rehydrate(UUID id, String dayName, int orderIndex, List<RoutineExercise> exercises) {
        return new RoutineDay(id, dayName, orderIndex, exercises);
    }

    public UUID getId() {
        return id;
    }

    public String getDayName() {
        return dayName;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public List<RoutineExercise> getExercises() {
        return exercises;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutineDay that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
