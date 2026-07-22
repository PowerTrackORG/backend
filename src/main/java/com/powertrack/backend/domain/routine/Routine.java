package com.powertrack.backend.domain.routine;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework:
 * los adaptadores de persistencia mapean esto hacia/desde su propia entidad.
 * <p>
 * Agregado raíz: expone una lista inmutable de {@link RoutineDay}, y cada día expone a
 * su vez una lista inmutable de {@link RoutineExercise}. Se modela como un único
 * agregado (en vez de entidades independientes cargadas por separado) porque una rutina
 * completa (nombre + días + ejercicios) se crea, se lee y se elimina siempre como una
 * unidad en el MVP.
 */
public final class Routine {

    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String description;
    private final Instant createdAt;
    private final List<RoutineDay> days;

    private Routine(UUID id, UUID userId, String name, String description, Instant createdAt, List<RoutineDay> days) {
        Objects.requireNonNull(userId, "userId es obligatorio");
        Objects.requireNonNull(name, "name es obligatorio");
        Objects.requireNonNull(days, "days es obligatorio");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name no puede estar vacío");
        }
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.days = List.copyOf(days);
    }

    public static Routine create(UUID userId, String name, String description, List<RoutineDay> days) {
        return new Routine(UUID.randomUUID(), userId, name, description, Instant.now(), days);
    }

    public static Routine rehydrate(UUID id, UUID userId, String name, String description, Instant createdAt, List<RoutineDay> days) {
        return new Routine(id, userId, name, description, createdAt, days);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<RoutineDay> getDays() {
        return days;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Routine routine)) return false;
        return Objects.equals(id, routine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
