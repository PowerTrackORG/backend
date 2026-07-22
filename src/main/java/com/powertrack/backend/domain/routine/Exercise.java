package com.powertrack.backend.domain.routine;

import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework.
 * <p>
 * Catálogo global de ejercicios: no tiene un dueño único, pero permite que un usuario
 * cree ejercicios personalizados. Decisión de diseño: {@code createdByUserId} es nullable;
 * {@code null} significa "ejercicio predefinido/global" (visible para todos los usuarios),
 * un valor no nulo significa "ejercicio personalizado", visible solo para ese usuario
 * (ver {@code ExerciseRepositoryPort#findPredefinedAndByUser}).
 */
public final class Exercise {

    private final UUID id;
    private final String name;
    private final String targetMuscle;
    private final String category;
    private final UUID createdByUserId;

    private Exercise(UUID id, String name, String targetMuscle, String category, UUID createdByUserId) {
        Objects.requireNonNull(name, "name es obligatorio");
        Objects.requireNonNull(targetMuscle, "targetMuscle es obligatorio");
        Objects.requireNonNull(category, "category es obligatorio");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name no puede estar vacío");
        }
        this.id = id;
        this.name = name;
        this.targetMuscle = targetMuscle;
        this.category = category;
        this.createdByUserId = createdByUserId;
    }

    public static Exercise create(String name, String targetMuscle, String category, UUID createdByUserId) {
        return new Exercise(UUID.randomUUID(), name, targetMuscle, category, createdByUserId);
    }

    public static Exercise rehydrate(UUID id, String name, String targetMuscle, String category, UUID createdByUserId) {
        return new Exercise(id, name, targetMuscle, category, createdByUserId);
    }

    public boolean isPredefined() {
        return createdByUserId == null;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetMuscle() {
        return targetMuscle;
    }

    public String getCategory() {
        return category;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Exercise exercise)) return false;
        return Objects.equals(id, exercise.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
