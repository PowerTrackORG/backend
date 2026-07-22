package com.powertrack.backend.domain.routine;

import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework.
 * <p>
 * Representa la personalización real del usuario dentro de su rutina: apunta a un
 * {@link Exercise} del catálogo pero define sus propios targets (series, rango de
 * repeticiones, descanso). El registro de progreso (futuro módulo de ejecución de
 * entrenamiento) apuntará a {@code routineExerciseId} y no a {@code exerciseId}
 * directamente, para poder comparar siempre contra los targets configurados por el
 * propio usuario en esa rutina.
 */
public final class RoutineExercise {

    private final UUID id;
    private final UUID exerciseId;
    private final int orderIndex;
    private final int targetSets;
    private final int targetRepMin;
    private final int targetRepMax;
    private final Integer restSeconds;
    private final String notes;

    private RoutineExercise(UUID id, UUID exerciseId, int orderIndex, int targetSets, int targetRepMin,
                             int targetRepMax, Integer restSeconds, String notes) {
        Objects.requireNonNull(exerciseId, "exerciseId es obligatorio");
        if (orderIndex < 0) {
            throw new IllegalArgumentException("orderIndex no puede ser negativo");
        }
        if (targetSets < 1) {
            throw new IllegalArgumentException("targetSets debe ser al menos 1");
        }
        if (targetRepMin < 1) {
            throw new IllegalArgumentException("targetRepMin debe ser al menos 1");
        }
        if (targetRepMax < targetRepMin) {
            throw new IllegalArgumentException("targetRepMax debe ser mayor o igual a targetRepMin");
        }
        if (restSeconds != null && restSeconds < 0) {
            throw new IllegalArgumentException("restSeconds no puede ser negativo");
        }
        this.id = id;
        this.exerciseId = exerciseId;
        this.orderIndex = orderIndex;
        this.targetSets = targetSets;
        this.targetRepMin = targetRepMin;
        this.targetRepMax = targetRepMax;
        this.restSeconds = restSeconds;
        this.notes = notes;
    }

    public static RoutineExercise create(UUID exerciseId, int orderIndex, int targetSets, int targetRepMin,
                                          int targetRepMax, Integer restSeconds, String notes) {
        return new RoutineExercise(UUID.randomUUID(), exerciseId, orderIndex, targetSets, targetRepMin, targetRepMax, restSeconds, notes);
    }

    public static RoutineExercise rehydrate(UUID id, UUID exerciseId, int orderIndex, int targetSets, int targetRepMin,
                                             int targetRepMax, Integer restSeconds, String notes) {
        return new RoutineExercise(id, exerciseId, orderIndex, targetSets, targetRepMin, targetRepMax, restSeconds, notes);
    }

    public UUID getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoutineExercise that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
