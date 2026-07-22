package com.powertrack.backend.domain.workout;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework.
 * <p>
 * Registra el desempeño de UN {@code routineExercise} dentro de una sesión (peso/reps/RPE
 * por serie + notas). Apunta a {@code routineExerciseId} (no a {@code exerciseId}) para
 * poder comparar siempre contra los targets configurados por el propio usuario en su
 * rutina, tal como documenta {@link com.powertrack.backend.domain.routine.RoutineExercise}.
 * <p>
 * Nota de diseño: no almacena {@code sessionId} explícitamente porque vive siempre anidado
 * dentro de {@link WorkoutSession#getLogs()}, siguiendo la misma convención de agregado ya
 * usada en {@code Routine -&gt; RoutineDay -&gt; RoutineExercise} (donde {@code RoutineDay}
 * tampoco guarda {@code routineId}). El adaptador de persistencia sí mantiene el FK hacia
 * la sesión en la entidad JPA.
 * <p>
 * La sensación general ({@link OverallFeeling}) NO vive aquí: es una propiedad de la
 * sesión completa (ver {@link WorkoutSession}), no del ejercicio individual.
 */
public final class WorkoutLog {

    private final UUID id;
    private final UUID routineExerciseId;
    private final String notes;
    private final List<LogSet> sets;

    private WorkoutLog(UUID id, UUID routineExerciseId, String notes, List<LogSet> sets) {
        Objects.requireNonNull(routineExerciseId, "routineExerciseId es obligatorio");
        Objects.requireNonNull(sets, "sets es obligatorio");
        if (sets.isEmpty()) {
            throw new IllegalArgumentException("un log de ejercicio debe tener al menos una serie registrada");
        }
        this.id = id;
        this.routineExerciseId = routineExerciseId;
        this.notes = notes;
        this.sets = List.copyOf(sets);
    }

    public static WorkoutLog create(UUID routineExerciseId, String notes, List<LogSet> sets) {
        return new WorkoutLog(UUID.randomUUID(), routineExerciseId, notes, sets);
    }

    public static WorkoutLog rehydrate(UUID id, UUID routineExerciseId, String notes, List<LogSet> sets) {
        return new WorkoutLog(id, routineExerciseId, notes, sets);
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoutineExerciseId() {
        return routineExerciseId;
    }

    public String getNotes() {
        return notes;
    }

    public List<LogSet> getSets() {
        return sets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkoutLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
